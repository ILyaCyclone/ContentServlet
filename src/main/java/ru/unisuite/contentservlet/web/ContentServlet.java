package ru.unisuite.contentservlet.web;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.contentservlet.model.HashAndLastModified;
import ru.unisuite.contentservlet.service.ContentRequest;
import ru.unisuite.contentservlet.service.ContentService;
import ru.unisuite.contentservlet.service.ResizeService;
import ru.unisuite.imageresizer.ImageResizer;
import ru.unisuite.imageresizer.ImageResizerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//@WebServlet({"/*", "/private/*"})
@WebServlet({ApplicationInitializer.CONTENT_URL_PATTERN, "/get/private/*"})
public class ContentServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(ContentServlet.class.getName());

    private ContentService contentService;
    private RequestMapper requestMapper;

    private HttpHeaders httpHeaders;

    private ResizeService resizeService;
    private ResizerType defaultResizerType;
    private byte defaultImageQuality;

    private final static String contentTypeHTML = "text/html; charset=UTF-8";

//    private io.micrometer.core.instrument.MeterRegistry meterRegistry;

    @Override
    public void init() {
        ApplicationConfig applicationConfig = (ApplicationConfig) getServletContext().getAttribute("applicationConfig");

        this.contentService = applicationConfig.contentService();
        this.httpHeaders = new HttpHeaders(applicationConfig);

        this.resizeService = applicationConfig.resizeService();
        this.defaultResizerType = applicationConfig.getResizerType();
        this.defaultImageQuality = applicationConfig.getDefaultImageQuality();

//        this.meterRegistry = applicationConfig.getMeterRegistry();

        this.requestMapper = new RequestMapper();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (logger.isTraceEnabled())
            logger.trace("request URI {}", request.getRequestURI() + "?" + request.getQueryString());

        ContentRequest contentRequest;
        try {
            contentRequest = requestMapper.mapHttpServletRequest(request);
            if (logger.isDebugEnabled()) logger.debug(contentRequest.toString());
        } catch (Exception e) {
            logger.error("Could not map HttpRequest {} to ContentRequest", request.getRequestURI() + "?" + request.getQueryString(), e);

            String html = "<p>Некорректный запрос</p>" +
                    "<p>Пожалуйста, обратитесь к документации на странице <a href=\"help\">/help</a></p>";
            replyWithHtml(response, html, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!contentRequest.hasRequiredParameters()) {
            serveHelp(request, response);
            return;
        }

        serveContentRequest(contentRequest, request, response);
    }

    private void serveContentRequest(ContentRequest contentRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        httpHeaders.setCacheControl(response, contentRequest);

        String requestedCacheControl = contentRequest.getCacheControl();
        if (!(Boolean.TRUE.equals(contentRequest.getNoCache()) || (requestedCacheControl != null && requestedCacheControl.contains("no-cache")))) {

            String requestedEtag = httpHeaders.getEtag(request);
            String requestedModifiedSince = httpHeaders.getModifiedSince(request);

            if (checkNotModified(contentRequest, requestedEtag, requestedModifiedSince)) {
                if (logger.isTraceEnabled()) logger.trace("304 not modified {}", contentRequest);
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

        }

        replyWithContent(contentRequest, response);
    }

    private boolean checkNotModified(ContentRequest contentRequest, String requestedEtag, String requestedModifiedSince) {
        if (requestedEtag != null || requestedModifiedSince != null) {
            HashAndLastModified hashAndLastModified = contentService.getHashAndLastModified(contentRequest);

            if (requestedEtag != null && requestedEtag.equals(hashAndLastModified.getHash())) {
                return true;
            }

            long requestedLastModified = HttpDateFormatter.parse(requestedModifiedSince);
            if (requestedLastModified != -1L && requestedLastModified == hashAndLastModified.getLastModified()) {
                return true;
            }
        }
        return false;
    }

    private void replyWithContent(ContentRequest contentRequest, HttpServletResponse response) throws IOException {
        Content content;
        try {
            content = contentService.getContent(contentRequest);
        } catch (NotFoundException nfe) {
            replyWithHtml(response, "Запрошенный контент не найден", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        httpHeaders.setContentDisposition(response, contentRequest);
        httpHeaders.setContentResponseHeaders(response, content);

        Integer width = contentRequest.getWidth();
        Integer height = contentRequest.getHeight();
        if (width != null || height != null || contentRequest.getQuality() != null) {
            byte quality = contentRequest.getQuality() != null ? contentRequest.getQuality() : defaultImageQuality;
            ResizerType resizerType = contentRequest.getResizerType() != null ? contentRequest.getResizerType() : defaultResizerType;
            switch (resizerType) {
                case IMAGEMAGICK:
                    resizeService.writeResized(contentRequest, content, response.getOutputStream());
                    break;
                case THUMBNAILATOR:
                    ImageResizer thumbnailatorResizer = ImageResizerFactory.getImageResizer();
                    if (width != null && height == null) {
                        thumbnailatorResizer.resizeByWidth(content.getDataStream(), width, response.getOutputStream(), (int) quality);
                        break;
                    }
                    if (width == null && height != null) {
                        thumbnailatorResizer.resizeByHeight(content.getDataStream(), height, response.getOutputStream(), (int) quality);
                        break;
                    }
                    thumbnailatorResizer.resize(content.getDataStream(), width, height, response.getOutputStream(), (int) quality);
                    break;
                case DB:
                default:
                    IOUtils.copy(content.getDataStream(), response.getOutputStream());
            }
            return;
        }
        IOUtils.copy(content.getDataStream(), response.getOutputStream());
    }



    private void serveHelp(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getServletContext().getRequestDispatcher("/help").forward(request, response);
        } catch (ServletException | IOException e) {
            logger.error("Could not forward to help page", e);
        }
    }



    private void replyWithHtml(HttpServletResponse response, String html, int statusCode) {
        response.setContentType(contentTypeHTML);
        response.setStatus(statusCode);
        try {
            response.getWriter().println(html);
        } catch (IOException e) {
            logger.error("Could not print html response", e);
        }
    }
}
