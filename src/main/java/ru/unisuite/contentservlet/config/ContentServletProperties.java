package ru.unisuite.contentservlet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ContentServletProperties {
    private static final Logger logger = LoggerFactory.getLogger(ContentServletProperties.class);
    private static final String CONFIG_FILE_NAME = "application.properties";
    private static final String PREFIX = "contentservlet.";

    private final String datasourceJndiName;
    private final String datasourceUrl;
    private final String datasourcePassword;
    private final String datasourceUsername;

    private final String cacheControl;
    private final String resizerType;
    private final String imageQuality;

    public ContentServletProperties() {
        this(CONFIG_FILE_NAME);
    }

    public ContentServletProperties(String configFilePath) {
        Properties prop = new Properties();
        Properties defaultProp = new Properties();
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(configFilePath);
             InputStream defaultInput = this.getClass().getClassLoader().getResourceAsStream("default.properties")) {
            prop.load(input);
            defaultProp.load(defaultInput);
        } catch (IOException e) {
            String errorMessage = "Unable to load " + CONFIG_FILE_NAME;
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }

        this.datasourceJndiName = prop.getProperty(PREFIX + "datasource.jndi-name");
        this.datasourceUrl = prop.getProperty(PREFIX + "datasource.url");
        this.datasourceUsername = prop.getProperty(PREFIX + "datasource.username");
        this.datasourcePassword = prop.getProperty(PREFIX + "datasource.password");

        this.cacheControl = getPropertyOrDefault(prop, defaultProp, PREFIX + "cachecontrol");

        this.resizerType = getPropertyOrDefault(prop, defaultProp, PREFIX + "resizer-type");
        this.imageQuality = getPropertyOrDefault(prop, defaultProp, PREFIX + "imagequality");
    }

    private String getPropertyOrDefault(Properties prop, Properties defaultProp, String key) {
        String value = prop.getProperty(key);
        if (value != null) {
            return value;
        }
        return defaultProp.getProperty(key);
    }



    public String getDatasourceJndiName() {
        return datasourceJndiName;
    }

    public String getDatasourceUrl() {
        return datasourceUrl;
    }

    public String getDatasourcePassword() {
        return datasourcePassword;
    }

    public String getDatasourceUsername() {
        return datasourceUsername;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public String getResizerType() {
        return resizerType;
    }

    public String getImageQuality() {
        return imageQuality;
    }
}
