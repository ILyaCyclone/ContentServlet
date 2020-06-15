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

class ImageByIdwmResizerDBTest extends ContentServiceParentTest {
    private static final String EXPECTED_FOLDER = "ImageByIdwmResizerDB";

    @Test
    void actualSize() throws IOException {
        String expectedResourceFilename = "idwm-842025_rt-db_q-80.jpg";
        final int expectedSize = 6599730;

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdWebMetaterm(842025L);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content actualContent = contentService.getContent(contentRequest);

        assertActualContent(expectedResourceFilename, expectedSize, actualContent);
    }

    @Test
    void resizeByWidth() throws IOException {
        String expectedResourceFilename = "idwm-842025_w-500_rt-db_q-80.jpg";
        final int expectedSize = 24889;

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdWebMetaterm(842025L);
        contentRequest.setWidth(500);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content actualContent = contentService.getContent(contentRequest);

        assertActualContent(expectedResourceFilename, expectedSize, actualContent);
    }

    @Test
    void resizeByHeight() throws IOException {
        String expectedResourceFilename = "idwm-842025_h-500_rt-db_q-80.jpg";
        final int expectedSize = 65976;

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdWebMetaterm(842025L);
        contentRequest.setHeight(500);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content actualContent = contentService.getContent(contentRequest);

        assertActualContent(expectedResourceFilename, expectedSize, actualContent);
    }

    @Test
    void resizeByWithAndHeight() throws IOException {
        String expectedResourceFilename = "idwm-842025_w-600_h-300_rt-db_q-80.jpg";
        final int expectedSize = 29309;

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdWebMetaterm(842025L);
        contentRequest.setWidth(600);
        contentRequest.setHeight(300);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content actualContent = contentService.getContent(contentRequest);

        assertActualContent(expectedResourceFilename, expectedSize, actualContent);
    }

    private void assertActualContent(String expectedResourceFilename, int expectedSize, Content actualContent) throws IOException {
        byte[] expectedBytes = super.getExpectedFileBytes(EXPECTED_FOLDER + File.separator + expectedResourceFilename);
        byte[] actualBytes = IOUtils.toByteArray(actualContent.getDataStream());
        Assertions.assertAll(
                () -> assertEquals("Фото1", actualContent.getFilename(), "filename mismatch")
                , () -> assertEquals("jpg", actualContent.getExtension(), "extension mismatch")
                , () -> assertEquals("image/jpeg", actualContent.getMimeType(), "mimetype mismatch")
                , () -> assertEquals("84AE445BED7F0913249DFA8B167234C57AE321CB", actualContent.getHash(), "hash mismatch")
                , () -> assertEquals(expectedSize, actualContent.getSize(), "size mismatch")
                , () -> assertArrayEquals(expectedBytes, actualBytes, "bytes mismatch")
        );
    }
}