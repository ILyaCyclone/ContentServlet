package ru.unisuite.contentservlet.web;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.filter.MetricsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.config.BuildProperties;
import ru.unisuite.contentservlet.config.ContentServletProperties;

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

        String propertyFileUri = System.getProperty("contentservlet.config.location"
                , System.getenv("CONTENTSERVLET_CONFIG_LOCATION"));
        ContentServletProperties contentServletProperties = propertyFileUri == null
                ? new ContentServletProperties()
                : new ContentServletProperties(propertyFileUri);

        logger.info("Initializing content-servlet with properties: {}"
                , contentServletProperties.toString().replace(ContentServletProperties.class.getSimpleName(), ""));

        BuildProperties buildProperties = null;
        try {
            buildProperties = new BuildProperties();
        } catch (IOException e) {
            logger.warn("Could not create buildProperties", e);
        }
        ApplicationConfig applicationConfig = new ApplicationConfig(contentServletProperties, buildProperties);
        servletContext.setAttribute("applicationConfig", applicationConfig);


        registerContentServlet(servletContext, applicationConfig, contentServletProperties);

        if (contentServletProperties.isEnableMetrics()) {
            registerPrometheusFilter(servletContext, contentServletProperties);
            registerCustomMetricsFilter(servletContext, contentServletProperties);
            registerPrometheusServlet(servletContext);
        }

        logger.info("Listening for content requests on url pattern '{}'...", contentServletProperties.getContentUrlPattern());
    }

    private void registerContentServlet(ServletContext servletContext, ApplicationConfig applicationConfig
            , ContentServletProperties contentServletProperties) {
        ContentServlet contentServlet = new ContentServlet(applicationConfig);
        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet("contentServlet", contentServlet);
        servletRegistration.setLoadOnStartup(1);

        servletRegistration.addMapping(contentServletProperties.getContentUrlPattern()
                , contentServletProperties.getContentSecureUrlPattern());
    }



    private void registerPrometheusFilter(ServletContext servletContext, ContentServletProperties contentServletProperties) {
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("metric-name", "contentservlet_metrics_filter");
        initParameters.put("help", "The time taken fulfilling servlet requests");
        initParameters.put("buckets", "0.5,0.75,0.9,0.95");
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
        ServletRegistration.Dynamic prometheusServlet = servletContext.addServlet("prometheus", MetricsServlet.class);
        prometheusServlet.setLoadOnStartup(1);
        prometheusServlet.addMapping("/metrics");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no action needed
    }
}
