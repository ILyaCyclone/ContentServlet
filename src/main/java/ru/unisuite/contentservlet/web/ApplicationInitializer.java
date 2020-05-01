package ru.unisuite.contentservlet.web;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.config.ContentServletProperties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        ContentServletProperties contentServletProperties = new ContentServletProperties();
        ApplicationConfig applicationConfig = new ApplicationConfig(contentServletProperties);

        servletContext.setAttribute("applicationConfig", applicationConfig);
        servletContext.setAttribute("meterRegistry", meterRegistry());
    }

    private MeterRegistry meterRegistry() {
        // https://habr.com/ru/post/442080/
        // https://tech.willhaben.at/monitoring-metrics-using-prometheus-a6d498dfcfba

        PrometheusMeterRegistry meterRegistry =
                new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM);
//        meterRegistry.config().commonTags(Arrays.asList(Tag.of("application", "content-servlet"), Tag.of("stack", "prod")));
        meterRegistry.config().commonTags("application", "content-servlet");

//        new ClassLoaderMetrics().bindTo(meterRegistry);
        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
//        new JvmThreadMetrics().bindTo(meterRegistry);
        new ProcessorMetrics().bindTo(meterRegistry);
//        new DiskSpaceMetrics("d:\\" or "/" for linux?).bindTo(meterRegistry);
        new FileDescriptorMetrics().bindTo(meterRegistry);
        new UptimeMetrics().bindTo(meterRegistry);
        new LogbackMetrics().bindTo(meterRegistry);
        return meterRegistry;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
