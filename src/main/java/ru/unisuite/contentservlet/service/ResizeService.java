package ru.unisuite.contentservlet.service;

import ru.unisuite.contentservlet.model.Content;

import java.io.IOException;
import java.io.OutputStream;

public interface ResizeService {
    void writeResized(ContentRequest contentRequest, Content content, OutputStream out) throws IOException;
}
