package ru.unisuite.contentservlet.web;

import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.config.ContentServletProperties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ConfigServlet extends HttpServlet {

    private final String configOutput;

    public ConfigServlet(ApplicationConfig applicationConfig, ContentServletProperties contentServletProperties) {
//        String output = contentServletProperties.toString().replaceAll("=(.+?),", "=$1,\n");
        String output = contentServletProperties.toString();
        output += "\nImageProcessors{";
        output += applicationConfig.getImageProcessors();
        output += "}";

        configOutput = output;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OutputStream out = response.getOutputStream();
        out.write(configOutput.getBytes(StandardCharsets.UTF_8));
    }
}
