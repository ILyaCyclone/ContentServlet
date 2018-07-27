package ru.miit.contentservlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.miit.cache.Cache;
import ru.miit.cache.CacheInstance;
import ru.miit.cache.CacheStatist;
import ru.miit.cache.cacheexception.CacheGetException;
import ru.miit.cache.cacheexception.CacheStartFailedException;
import ru.miit.contentservlet.databasereader.DatabaseReaderException;
import ru.miit.contentservlet.databasereader.DatabaseReaderNoDataException;

@WebServlet({"/*", "/secure/*"})
public class ContentServlet extends HttpServlet {

	private ContentGetter contentGetter = new ContentGetter();
	private CacheInstance cacheInstance;

	private static final long serialVersionUID = 1L;

	public static boolean USE_CACHE;

	private Logger loggerContentServlet;

	private final static String contentTypeHTML = "text/html; charset=UTF-8";
	private final static String ContentDispositionText = "Content-Disposition";

	public void init() {
		
		ContentServletProperties contentServletProperties = null;
		try {
			contentServletProperties = new ContentServletProperties();
		} catch (ContentServletPropertiesException e) {
			throw new RuntimeException("Problems with ContentServlet config file. " + e.toString(), e);
		}

		USE_CACHE = contentServletProperties.isUseCache();

		ContentLogger.initLogManager(contentServletProperties);
		loggerContentServlet = ContentLogger.getLogger(ContentServlet.class.getName());

		if (USE_CACHE) {
			try {
				cacheInstance = new CacheInstance("C:\\Users\\romanov\\Desktop\\cache\\cacheConfig.xml");
			} catch (CacheStartFailedException e) {
				loggerContentServlet.log(Level.SEVERE, "Cache didn't start. " + e.toString());
			}
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {

		Cache cache = null;
		if (USE_CACHE) {
			cache = cacheInstance.getCache();
			long downtime = 0L; // берется из бд
			if (cache.isUp)
				cache.applyDowntine(downtime);
		}
		
		// Инициализация класса со значениями всех параметров
		RequestParameters requestParameters = null;
		try {
			requestParameters = new RequestParameters(request.getParameterMap());
		} catch (NumberFormatException e) {

			loggerContentServlet.log(Level.SEVERE, "Request parameters didn't initialised. " + e.toString());
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (IOException e1) {
				loggerContentServlet.log(Level.SEVERE, "Error did not show to client. " + e1.toString());
			}
			return;
		}

		// Задание Header
		String respHeader = contentGetter.getHeader(request, requestParameters.getContentDisposition());
		response.setHeader(ContentDispositionText, respHeader);

		// Временно чтобы работало
		Integer contentType = requestParameters.getContentType();
		if (contentType == null) {
			contentType = -1; 
		}

		switch (contentType) {
		case 1: {

			response.setContentType(contentTypeHTML);

			String codeData = null;

			try (PrintWriter printWriter = response.getWriter()) {

				printWriter.println("<html><body>");
				try {

					codeData = contentGetter.getCodeData(requestParameters.getWebMetaId());

					request.setAttribute("codeData", codeData);

					printWriter.println("<p>" + codeData + "</p>");

				} catch (DatabaseReaderException e) {

					printWriter.println("<h3>Error</h3>");
					printWriter.println("<p>" + e.getMessage() + "</p>");
					loggerContentServlet.log(Level.SEVERE, "CodeData wasn't fetched. " + e.toString());

				} finally {
					printWriter.println("</body></html>");
				}
			} catch (IOException e) {
				loggerContentServlet.log(Level.SEVERE, "PrintWriter did not created. " + e.toString());
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
					loggerContentServlet.log(Level.SEVERE, "ListData wasn't fetched. " + e.toString());

				} finally {
					printWriter.println("</body></html>");
				}
			} catch (IOException e) {
				loggerContentServlet.log(Level.SEVERE, "PrintWriter did not created. " + e.toString());
			}
			break;
		}
		default: {

			try (OutputStream os = response.getOutputStream()) {

				contentGetter.getObject(requestParameters, os, response, cache);

				if (USE_CACHE) {
					if (cache.isUp) {

						CacheStatist statist = cache.getStatistics();

						System.out.println("cacheHits: " + statist.getCacheHits() + " cacheMisses: "
								+ statist.getCacheMisses() + " Ratio: " + statist.getCacheHitRatio());
					}
				}
			} catch (CacheGetException | DatabaseReaderException | IOException e) {
				loggerContentServlet.log(Level.SEVERE, "Object getting is failed. " + e.toString());
			} catch (DatabaseReaderNoDataException e) {
				
				response.setStatus(HttpServletResponse.SC_NOT_FOUND); //Не работает, потом уточнить как лучше поступить. Возможно, стоит передвать response в getObject().
				
				loggerContentServlet.log(Level.SEVERE, e.toString());
				return;
			}
		}
		}
		
//		cache.shutdown();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		if (cacheInstance != null)
			cacheInstance.close();
	}

}
