package ru.unisuite.contentservlet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.repository.ContentRepository;
import ru.unisuite.contentservlet.repository.ContentRepositoryImpl;
import ru.unisuite.contentservlet.service.ContentService;
import ru.unisuite.contentservlet.service.ContentServiceImpl;
import ru.unisuite.contentservlet.service.NameCreator;

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


    public ApplicationConfig() {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            Properties prop = new Properties();
            prop.load(input);

            String datasourceJndiName = prop.getProperty("ru.unisuite.contentservlet.jndi.datasource.name");
            this.dataSource = new DataSourceLookup().lookup(datasourceJndiName);

            this.persistentCacheEnabled = Boolean.parseBoolean(prop.getProperty("ru.unisuite.contentservlet.usecache"));
            this.cacheFilenameCreator = persistentCacheEnabled ? new NameCreator() : null;

            this.httpCacheControlDefaultValue = prop.getProperty("ru.unisuite.contentservlet.cachecontrol");

        } catch (IOException e) {
            String errorMessage = "Unable to load " + CONFIG_FILE_NAME;
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }



    public ContentService contentService() {
        return new ContentServiceImpl(contentRepository(), persistentCacheEnabled, cacheFilenameCreator);
    }

    public String getCacheControl() {
        return this.httpCacheControlDefaultValue;
    }

    // this is about persistent server side cache, not about HTTP cache
    public boolean isPersistentCacheEnabled() {
        return persistentCacheEnabled;
    }

    private ContentRepository contentRepository() {
        return new ContentRepositoryImpl(dataSource, persistentCacheEnabled);
    }
}
