package ru.unisuite.contentservlet.config;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import io.micrometer.core.instrument.noop.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;

import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

class MeterRegistryManager {

    static MeterRegistry prometheusMeterRegistry(String applicationName) {
        // https://habr.com/ru/post/442080/
        // https://tech.willhaben.at/monitoring-metrics-using-prometheus-a6d498dfcfba

        PrometheusMeterRegistry meterRegistry =
                new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM);
//        meterRegistry.config().commonTags(Arrays.asList(Tag.of("application", "content-servlet"), Tag.of("stack", "prod")));
        meterRegistry.config().commonTags("application", applicationName);

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


    static MeterRegistry noopMeterRegistry() {
        return new MeterRegistry(Clock.SYSTEM) {
            @Override
            protected <T> Gauge newGauge(Meter.Id id, T obj, ToDoubleFunction<T> valueFunction) {
                return new NoopGauge(id);
            }

            @Override
            protected Counter newCounter(Meter.Id id) {
                return new NoopCounter(id);
            }

            @Override
            protected LongTaskTimer newLongTaskTimer(Meter.Id id) {
                return new NoopLongTaskTimer(id);
            }

            @Override
            protected Timer newTimer(Meter.Id id, DistributionStatisticConfig distributionStatisticConfig, PauseDetector pauseDetector) {
                return new NoopTimer(id);
            }

            @Override
            protected DistributionSummary newDistributionSummary(Meter.Id id, DistributionStatisticConfig distributionStatisticConfig, double scale) {
                return new NoopDistributionSummary(id);
            }

            @Override
            protected Meter newMeter(Meter.Id id, Meter.Type type, Iterable<Measurement> measurements) {
                return new NoopMeter(id);
            }

            @Override
            protected <T> FunctionTimer newFunctionTimer(Meter.Id id, T obj, ToLongFunction<T> countFunction, ToDoubleFunction<T> totalTimeFunction, TimeUnit totalTimeFunctionUnit) {
                return new NoopFunctionTimer(id);
            }

            @Override
            protected <T> FunctionCounter newFunctionCounter(Meter.Id id, T obj, ToDoubleFunction<T> countFunction) {
                return new NoopFunctionCounter(id);
            }

            @Override
            protected TimeUnit getBaseTimeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            protected DistributionStatisticConfig defaultHistogramConfig() {
                return DistributionStatisticConfig.NONE;
            }
        };
    }
}
