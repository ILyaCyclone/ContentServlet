package ru.unisuite.contentservlet.web;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.contentservlet.service.ContentRequest;
import ru.unisuite.contentservlet.service.ContentService;
import ru.unisuite.contentservlet.service.ResizeServiceImpl;
import ru.unisuite.scf4j.Cache;
import ru.unisuite.scf4j.CacheFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@WebServlet("/*")
//@WebServlet({ "/get/*", "/get/secure/*" })
public class ContentServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(ContentServlet.class.getName());

    private ContentService contentService;
    private RequestMapper requestMapper;

    private ResizeServiceImpl resizeService;
    private ResizerType defaultResizerType;


    private String httpCacheControlDefaultValue;

    private boolean persistentCacheEnabled;
    private CacheFactory cacheFactory;
    private Cache persistentCache;


    private static final ZoneId GMT = ZoneId.of("GMT");
    private static final DateTimeFormatter LAST_MODIFIED_FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(GMT);

    private final static String contentTypeHTML = "text/html; charset=UTF-8";
    private final static String contentDispositionHeaderName = "Content-Disposition";
    private final static String cacheControlHeaderName = "Cache-Control";

    private final static String CACHE_CONFIG_FILE_NAME = "cache-config.xml";

    @Override
    public void init() {
        ApplicationConfig applicationConfig = new ApplicationConfig();

        this.contentService = applicationConfig.contentService();
        this.requestMapper = new RequestMapper();

        this.resizeService = applicationConfig.resizeService();
        this.defaultResizerType = applicationConfig.getResizerType();

        this.httpCacheControlDefaultValue = applicationConfig.getCacheControl();

//        this.persistentCacheEnabled = applicationConfig.isPersistentCacheEnabled();
//
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
        if(logger.isTraceEnabled()) logger.trace("request parameters {}", request.getParameterMap().toString());

        String ifModifiedSince = request.getHeader("If-Modified-Since");
        if(ifModifiedSince != null) {
            System.out.println("ifModifiedSince = " + ifModifiedSince);
        }
        String ifNoneMatch = request.getHeader("If-None-Match");
        if(ifNoneMatch != null) {
            System.out.println("ifNoneMatch = " + ifNoneMatch);
        }

        ContentRequest contentRequest;
        try {
            contentRequest = requestMapper.mapHttpServletRequest(request);
            logger.debug(contentRequest.toString());
        } catch (Exception e) {
            logger.error("Could not map HttpRequest " + request.getParameterMap().toString() + " to ContentRequest", e);
//                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            String html = "<p>Некорректный запрос</p>" +
                    "<p>Пожалуйста, обратитесь к документации на странице <a href=\"help\">/help</a></p>";
            replyWithHtml(response, html, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (contentRequest.isEmpty()) {
            serveHelp(request, response);
            return;
        }

        serveContent(contentRequest, response);
    }



    private void serveContent(ContentRequest contentRequest, HttpServletResponse response) throws IOException {

        Content content;
        try {
            content = contentService.getContent(contentRequest);
        } catch (NotFoundException nfe) {
            replyWithHtml(response, "Запрошенный контент не найден", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setHeader(contentDispositionHeaderName, getHttpContentDisposition(contentRequest));
        response.setHeader(cacheControlHeaderName, getHttpCacheControl(contentRequest));
        response.setHeader("Last-Modified", getHttpLastModified(content));
        response.setHeader("ETag", String.valueOf(content.getLastModified()));
        response.setContentType(content.getMimeType());
        // content-length is not needed

        ResizerType resizerType = contentRequest.getResizerType() != null ? contentRequest.getResizerType() : defaultResizerType;
        switch (resizerType) {
            case DB:
                IOUtils.copy(content.getDataStream(), response.getOutputStream());
                break;
            case THUMBNAILATOR:
                resizeService.writeResized(contentRequest, content, response.getOutputStream());
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

    private String getHttpLastModified(Content content) {
        Instant instant = Instant.ofEpochSecond(content.getLastModified());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, GMT);
        return LAST_MODIFIED_FORMATTER.format(localDateTime);
    }

    private String getHttpCacheControl(ContentRequest contentRequest) {
        return Boolean.TRUE.equals(contentRequest.getNoCache()) ? "no-cache" : httpCacheControlDefaultValue;
    }

    private String getHttpContentDisposition(ContentRequest contentRequest) {
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

    private void replyWithHtml(HttpServletResponse response, String html, int statusCode) {
        response.setContentType(contentTypeHTML);
        response.setStatus(statusCode);
        try {
            response.getWriter().println(html);
        } catch (IOException e) {
            logger.error("Could not get response writer", e);
        }
    }

    @Override
    public void destroy() {
        if (cacheFactory != null)
            cacheFactory.close();
    }
}
