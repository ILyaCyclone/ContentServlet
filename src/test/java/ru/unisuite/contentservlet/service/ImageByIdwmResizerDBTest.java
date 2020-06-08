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
        String expectedFilename = "idwm-842025_rt-db_q-80.jpg";

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdWebMetaterm(842025L);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content content = contentService.getContent(contentRequest);

        byte[] expectedBytes = expectedBytes(expectedFilename);
        byte[] actualBytes = IOUtils.toByteArray(content.getDataStream());
        Assertions.assertAll(
                () -> assertEquals("Фото1", content.getFilename())
                , () -> assertEquals("jpg", content.getExtension())
                , () -> assertEquals("image/jpeg", content.getMimeType())
                , () -> assertArrayEquals(expectedBytes, actualBytes, "bytes mismatch")
        );
    }

    @Test
    void resizeByWidth() throws IOException {
        String expectedFilename = "idwm-842025_w-500_rt-db_q-80.jpg";

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdWebMetaterm(842025L);
        contentRequest.setWidth(500);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content content = contentService.getContent(contentRequest);

        byte[] expectedBytes = expectedBytes(expectedFilename);
        byte[] actualBytes = IOUtils.toByteArray(content.getDataStream());
        Assertions.assertAll(
                () -> assertEquals("Фото1", content.getFilename())
                , () -> assertEquals("jpg", content.getExtension())
                , () -> assertEquals("image/jpeg", content.getMimeType())
                , () -> assertArrayEquals(expectedBytes, actualBytes, "bytes mismatch")
        );
    }

    @Test
    void resizeByHeight() throws IOException {
        String expectedFilename = "idwm-842025_h-500_rt-db_q-80.jpg";

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdWebMetaterm(842025L);
        contentRequest.setHeight(500);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content content = contentService.getContent(contentRequest);

        byte[] expectedBytes = expectedBytes(expectedFilename);
        byte[] actualBytes = IOUtils.toByteArray(content.getDataStream());
        Assertions.assertAll(
                () -> assertEquals("Фото1", content.getFilename())
                , () -> assertEquals("jpg", content.getExtension())
                , () -> assertEquals("image/jpeg", content.getMimeType())
                , () -> assertArrayEquals(expectedBytes, actualBytes, "bytes mismatch")
        );
    }

    @Test
    void resizeByWithAndHeight() throws IOException {
        String expectedFilename = "idwm-842025_w-600_h-300_rt-db_q-80.jpg";

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setIdWebMetaterm(842025L);
        contentRequest.setHeight(600);
        contentRequest.setHeight(300);
        contentRequest.setResizerType(ResizerType.DB);
        contentRequest.setQuality((byte) 80);
        Content content = contentService.getContent(contentRequest);

        byte[] expectedBytes = expectedBytes(expectedFilename);
        byte[] actualBytes = IOUtils.toByteArray(content.getDataStream());
        Assertions.assertAll(
                () -> assertEquals("Фото1", content.getFilename())
                , () -> assertEquals("jpg", content.getExtension())
                , () -> assertEquals("image/jpeg", content.getMimeType())
                , () -> assertArrayEquals(expectedBytes, actualBytes, "bytes mismatch")
        );
    }

    private byte[] expectedBytes(String expectedFilename) throws IOException {
        return super.getExpectedFileBytes(EXPECTED_FOLDER + File.separator + expectedFilename);
    }
}