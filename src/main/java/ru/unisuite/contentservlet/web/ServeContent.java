package ru.unisuite.contentservlet.web;

import ru.unisuite.contentservlet.service.ContentRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ServeContent {
    void serveContentRequest(ContentRequest contentRequest, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
