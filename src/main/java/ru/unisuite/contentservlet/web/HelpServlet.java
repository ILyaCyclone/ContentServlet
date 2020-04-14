package ru.unisuite.contentservlet.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet("/help")
public class HelpServlet extends HttpServlet {
    private static final String HELP_FILE = "/help.txt";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        respondWithHelp(response);
    }

    private void respondWithHelp(HttpServletResponse response) throws IOException {

        try (InputStream is = getServletContext().getResourceAsStream(HELP_FILE);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isr);
             PrintWriter writer = response.getWriter()) {
            String text;
            while ((text = reader.readLine()) != null) {
                writer.println(text);
            }
        }
	}
}
