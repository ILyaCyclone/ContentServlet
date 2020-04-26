package ru.unisuite.contentservlet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.repository.*;
import ru.unisuite.contentservlet.service.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class.getName());
    private final static String CONFIG_FILE_NAME = "application.properties";

    private final DataSource dataSource;

    private final boolean persistentCacheEnabled; // persistent cache
    private final NameCreator cacheFilenameCreator;

    private final String httpCacheControlDefaultValue; // HTTP cache default value

    private final ResizerType resizerType;

    private byte defaultQuality = 80;


    public ApplicationConfig() {
        this(CONFIG_FILE_NAME);
    }

    public ApplicationConfig(String configFile) {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);

            DataSource dataSource;
            String datasourceJndiName = prop.getProperty("contentservlet.datasource.jndi-name");
            if(datasourceJndiName != null) {
                dataSource = new DataSourceManager().lookup(datasourceJndiName);
            } else {
                String datasourceUrl = prop.getProperty("contentservlet.datasource.url");
                String datasourceUsername = prop.getProperty("contentservlet.datasource.username");
                String datasourcePassword = prop.getProperty("contentservlet.datasource.password");
                dataSource = new DataSourceManager().createDataSource(datasourceUrl, datasourceUsername, datasourcePassword);
            }
            if(dataSource == null) {
                throw new RuntimeException("Unable to configure jdbc DataSource");
            }
            this.dataSource = dataSource;


            this.persistentCacheEnabled = Boolean.parseBoolean(prop.getProperty("contentservlet.usecache"));
            this.cacheFilenameCreator = persistentCacheEnabled ? new NameCreator() : null;

            this.httpCacheControlDefaultValue = prop.getProperty("contentservlet.cachecontrol");

            this.resizerType = ResizerType.valueOf(prop.getProperty("contentservlet.resizer-type").toUpperCase());

        } catch (IOException e) {
            String errorMessage = "Unable to load " + CONFIG_FILE_NAME;
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }



    public ContentService contentService() {
        return new ContentServiceImpl(contentRepository(), hashAndLastModifiedRepository(), resizerType);
    }

    public ResizeService resizeService() {
//        return new ResizeServiceImpl(ImageResizerFactory.getImageResizer(), defaultQuality);
        return new ResizeServiceIm4java();
    }

    public ResizerType getResizerType() {
        return resizerType;
    }

    public String getCacheControl() {
        return this.httpCacheControlDefaultValue;
    }

    // this is about persistent server side cache, not about HTTP cache
    public boolean isPersistentCacheEnabled() {
        return persistentCacheEnabled;
    }

    public ContentRepository contentRepository() {
        return new ContentRepositoryImpl(dataSource, new ContentRowMapper());
    }

    public HashAndLastModifiedRepository hashAndLastModifiedRepository() {
        return new HashAndLastModifiedRepositoryImpl(dataSource, new HashAndLastModifiedRowMapper());
    }

}
