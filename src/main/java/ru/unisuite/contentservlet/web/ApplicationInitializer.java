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

    public static final String CONTENT_URL_PATTERN = "/get/*";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        ContentServletProperties contentServletProperties = new ContentServletProperties();
        logger.debug("Initializing content-servlet with properties: {}, listening for content requests on url pattern '{}'..."
                , contentServletProperties.toString().replace(ContentServletProperties.class.getSimpleName(), "")
                , CONTENT_URL_PATTERN);

        BuildProperties buildProperties = null;
        try {
            buildProperties = new BuildProperties();
        } catch (IOException e) {
            logger.warn("Could not create buildProperties", e);
        }
        ApplicationConfig applicationConfig = new ApplicationConfig(contentServletProperties, buildProperties);

        servletContext.setAttribute("applicationConfig", applicationConfig);

        if (contentServletProperties.isEnableMetrics()) {
            registerPrometheusFilter(servletContext);
            registerCustomMetricsFilter(servletContext);
            registerPrometheusServlet(servletContext);
        }
    }



    private void registerPrometheusFilter(ServletContext servletContext) {
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("metric-name", "contentservlet_metrics_filter");
        initParameters.put("help", "The time taken fulfilling servlet requests");
        initParameters.put("buckets", "0.5,0.75,0.9,0.95");
        initParameters.put("path-components", "2"); // group uri by levels, including root. E.g. 1 for /content/*, 2 for /content/get/*

        FilterRegistration.Dynamic prometheusFilter = servletContext.addFilter("prometheusFilter", MetricsFilter.class);
        prometheusFilter.setInitParameters(initParameters);
        prometheusFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, CONTENT_URL_PATTERN);
    }

    private void registerCustomMetricsFilter(ServletContext servletContext) {
        FilterRegistration.Dynamic customMetricsFilter = servletContext.addFilter("customMetricsFilter", CustomMetricsFilter.class);
        customMetricsFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, CONTENT_URL_PATTERN);
    }

    private void registerPrometheusServlet(ServletContext servletContext) {
        ServletRegistration.Dynamic prometheusServlet = servletContext.addServlet("prometheus", MetricsServlet.class);
        prometheusServlet.setLoadOnStartup(1);
        prometheusServlet.addMapping("/metrics");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
