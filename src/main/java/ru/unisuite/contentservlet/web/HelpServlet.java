package ru.unisuite.contentservlet.web;

import org.apache.commons.io.IOUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@WebServlet("/help")
public class HelpServlet extends HttpServlet {
    private static final String HELP_FILE = "/help.txt";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        respondWithHelp(response);
    }

    private void respondWithHelp(HttpServletResponse response) throws IOException {
        try (InputStream is = getServletContext().getResourceAsStream(HELP_FILE);
             OutputStream out = response.getOutputStream()) {
            IOUtils.copy(is, out);
        }
	}
}
