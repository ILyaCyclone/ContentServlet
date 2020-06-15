package ru.unisuite.contentservlet.service;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.model.Content;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageByIdFeResizerDBTest extends ContentServiceParentTest {
    private static final String EXPECTED_FOLDER = "ImageByIdFeResizerDB";

    @Test
    void actualSize() throws IOException {
        String expectedResourceFilename = "idfe-64285_rt-db_q-80.jpg";
        final int expectedSize = 2986903;

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdFe(64285L);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content actualContent = contentService.getContent(contentRequest);

        assertActualContent(expectedResourceFilename, expectedSize, actualContent);
    }

    @Test
    void resizeByWidth() throws IOException {
        String expectedResourceFilename = "idfe-64285_w-500_rt-db_q-80.jpg";
        final int expectedSize = 35415;

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdFe(64285L);
        contentRequest.setWidth(500);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content actualContent = contentService.getContent(contentRequest);

        assertActualContent(expectedResourceFilename, expectedSize, actualContent);
    }

    @Test
    void resizeByHeight() throws IOException {
        String expectedResourceFilename = "idfe-64285_h-500_rt-db_q-80.jpg";
        final int expectedSize = 19436;

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdFe(64285L);
        contentRequest.setHeight(500);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content actualContent = contentService.getContent(contentRequest);

        assertActualContent(expectedResourceFilename, expectedSize, actualContent);
    }

    @Test
    void resizeByWithAndHeight() throws IOException {
        String expectedResourceFilename = "idfe-64285_w-400_h-600_rt-db_q-80.jpg";
        final int expectedSize = 25179;

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdFe(64285L);
        contentRequest.setWidth(400);
        contentRequest.setHeight(600);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content actualContent = contentService.getContent(contentRequest);

        assertActualContent(expectedResourceFilename, expectedSize, actualContent);
    }

    private void assertActualContent(String expectedResourceFilename, int expectedSize, Content content) throws IOException {
        byte[] expectedBytes = super.getExpectedFileBytes(EXPECTED_FOLDER + File.separator + expectedResourceFilename);
        byte[] actualBytes = IOUtils.toByteArray(content.getDataStream());
        Assertions.assertAll(
                () -> assertEquals("64285", content.getFilename(), "filename mismatch")
                , () -> assertEquals("jpg", content.getExtension(), "extension mismatch")
                , () -> assertEquals("image/jpeg", content.getMimeType(), "mimetype mismatch")
                , () -> assertEquals(null, content.getHash(), "hash mismatch") // DB function doesn't support hash
                , () -> assertEquals(expectedSize, content.getSize(), "size mismatch")
                , () -> assertArrayEquals(expectedBytes, actualBytes, "bytes mismatch")
        );
    }
}