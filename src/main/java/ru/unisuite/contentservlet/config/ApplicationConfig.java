package ru.unisuite.contentservlet.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.repository.*;
import ru.unisuite.contentservlet.service.ContentService;
import ru.unisuite.contentservlet.service.ContentServiceImpl;
import ru.unisuite.contentservlet.service.ResizeService;
import ru.unisuite.contentservlet.service.ResizeServiceIm4java;

import javax.sql.DataSource;

public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class.getName());

    private final DataSource dataSource;

    private final ContentServiceImpl contentService;
    private final ContentRepositoryImpl contentRepository;
    private final HashAndLastModifiedRepositoryImpl hashAndLastModifiedRepository;
    private final ResizeServiceIm4java resizeService;

    private final String defaultHttpCacheControl;

    private final ResizerType resizerType;

    private final byte defaultImageQuality;

    private final MeterRegistry meterRegistry;


    public ApplicationConfig(ContentServletProperties prop) {

        try {
            if (prop.getDatasourceJndiName() != null) {
                this.dataSource = DataSourceManager.lookup(prop.getDatasourceJndiName());
            } else {
                String datasourceUrl = prop.getDatasourceUrl();
                String datasourceUsername = prop.getDatasourceUsername();
                String datasourcePassword = prop.getDatasourcePassword();
                this.dataSource = DataSourceManager.createDataSource(datasourceUrl, datasourceUsername, datasourcePassword);
            }
        } catch (Exception e) {
            logger.error("Unable to configure jdbc dataSource", e);
            throw new RuntimeException("Unable to configure jdbc dataSource", e);
        }


        this.resizerType = ResizerType.forValue(prop.getResizerType());

        this.contentRepository = new ContentRepositoryImpl(this.dataSource, new ContentRowMapper());

        this.hashAndLastModifiedRepository = new HashAndLastModifiedRepositoryImpl(this.dataSource, new HashAndLastModifiedRowMapper());

        this.contentService = new ContentServiceImpl(this.contentRepository, this.hashAndLastModifiedRepository, this.resizerType);

        this.resizeService = new ResizeServiceIm4java();


        this.defaultHttpCacheControl = prop.getCacheControl();

        this.defaultImageQuality = Byte.parseByte(prop.getImageQuality());

        this.meterRegistry = prop.isEnableMetrics() ? MeterRegistryManager.prometheusMeterRegistry(prop.getApplicationName())
                : MeterRegistryManager.noopMeterRegistry();
    }



    public ContentService contentService() {
        return contentService;
    }

    public ResizeService resizeService() {
//        return new ResizeServiceImpl(ImageResizerFactory.getImageResizer(), defaultQuality);
        return this.resizeService;
    }

    public ResizerType getResizerType() {
        return resizerType;
    }

    public String getCacheControl() {
        return this.defaultHttpCacheControl;
    }

    public ContentRepository contentRepository() {
        return this.contentRepository;
    }

    public HashAndLastModifiedRepository hashAndLastModifiedRepository() {
        return this.hashAndLastModifiedRepository;
    }

    public byte getDefaultImageQuality() {
        return this.defaultImageQuality;
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
