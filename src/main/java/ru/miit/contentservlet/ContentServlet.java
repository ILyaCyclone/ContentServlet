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
import ru.miit.cacheexception.CacheGetException;
import ru.miit.cacheexception.CacheMetadataStoreConnectionException;
import ru.miit.cacheexception.CacheStartFailedException;
import ru.miit.databasereader.OracleDatabaseReaderException;

@WebServlet("/ContentServlet")
public class ContentServlet extends HttpServlet {

	public CacheInstance cacheInstance;

	private static final long serialVersionUID = 1L;

	private final Logger logger = ContentLogger.getLogger(ContentServlet.class.getName());

	private final static String contentTypeHTML = "text/html; charset=UTF-8";
	private final static String ContentDispositionText = "Content-Disposition";

	public static boolean cacheIsUp = false;

	public void init() {

//		ContentLogger.initLogManager();

		try {
			cacheInstance = new CacheInstance("C:\\Users\\romanov\\Desktop\\cache\\cacheConfig.xml");
			
			cacheIsUp = true;

		} catch (CacheStartFailedException e) {
			logger.log(Level.SEVERE, "Cache didn't start. " + e.toString());
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {

		Cache cache = null;
		
		try {
			cache = cacheInstance.getCache(); // Нужно ли коллекционировать их и заккрывать их всех в destroy?

			long downtime = 0L; // берется из бд
			cache.applyDowntine(downtime);

			// Инициализация класса со значениями всех параметров
			RequestParameters requestParameters = null;
			try {
				requestParameters = new RequestParameters(request.getParameterMap());
			} catch (NumberFormatException e) {

				logger.log(Level.SEVERE, "Request parameters didn't initialised. " + e.toString());
				try {
					response.sendError(404);
				} catch (IOException e1) {
					logger.log(Level.SEVERE, "Error did not show to client. " + e1.toString());
				}
			}

			ContentGetter contentGetter = new ContentGetter();

			// Задание Header
			String respHeader = contentGetter.getHeader(request, requestParameters.getContentDisposition());
			response.setHeader(ContentDispositionText, respHeader);

			if (requestParameters.getContentType() == null) {
				requestParameters.contentType = -1; // Временно, чтобы работало
			}

			switch (requestParameters.getContentType()) {
			case 1: {

				response.setContentType(contentTypeHTML);

				String codeData = null;

				try (PrintWriter printWriter = response.getWriter()) {

					printWriter.println("<html><body>");
					try {

						codeData = contentGetter.getCodeData(requestParameters.getWebMetaId());

						request.setAttribute("codeData", codeData);

						printWriter.println("<p>" + codeData + "</p>");

					} catch (OracleDatabaseReaderException e) {

						printWriter.println("<h3>Error</h3>");
						printWriter.println("<p>" + e.getMessage() + "</p>");
						logger.log(Level.SEVERE, "CodeData wasn't fetched. " + e.toString());

					} finally {
						printWriter.println("</body></html>");
					}
				} catch (IOException e) {
					logger.log(Level.SEVERE, "PrintWriter did not created. " + e.toString());
				}

				break;
			}
			case 777: {

				response.setContentType(contentTypeHTML);
				try (PrintWriter printWriter = response.getWriter()) {

					printWriter.println("<html><body>");
					try {

						contentGetter.getResTestListData(printWriter);

					} catch (OracleDatabaseReaderException e) {

						printWriter.println("<h3>Error</h3>");
						printWriter.println("<p>" + e.getMessage() + "</p>");
						logger.log(Level.SEVERE, "ListData wasn't fetched. " + e.toString());

					} finally {
						printWriter.println("</body></html>");
					}
				} catch (IOException e) {
					logger.log(Level.SEVERE, "PrintWriter did not created. " + e.toString());
				}
				break;
			}
			default: {

				try (OutputStream os = response.getOutputStream()) {

					contentGetter.getObject(requestParameters, os, response, cache);

					if (cacheIsUp) {

						CacheStatist statist = cache.getStatistics();

						System.out.println("cacheHits: " + statist.getCacheHits() + " cacheMisses: "
								+ statist.getCacheMisses() + " Ratio: " + statist.getCacheHitRatio());
					}

				} catch (CacheGetException | OracleDatabaseReaderException | IOException e) {

					logger.log(Level.SEVERE, "Object getting is failed. " + e.toString());
					e.printStackTrace();
					try {
						response.sendError(404);
					} catch (IOException e1) {
						logger.log(Level.SEVERE, "Error did not show to client. " + e1.toString());
					}

				}
			}
			}
		} catch (CacheMetadataStoreConnectionException e) {
			logger.log(Level.SEVERE, "Metadatastore connection was not opened. " + e.toString());
			e.printStackTrace();
			
			try {
				response.sendError(404);
			} catch (IOException e1) {
				logger.log(Level.SEVERE, "Error did not show to client. " + e1.toString());
			}
		}
		finally {
			if (cache != null)
				cache.close();
		}
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
