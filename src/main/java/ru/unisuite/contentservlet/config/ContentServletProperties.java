package ru.unisuite.contentservlet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class ContentServletProperties {
    private static final Logger logger = LoggerFactory.getLogger(ContentServletProperties.class);
    private static final String CONFIG_FILE_NAME = "application.properties";
    private static final String PREFIX = "contentservlet.";

    private final String applicationName;
    private final String contentUrlPattern;
    private final String contentSecureUrlPattern;

    private final String datasourceJndiName;
    private final String datasourceUrl;
    private final String datasourcePassword;
    private final String datasourceUsername;

    private final String cacheControl;
    private final String resizerType;
    private final String imageQuality;

    private final boolean enableMetrics;

    private final Map<String, String> allProperties;

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
        } catch (Exception e) {
            String errorMessage = "Unable to load '" + configFilePath + '\'';
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }

        this.allProperties = defaultProp.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put((String) entry.getKey(), (String) entry.getValue()), HashMap::putAll);
        prop.forEach((key, value) -> this.allProperties.put((String) key, (String) value));

        this.applicationName = getPropertyOrDefault(prop, defaultProp, PREFIX + "application-name");
        this.contentUrlPattern = getPropertyOrDefault(prop, defaultProp, PREFIX + "content-url-pattern");
        this.contentSecureUrlPattern = getPropertyOrDefault(prop, defaultProp, PREFIX + "content-secure-url-pattern");

        this.datasourceJndiName = prop.getProperty(PREFIX + "datasource.jndi-name");
        this.datasourceUrl = prop.getProperty(PREFIX + "datasource.url");
        this.datasourceUsername = prop.getProperty(PREFIX + "datasource.username");
        this.datasourcePassword = prop.getProperty(PREFIX + "datasource.password");

        this.cacheControl = getPropertyOrDefault(prop, defaultProp, PREFIX + "cachecontrol");

        this.resizerType = getPropertyOrDefault(prop, defaultProp, PREFIX + "resizer-type");
        this.imageQuality = getPropertyOrDefault(prop, defaultProp, PREFIX + "image-quality");

        this.enableMetrics = Boolean.parseBoolean(getPropertyOrDefault(prop, defaultProp, PREFIX + "enable-metrics"));
    }

    private String getPropertyOrDefault(Properties prop, Properties defaultProp, String key) {
        return prop.getProperty(key, defaultProp.getProperty(key));
    }



    public String getApplicationName() {
        return applicationName;
    }

    public String getContentUrlPattern() {
        return contentUrlPattern;
    }

    public String getContentSecureUrlPattern() {
        return contentSecureUrlPattern;
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

    public boolean isEnableMetrics() {
        return enableMetrics;
    }

    public Map<String, String> values() {
        return allProperties;
    }

    @Override
    public String toString() {
        return "ContentServletProperties{"
                + values().entrySet().stream()
                    .map(e -> e.getKey() + "='" + (isSecret(e) ? "***" : e.getValue())+'\'')
                    .collect(Collectors.joining(", "))
                + '}';
    }

    private boolean isSecret(Map.Entry<String, String> e) {
        return e.getKey().contains("password");
    }
}
