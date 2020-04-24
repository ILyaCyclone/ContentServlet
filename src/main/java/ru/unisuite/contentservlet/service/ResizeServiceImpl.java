package ru.unisuite.contentservlet.service;

import org.apache.commons.io.IOUtils;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.imageresizer.ImageResizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResizeServiceImpl {
    private final ImageResizer imageResizer;
    private final byte defaultQuality;

    public ResizeServiceImpl(ImageResizer imageResizer, byte defaultQuality) {
        this.imageResizer = imageResizer;
        this.defaultQuality = defaultQuality;
    }

    public void writeResized(ContentRequest contentRequest, Content content, OutputStream out) throws IOException {
        int quality = (contentRequest.getQuality() != null ? contentRequest.getQuality().intValue() : defaultQuality);
        InputStream in = content.getDataStream();
        if(contentRequest.getWidth() != null && contentRequest.getHeight() != null) {
            imageResizer.resize(in, contentRequest.getWidth(), contentRequest.getHeight(), out, quality);
            return;
        }
        if(contentRequest.getWidth() != null) {
            imageResizer.resizeByWidth(in, contentRequest.getWidth(), out, quality);
            return;
        }
        if(contentRequest.getHeight() != null) {
            imageResizer.resizeByHeight(in, contentRequest.getHeight(), out, quality);
            return;
        }
        IOUtils.copy(in, out);
    }
}
