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
        String expectedResourceFilename = "wmalias-test.star.png";

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setMetatermAlias("test.star.png");
        Content content = contentService.getContent(contentRequest);

        byte[] expectedBytes = expectedBytes(expectedResourceFilename);
        byte[] actualBytes = IOUtils.toByteArray(content.getDataStream());
        Assertions.assertAll(
                () -> assertEquals("star", content.getFilename(), "filename mismatch")
                , () -> assertEquals("png", content.getExtension(), "extension mismatch")
                , () -> assertEquals("image/png", content.getMimeType(), "mimetype mismatch")
                , () -> assertEquals("7C1C54F29C9BD797C5FD9E082D938C67BF419E2A", content.getHash(), "hash mismatch")
                , () -> assertEquals(528L, content.getSize())
                , () -> assertArrayEquals(expectedBytes, actualBytes, "bytes mismatch")
        );
    }

    private byte[] expectedBytes(String expectedResourceFilename) throws IOException {
        return super.getExpectedFileBytes(EXPECTED_FOLDER + File.separator + expectedResourceFilename);
    }
}
