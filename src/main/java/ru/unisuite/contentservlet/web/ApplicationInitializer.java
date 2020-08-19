package ru.unisuite.contentservlet.web;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.filter.MetricsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.config.BuildProperties;
import ru.unisuite.contentservlet.config.ContentServletProperties;
import ru.unisuite.contentservlet.config.PropertyResolver;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@WebListener
public class ApplicationInitializer implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        PropertyResolver propertyResolver = new PropertyResolver();
        String propertyFileUri = propertyResolver.resolve("contentservlet.config.location", "application.properties");
        try {
            propertyResolver.addProperties(propertyFileUri);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load property file '" + propertyFileUri + '\'', e);
        }

        try {
            propertyResolver.addProperties("default.properties");
        } catch (Exception e) {
            throw new RuntimeException("Unable to load property file 'default.properties'", e);
        }

        ContentServletProperties contentServletProperties = new ContentServletProperties(propertyResolver);

        if(logger.isInfoEnabled()) logger.info("Initializing content-servlet with properties: {}"
                , contentServletProperties.toString().replace(ContentServletProperties.class.getSimpleName(), ""));

        BuildProperties buildProperties = null;
        try {
            buildProperties = new BuildProperties();
        } catch (IOException e) {
            logger.warn("Could not create buildProperties", e);
        }

        ApplicationConfig applicationConfig = new ApplicationConfig(contentServletProperties, buildProperties, propertyResolver);
        servletContext.setAttribute("applicationConfig", applicationConfig);

        registerContentServlet(servletContext, applicationConfig, contentServletProperties);
        registerConfigServlet(servletContext, applicationConfig, contentServletProperties);

        if (contentServletProperties.isEnableMetrics()) {
            registerPrometheusFilter(servletContext, contentServletProperties);
            registerCustomMetricsFilter(servletContext, contentServletProperties);
            registerPrometheusServlet(servletContext);
        }

        logger.info("Listening for content requests on url pattern '{}'...", contentServletProperties.getContentUrlPattern());
    }

    private void registerContentServlet(ServletContext servletContext, ApplicationConfig applicationConfig
            , ContentServletProperties contentServletProperties) {
        registerServlet(servletContext, "contentServlet", new ContentServlet(applicationConfig)
                , contentServletProperties.getContentUrlPattern(), contentServletProperties.getContentSecureUrlPattern());
    }

    private void registerConfigServlet(ServletContext servletContext, ApplicationConfig applicationConfig
            , ContentServletProperties contentServletProperties) {
        ConfigServlet configServlet = new ConfigServlet(applicationConfig, contentServletProperties);
        registerServlet(servletContext, "config", configServlet, "/config");
    }



    private void registerPrometheusFilter(ServletContext servletContext, ContentServletProperties contentServletProperties) {
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("metric-name", "contentservlet_request_duration_seconds");
        initParameters.put("help", "The time taken fulfilling servlet requests");
        initParameters.put("buckets", "0.25,0.5,1,1.5,2,3,5,10"); // buckets in seconds
        initParameters.put("path-components", "2"); // group uri by levels, including root. E.g. 1 for /content/*, 2 for /content/get/*

        FilterRegistration.Dynamic prometheusFilter = servletContext.addFilter("prometheusFilter", MetricsFilter.class);
        prometheusFilter.setInitParameters(initParameters);
        prometheusFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true
                , contentServletProperties.getContentUrlPattern(), contentServletProperties.getContentSecureUrlPattern());
    }

    private void registerCustomMetricsFilter(ServletContext servletContext, ContentServletProperties contentServletProperties) {
        FilterRegistration.Dynamic customMetricsFilter = servletContext.addFilter("customMetricsFilter", CustomMetricsFilter.class);
        customMetricsFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true
                , contentServletProperties.getContentUrlPattern(), contentServletProperties.getContentSecureUrlPattern());
    }

    private void registerPrometheusServlet(ServletContext servletContext) {
        registerServlet(servletContext, "prometheus", MetricsServlet.class, "/metrics");
    }



    private void registerServlet(ServletContext servletContext, String servletName, Class<? extends Servlet> servletClass
            , String... urlPatterns) {
        ServletRegistration.Dynamic prometheusServlet = servletContext.addServlet(servletName, servletClass);
        prometheusServlet.setLoadOnStartup(1);
        prometheusServlet.addMapping(urlPatterns);
    }

    private void registerServlet(ServletContext servletContext, String servletName, Servlet servlet
            , String... urlPatterns) {
        ServletRegistration.Dynamic prometheusServlet = servletContext.addServlet(servletName, servlet);
        prometheusServlet.setLoadOnStartup(1);
        prometheusServlet.addMapping(urlPatterns);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no action needed
    }
}
