package ru.unisuite.contentservlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "HelpServlet", urlPatterns = { "/help" })
public class HelpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	static final String HELP_FILE = "/help.txt";

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		respondWithHelp(request, response);
	}

	private void respondWithHelp(HttpServletRequest request, HttpServletResponse response) throws IOException {

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
