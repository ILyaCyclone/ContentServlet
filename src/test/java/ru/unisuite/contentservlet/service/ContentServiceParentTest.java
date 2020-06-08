package ru.unisuite.contentservlet.service;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.config.ContentServletProperties;
import ru.unisuite.contentservlet.config.PropertyResolver;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Run with real database, get jdbc connection properties from /src/test/resources/application.properties
 */
class ContentServiceParentTest {
    static ContentService contentService;

    @BeforeAll
    static void init() throws IOException {
        PropertyResolver propertyResolver = new PropertyResolver();
        propertyResolver.addProperties("jdbc.properties");
        propertyResolver.addProperties("default.properties");

        ContentServletProperties contentServletProperties = new ContentServletProperties(propertyResolver);

        ApplicationConfig applicationConfig = new ApplicationConfig(contentServletProperties, null, propertyResolver);
        contentService = applicationConfig.contentService();
    }



    byte[] getExpectedFileBytes(String filename) throws IOException {
        URL expectedFileUrl = this.getClass().getClassLoader().getResource("expected" + File.separator + filename);
        return IOUtils.toByteArray(expectedFileUrl);
    }
}
