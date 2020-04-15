package ru.unisuite.contentservlet.web;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.contentservlet.repository.DatabaseReaderException;
import ru.unisuite.contentservlet.service.ContentRequest;
import ru.unisuite.contentservlet.service.ContentService;
import ru.unisuite.contentservlet.service.ResizeServiceImpl;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/*")
//@WebServlet({ "/get/*", "/get/secure/*" })
public class ContentServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(ContentServlet.class.getName());

    private ContentService contentService;
    private RequestMapper requestMapper;

    private ResizeServiceImpl resizeService;
    private ResizerType resizerType;


    private String httpCacheControlDefaultValue;

    private boolean persistentCacheEnabled;
    private CacheFactory cacheFactory;
    private Cache persistentCache;


    private static final ZoneId GMT = ZoneId.of("GMT");
    private static final DateTimeFormatter LAST_MODIFIED_FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(GMT);

    private final static String contentTypeHTML = "text/html; charset=UTF-8";
    private final static String contentDispositionText = "Content-Disposition";
    private final static String cacheControlHeaderName = "Cache-Control";

    private final static String CACHE_CONFIG_FILE_NAME = "cache-config.xml";

    @Override
    public void init() {
        ApplicationConfig applicationConfig = new ApplicationConfig();

        this.contentService = applicationConfig.contentService();
        this.requestMapper = new RequestMapper();

        this.resizeService = applicationConfig.resizeService();
        this.resizerType = applicationConfig.getResizerType();

        this.httpCacheControlDefaultValue = applicationConfig.getCacheControl();

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("request parameters {}", request.getParameterMap().toString());
        // Инициализация класса со значениями всех параметров
        ContentRequest contentRequest;
        try {
            contentRequest = requestMapper.mapHttpServletRequest(request);
            logger.debug(contentRequest.toString());
        } catch (Exception e) {
            logger.error("Could not map HttpRequest "+request.getParameterMap().toString()+" to ContentRequest", e);
            try {
//                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                String html = "<p>Некорректный запрос</p>" +
                        "<p>Пожалуйста, обратитесь к документации на странице <a href=\"help\">/help</a></p>";
                replyWithHtml(response, html, HttpServletResponse.SC_BAD_REQUEST);
                return;
            } catch (IOException e1) {
                logger.error("Error did not show to client. " + e1.toString(), e);
            }
            return;
        }

        if (contentRequest.isEmpty()) {
            serveHelp(request, response);
            return;
        }

        // Задание Header
        response.setHeader(contentDispositionText, getContentDisposition(contentRequest));

        String cacheControl = getHttpCacheControl(contentRequest.getNoCache());
        response.setHeader(cacheControlHeaderName, cacheControl);

        // Временно чтобы работало
        Integer contentType = contentRequest.getContentType();
        if (contentType == null) {
            contentType = -1;
        }

        switch (contentType) {
            case 1:
                serveContentTypeHtml(response, contentRequest);
                break;
            default:
                serveContent(response, contentRequest);
        }
    }


    private void serveHelp(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getServletContext().getRequestDispatcher("/help").forward(request, response);
        } catch (ServletException | IOException e) {
            logger.error("Servlet can't show /help page. " + e.getMessage(), e);
        }
    }

    private void serveContent(HttpServletResponse response, ContentRequest contentRequest) throws IOException {
        if(contentRequest.getWebMetaId() != null) {
            Content content = contentService.getContentByIdWebMetaterm(contentRequest.getWebMetaId()
                    , contentRequest.getWidth(), contentRequest.getHeight(), contentRequest.getQuality());

            response.setContentType(content.getMimeType());

//            response.setContentLengthLong(content.getSize()); // the internet says content-length is managed automatically
            response.setHeader("Last-Modified", getHttpLastModified(content));
            // already set
//            response.setHeader(contentDispositionText, getContentDisposition(contentRequest));
//            String cacheControl = getHttpCacheControl(contentRequest.getNoCache());
//            response.setHeader(cacheControlHeaderName, cacheControl);

            resizeService.resize(content, response.getOutputStream(), contentRequest);
//            IOUtils.copy(content.getDataStream(), response.getOutputStream());
//            content.getDataStream().close();
            return;
        }
        serveContent_old(response, contentRequest);
    }

    private String getHttpLastModified(Content content) {
        Instant instant = Instant.ofEpochSecond(content.getLastModified());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, GMT);
        return LAST_MODIFIED_FORMATTER.format(localDateTime);
    }

    private void serveContent_old(HttpServletResponse response, ContentRequest contentRequest) {
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
        } catch (NotFoundException e) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e1) {
                logger.warn(e1.toString(), e1);
            }

            logger.error(e.toString(), e);
        }
    }

    private void serveContentTypeHtml(HttpServletResponse response, ContentRequest contentRequest) {
        try (PrintWriter printWriter = response.getWriter()) {

            try {
                String htmlImgCode = contentService.getHtmlImgCode(contentRequest.getWebMetaId());
                response.setContentType(contentTypeHTML);
                printWriter.println(htmlImgCode);
            } catch (DatabaseReaderException e) {

                printWriter.println("<h3>Error</h3>");
                printWriter.println("<p>" + e.getMessage() + "</p>");
                logger.error("CodeData wasn't fetched. " + e.toString(), e);

            }
        } catch (IOException e) {
            logger.error("PrintWriter did not created. " + e.toString(), e);
        }
    }

    @Override
    public void destroy() {
        if (cacheFactory != null)
            cacheFactory.close();
    }

    private String getHttpCacheControl(Boolean noCacheRequested) {
        return Boolean.TRUE.equals(noCacheRequested) ? "no-cache" : httpCacheControlDefaultValue;
    }

    private String getContentDisposition(ContentRequest contentRequest) {
        String fileName = contentRequest.getFilename();

        if (contentRequest.getContentDisposition() == null) {
            return "filename=" + fileName;
        } else {

            switch (contentRequest.getContentDisposition()) {
                case 1:
                    return "attachment; filename=" + fileName;
                case 2:
                    return "inline; filename=" + fileName;
                default:
                    return "filename=" + fileName;
            }
        }

    }



    private void replyWithHtml(HttpServletResponse response, String html, int statusCode) throws IOException {
        response.setContentType(contentTypeHTML);
        response.setStatus(statusCode);
        response.getWriter().println(html);
    }
//    private void replyWithHtml(PrintWriter printWriter, String html, int statusCode) throws IOException {
//        response.setContentType(contentTypeHTML);
//        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//        response.getWriter().println("<p>Некорректный запрос</p>" +
//                "<p>Пожалуйста, обратитесь к документации на странице <a href=\"help\">/help</a></p>");
//    }
}
