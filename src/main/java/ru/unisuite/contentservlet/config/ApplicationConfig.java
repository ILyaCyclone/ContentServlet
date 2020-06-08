package ru.unisuite.contentservlet.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.repository.*;
import ru.unisuite.contentservlet.service.ContentService;
import ru.unisuite.contentservlet.service.ContentServiceImpl;
import ru.unisuite.imageprocessing.ImageProcessor;

import javax.sql.DataSource;
import java.util.Map;

public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class.getName());

    private final BuildProperties buildProperties;

//    private final DataSource dataSource;

    private final ContentServiceImpl contentService;
    private final Map<ResizerType, ImageProcessor> imageProcessors;

    private final String defaultHttpCacheControl;

    private final ResizerType resizerType;


    private final byte defaultImageQuality;

    private final MeterRegistry meterRegistry;

    public ApplicationConfig(ContentServletProperties prop, BuildProperties buildProperties, PropertyResolver propertyResolver) {
        this.buildProperties = buildProperties;

        DataSource dataSource = DataSourceManager.getDataSource(prop);


        this.resizerType = ResizerType.forValue(prop.getResizerType());

        ContentRepository contentRepository = new ContentRepositoryImpl(dataSource, new ContentRowMapper());

        HashAndLastModifiedRepository hashAndLastModifiedRepository = new HashAndLastModifiedRepositoryImpl(dataSource
                , new HashAndLastModifiedRowMapper());

        this.contentService = new ContentServiceImpl(contentRepository, hashAndLastModifiedRepository, this.resizerType);


        this.defaultHttpCacheControl = prop.getCacheControl();

        this.defaultImageQuality = Byte.parseByte(prop.getImageQuality());


        this.imageProcessors = ImageProcessorsManager.implementations(propertyResolver);


        this.meterRegistry = prop.isEnableMetrics() ? MeterRegistryManager.prometheusMeterRegistry(prop.getApplicationName())
                : MeterRegistryManager.noopMeterRegistry();
    }


    public BuildProperties getBuildProperties() {
        return buildProperties;
    }

    public ContentService contentService() {
        return contentService;
    }

    public ResizerType getResizerType() {
        return resizerType;
    }

    public String getCacheControl() {
        return this.defaultHttpCacheControl;
    }

    public byte getDefaultImageQuality() {
        return this.defaultImageQuality;
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    public Map<ResizerType, ImageProcessor> getImageProcessors() {
        return imageProcessors;
    }
}
