package ru.unisuite.contentservlet;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

@WebFilter(urlPatterns = "/*")
public class ContentDispatcherFilter implements Filter {

	private static final String LOGIN_URL = "login";
	private static final String CONTENT_URL = "get";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		String path = req.getRequestURI().substring(req.getContextPath().length());
		String pathStart = path.split("/")[1];

		switch (pathStart) {
		case (LOGIN_URL):
			chain.doFilter(request, response);
			break;
		default:
			request.getRequestDispatcher("/" + CONTENT_URL + path).forward(request, response);
			return;
		}

	}

	@Override
	public void destroy() {
	}

}
