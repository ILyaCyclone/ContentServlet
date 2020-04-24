//package ru.unisuite.contentservlet.service;
//
//import org.apache.commons.io.IOUtils;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import ru.unisuite.contentservlet.config.ApplicationConfig;
//import ru.unisuite.contentservlet.config.ResizerType;
//import ru.unisuite.contentservlet.model.Content;
//
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class ContentServiceImplTest {
//
//    ApplicationConfig applicationConfig = new ApplicationConfig();
//
//    ContentService contentService;
//
//    @BeforeEach
//    void init() {
//        this.contentService = new ContentServiceImpl(applicationConfig.contentRepository(), ResizerType.THUMBNAILATOR, false, null);
//
//    }
//
////    @Test
////    void s() {
////        new MockHttpRequest
////        contentService.getObject();
////    }
//
//    @Test
//    void getContentByIdWebMetaterm() throws IOException {
//        Content content = contentService.getContentByIdWebMetaterm(842025, 1200, 300, (byte) 80);
//        System.out.println("content.getFilename() = " + content.getFilename());
//
//        byte[] bytes = IOUtils.toByteArray(content.getDataStream());
//        int bytesRead = bytes.length;
//        System.out.println("bytes.length = " + bytesRead);
//
//
//        Assertions.assertAll(
//                () -> assertTrue(bytesRead > 0, "bytes read should be > 0")
//                , () -> assertEquals("Фото1", content.getFilename())
//                , () -> assertEquals("jpg", content.getExtension())
//                , () -> assertEquals("image/jpeg", content.getMimeType())
//        );
//    }
//}