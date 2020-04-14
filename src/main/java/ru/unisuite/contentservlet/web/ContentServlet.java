package ru.unisuite.contentservlet.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.repository.DatabaseReaderException;
import ru.unisuite.contentservlet.service.ContentRequest;
import ru.unisuite.contentservlet.service.ContentService;
import ru.unisuite.scf4j.Cache;
import ru.unisuite.scf4j.CacheFactory;
import ru.unisuite.scf4j.exception.SCF4JCacheGetException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/*")
//@WebServlet({ "/get/*", "/get/secure/*" })
public class ContentServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(ContentServlet.class.getName());

    private ContentService contentService;
    private RequestMapper requestMapper;

    private String httpCacheControlDefaultValue;

    private boolean persistentCacheEnabled;
    private CacheFactory cacheFactory;
    private Cache persistentCache;

    private static final long serialVersionUID = 1L;



    private final static String contentTypeHTML = "text/html; charset=UTF-8";
    private final static String contentDispositionText = "Content-Disposition";
    private final static String cacheControlHeaderName = "Cache-Control";

    private final static String CACHE_CONFIG_FILE_NAME = "cache-config.xml";

    @Override
    public void init() {
        ApplicationConfig applicationConfig = new ApplicationConfig();

        this.contentService = applicationConfig.contentService();
        this.requestMapper = new RequestMapper();

        this.persistentCacheEnabled = applicationConfig.isPersistentCacheEnabled();

//		if (persistentCacheEnabled) {
//			try {
//				cacheFactory = GeneralCacheFactory.getCacheFactory(this.getClass().getClassLoader().getResource(CACHE_CONFIG_FILE_NAME).getPath());
//			} catch (SCF4JCacheStartFailedException e) {
//				throw new RuntimeException("Problems with Cache config file. " + e.toString(), e);
//			}
//			persistentCache = cacheFactory.getCache();
//		}
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // Инициализация класса со значениями всех параметров
        ContentRequest contentRequest;
        try {
            contentRequest = requestMapper.mapHttpServletRequest(request);
            logger.debug(contentRequest.toString());
        } catch (Exception e) {

            logger.error("Request parameters didn't initialised. " + e.toString(), e);
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException e1) {
                logger.error("Error did not show to client. " + e1.toString(), e);
            }
            return;
        }

        if (contentRequest.isEmpty()) {
            try {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.getServletContext().getRequestDispatcher("/help").forward(request, response);
            } catch (ServletException | IOException e) {
                logger.error("Servlet can't show /help page. " + e.getMessage(), e);
            }
            return;
        }

        // Задание Header
        String respHeader = getContentDisposition(request, contentRequest.getContentDisposition());
        response.setHeader(contentDispositionText, respHeader);

        String cacheControl = getHTTPCacheControl(contentRequest.getNoCache());
        response.setHeader(cacheControlHeaderName, cacheControl);

        // Временно чтобы работало
        Integer contentType = contentRequest.getContentType();
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

                        codeData = contentService.getCodeData(contentRequest.getWebMetaId());

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

            default: {

                try (OutputStream os = response.getOutputStream()) {

                    contentService.getObject(contentRequest, os, response, persistentCache);

                    if (persistentCacheEnabled) {
                        if (persistentCache.connectionIsUp()) {

                            System.out.println(persistentCache.getStatistics());
                        }
                    }
                } catch (SCF4JCacheGetException | DatabaseReaderException | IOException e) {
                    logger.error("Exception for contentType default", e);

                    try {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (IOException e1) {
                        logger.error(e1.toString(), e1);
                    }

                    logger.error("Object getting is failed. " + e.toString(), e);
                    return;
                } catch (NotFoundException e) {
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

    private String getHTTPCacheControl(Boolean noCacheRequested) {
        return Boolean.TRUE.equals(noCacheRequested) ? "no-cache" : httpCacheControlDefaultValue;
    }

    private String getContentDisposition(final HttpServletRequest request, final Integer contentDisposition) {
        String fileName = getFileName(request.getRequestURI());

        if (contentDisposition == null) {
            return "filename=" + fileName;
        } else {

            switch (contentDisposition) {
                case 1:
                    return "attachment; filename=" + fileName;
                case 2:
                    return "inline; filename=" + fileName;
                default:
                    return "filename=" + fileName;
            }
        }

    }

    private final Pattern filenamePattern = Pattern.compile("[^/]*[^/]");

    private String getFileName(String uri) {
        Matcher matcher = filenamePattern.matcher(uri);
        String rS = null;
        while (matcher.find()) {
            rS = matcher.group(matcher.groupCount());
        }
        return rS;
    }
}
