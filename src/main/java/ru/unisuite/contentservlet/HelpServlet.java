package ru.unisuite.contentservlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;

@WebServlet(name = "HelpServlet", urlPatterns = { "/help" })
public class HelpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	static final String HELP_FILE = "/help.txt";

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		respondWithHelp(request, response);
	}

	private void respondWithHelp(HttpServletRequest request, HttpServletResponse response) throws IOException {

		try (InputStream inputStream = getServletContext().getResourceAsStream(HELP_FILE);
				OutputStream output = response.getOutputStream()) {
			ByteStreams.copy(inputStream, output);
			output.flush();
		}
	}
}
