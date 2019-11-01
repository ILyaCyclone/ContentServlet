package ru.unisuite.contentservlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.unisuite.contentservlet.databasereader.DatabaseReaderException;
import ru.unisuite.contentservlet.databasereader.DatabaseReaderNoDataException;
import ru.unisuite.scf4j.Cache;
import ru.unisuite.scf4j.CacheFactory;
import ru.unisuite.scf4j.GeneralCacheFactory;
import ru.unisuite.scf4j.exception.SCF4JCacheGetException;
import ru.unisuite.scf4j.exception.SCF4JCacheStartFailedException;

@WebServlet("/*")//@WebServlet({ "/get/*", "/get/secure/*" })
public class ContentServlet extends HttpServlet {

	private ContentGetter contentGetter;
	private CacheFactory cacheFactory;
	private Cache persistantCache;

	private static final long serialVersionUID = 1L;

	public static boolean USE_CACHE;

	private Logger logger = LoggerFactory.getLogger(ContentServlet.class.getName());

	private final static String contentTypeHTML = "text/html; charset=UTF-8";
	private final static String contentDispositionText = "Content-Disposition";
	private final static String cacheControlHeaderName = "Cache-Control";
	
	private final static String CACHE_CONFIG_FILE_NAME = "cache-config.xml";
	
	private int defaultQuality;

	public void init() {

		ContentServletProperties contentServletProperties;
		try {
			contentServletProperties = new ContentServletProperties();
		} catch (ContentServletPropertiesException e) {
			throw new RuntimeException("Problems with ContentServlet config file. " + e.toString(), e);
		}

		contentGetter = new ContentGetter(contentServletProperties);
		
		try {
			defaultQuality = contentGetter.getDefaultImageQuality();
		} catch (DatabaseReaderException | DatabaseReaderNoDataException e1) {
			throw new RuntimeException("Can't read default image quality. " + e1.toString(), e1);
		}

		USE_CACHE = contentServletProperties.isUseCache();
		
		if (USE_CACHE) {
			try {
				cacheFactory = GeneralCacheFactory.getCacheFactory(this.getClass().getClassLoader().getResource(CACHE_CONFIG_FILE_NAME).getPath());
			} catch (SCF4JCacheStartFailedException e) {
				throw new RuntimeException("Problems with Cache config file. " + e.toString(), e);
			}
			persistantCache = cacheFactory.getCache();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		// Инициализация класса со значениями всех параметров
		RequestParameters requestParameters;
		try {
			requestParameters = new RequestParameters(request.getParameterMap(), defaultQuality);
			logger.debug("HTTP request: " + requestParameters.toString());
		} catch (NumberFormatException e) {

			logger.error("Request parameters didn't initialised. " + e.toString(), e);
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (IOException e1) {
				logger.error("Error did not show to client. " + e1.toString(), e);
			}
			return;
		}
		
		if (requestParameters.isEmpty()) {
			try {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				request.getServletContext().getRequestDispatcher("/help").forward(request, response);
			} catch (ServletException | IOException e) {
				logger.error("Servlet can't show /help page. " + e.getMessage(), e);
			}
			return;
		}

		// Задание Header
		String respHeader = contentGetter.getContentDisposition(request, requestParameters.getContentDisposition());
		response.setHeader(contentDispositionText, respHeader);
		
		String cacheControl = contentGetter.getCacheControl(requestParameters.getCacheControl());
		response.setHeader(cacheControlHeaderName, cacheControl);

		// Временно чтобы работало
		Integer contentType = requestParameters.getContentType();
		if (contentType == null) {
			contentType = -1;
		}

		switch (contentType) {
		case 1: {

			response.setContentType(contentTypeHTML);

			String codeData;

			try (PrintWriter printWriter = response.getWriter()) {

				printWriter.println("<html><body>");
				try {

					codeData = contentGetter.getCodeData(requestParameters.getWebMetaId());

					request.setAttribute("codeData", codeData);

					printWriter.println("<p>" + codeData + "</p>");

				} catch (DatabaseReaderException e) {

					printWriter.println("<h3>Error</h3>");
					printWriter.println("<p>" + e.getMessage() + "</p>");
					logger.error("CodeData wasn't fetched. " + e.toString(), e);

				} finally {
					printWriter.println("</body></html>");
				}
			} catch (IOException e) {
				logger.error("PrintWriter did not created. " + e.toString(), e);
			}

			break;
		}
		case 777: {

			response.setContentType(contentTypeHTML);
			try (PrintWriter printWriter = response.getWriter()) {

				printWriter.println("<html><body>");
				try {

					contentGetter.getResTestListData(printWriter);

				} catch (DatabaseReaderException e) {

					printWriter.println("<h3>Error</h3>");
					printWriter.println("<p>" + e.getMessage() + "</p>");
					logger.error("ListData wasn't fetched. " + e.toString(), e);

				} finally {
					printWriter.println("</body></html>");
				}
			} catch (IOException e) {
				logger.error("PrintWriter did not created. " + e.toString(), e);
			}
			break;
		}
		default: {

			try (OutputStream os = response.getOutputStream()) {

				contentGetter.getObject(requestParameters, os, response, persistantCache);

				if (USE_CACHE) {
					if (persistantCache.connectionIsUp()) {

						System.out.println(persistantCache.getStatistics());
					}
				}
			} catch (SCF4JCacheGetException | DatabaseReaderException | IOException e) {

				try {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (IOException e1) {
					logger.error(e1.toString(), e1);
				}

				logger.error("Object getting is failed. " + e.toString(), e);

				return;

			} catch (DatabaseReaderNoDataException e) {

				try {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				} catch (IOException e1) {
					logger.warn(e1.toString(), e1);
				}

				logger.error(e.toString(), e);
				return;
			}
		}
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);
	}

	public void destroy() {
		if (cacheFactory != null)
			cacheFactory.close();
	}

}
