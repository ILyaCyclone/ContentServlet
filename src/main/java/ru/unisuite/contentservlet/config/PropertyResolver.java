package ru.unisuite.contentservlet.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropertyResolver {
    // in order of priority
    private final Properties systemProperties = System.getProperties();
    private final Map<String, String> environmentProperties = System.getenv();

    private List<Properties> propertiesList;

    public void addProperties(String propertyFileName) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(propertyFileName)) {
            properties.load(input);
        }

        if (propertiesList == null) {
            propertiesList = new ArrayList<>();
        }
        propertiesList.add(properties);
    }

    public String resolve(String key) {
        return resolve(key, null);
    }

    public String resolve(String key, String defaultValue) {
        if (systemProperties.containsKey(key)) {
            return systemProperties.getProperty(key);
        }

        String envPropertyKey = systemPropertyToEnvProperty(key);
        if (environmentProperties.containsKey(envPropertyKey)) {
            return environmentProperties.get(envPropertyKey);
        }

        if(propertiesList != null) {
            for (Properties properties : propertiesList) {
                if(properties.containsKey(key)) {
                    return properties.getProperty(key);
                }
            }
        }
        return defaultValue;
    }

    /**
     * https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-relaxed-binding-from-environment-variables
     * To convert a property name in the canonical-form to an environment variable name you can follow these rules:
     * Replace dots (.) with underscores (_).
     * Remove any dashes (-).
     * Convert to uppercase.
     */
    private String systemPropertyToEnvProperty(String key) {
        return key.replace('.', '_')
                .replace("-", "")
                .toUpperCase();
    }
}
