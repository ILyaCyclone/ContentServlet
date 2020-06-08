package ru.unisuite.contentservlet.service;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.unisuite.contentservlet.model.Content;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentByWmAlias extends ContentServiceParentTest {
    private static final String EXPECTED_FOLDER = "ContentByWmAlias";

    @Test
    void byWmAlias() throws IOException {
        String expectedFilename = "wmalias-portal2.svg-sprite.svg";

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setMetatermAlias("portal2.svg-sprite");
        Content content = contentService.getContent(contentRequest);

        byte[] expectedBytes = expectedBytes(expectedFilename);
        byte[] actualBytes = IOUtils.toByteArray(content.getDataStream());
        Assertions.assertAll(
                () -> assertEquals("svg-sprite-miit", content.getFilename())
                , () -> assertEquals("svg", content.getExtension())
                , () -> assertEquals("image/svg+xml", content.getMimeType())
                , () -> assertArrayEquals(expectedBytes, actualBytes, "bytes mismatch")
        );
    }

    private byte[] expectedBytes(String expectedFilename) throws IOException {
        return super.getExpectedFileBytes(EXPECTED_FOLDER + File.separator + expectedFilename);
    }
}
