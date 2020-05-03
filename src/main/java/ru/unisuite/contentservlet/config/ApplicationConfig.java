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

//    private final DataSource dataSource;

    private final ContentServiceImpl contentService;
    private final Map<ResizerType, ImageProcessor> imageProcessors;

    private final String defaultHttpCacheControl;

    private final ResizerType resizerType;


    private final byte defaultImageQuality;

    private final MeterRegistry meterRegistry;


    public ApplicationConfig(ContentServletProperties prop) {
        DataSource dataSource;
        try {
            if (prop.getDatasourceJndiName() != null) {
                dataSource = DataSourceManager.lookup(prop.getDatasourceJndiName());
            } else {
                String datasourceUrl = prop.getDatasourceUrl();
                String datasourceUsername = prop.getDatasourceUsername();
                String datasourcePassword = prop.getDatasourcePassword();
                dataSource = DataSourceManager.createDataSource(datasourceUrl, datasourceUsername, datasourcePassword);
            }
        } catch (Exception e) {
            logger.error("Unable to configure jdbc dataSource", e);
            throw new RuntimeException("Unable to configure jdbc dataSource", e);
        }


        this.resizerType = ResizerType.forValue(prop.getResizerType());

        ContentRepository contentRepository = new ContentRepositoryImpl(dataSource, new ContentRowMapper());

        HashAndLastModifiedRepository hashAndLastModifiedRepository = new HashAndLastModifiedRepositoryImpl(dataSource, new HashAndLastModifiedRowMapper());

        this.contentService = new ContentServiceImpl(contentRepository, hashAndLastModifiedRepository, this.resizerType);


        this.defaultHttpCacheControl = prop.getCacheControl();

        this.defaultImageQuality = Byte.parseByte(prop.getImageQuality());


        this.imageProcessors = ImageProcessorsManager.implementations(prop);


        this.meterRegistry = prop.isEnableMetrics() ? MeterRegistryManager.prometheusMeterRegistry(prop.getApplicationName())
                : MeterRegistryManager.noopMeterRegistry();
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
