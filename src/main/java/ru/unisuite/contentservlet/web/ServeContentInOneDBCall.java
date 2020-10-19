package ru.unisuite.contentservlet.web;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.contentservlet.service.ContentRequest;
import ru.unisuite.contentservlet.service.ContentService;
import ru.unisuite.imageprocessing.ImageParameters;
import ru.unisuite.imageprocessing.ImageProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ServeContentInOneDBCall implements ServeContent {
    private static final Logger logger = LoggerFactory.getLogger(ServeContentInOneDBCall.class);

    private final ContentService contentService;
    private final HttpHeaders httpHeaders;
    private final Map<ResizerType, ImageProcessor> imageProcessors;
    private final byte defaultImageQuality;
    private final ResizerType defaultResizerType;

    public ServeContentInOneDBCall(ContentService contentService, HttpHeaders httpHeaders, Map<ResizerType, ImageProcessor> imageProcessors, byte defaultImageQuality, ResizerType defaultResizerType) {
        this.contentService = contentService;
        this.httpHeaders = httpHeaders;
        this.imageProcessors = imageProcessors;
        this.defaultImageQuality = defaultImageQuality;
        this.defaultResizerType = defaultResizerType;
    }

    @Override
    public void serveContentRequest(ContentRequest contentRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Content content;
        try {
            content = contentService.getContent(contentRequest);
        } catch (NotFoundException nfe) {
            replyWithHtml(response, "Запрошенный контент не найден", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        httpHeaders.setCacheControl(response, contentRequest);

        String requestedCacheControl = contentRequest.getCacheControl();
        if (!(Boolean.TRUE.equals(contentRequest.getNoCache()) || (requestedCacheControl != null && requestedCacheControl.contains("no-cache")))) {

            String requestedEtag = httpHeaders.getEtag(request);
            String requestedModifiedSince = httpHeaders.getModifiedSince(request);

            if (checkNotModified(content, requestedEtag, requestedModifiedSince)) {
                if (logger.isTraceEnabled()) logger.trace("304 not modified {}", contentRequest);
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

        }

        replyWithContent(contentRequest, content, response);
    }

    private boolean checkNotModified(Content content, String requestedEtag, String requestedModifiedSince) {
        if (requestedEtag != null || requestedModifiedSince != null) {
            if (requestedEtag != null && requestedEtag.equals(content.getHash())) {
                return true;
            }

            long requestedLastModified = HttpDateFormatter.parse(requestedModifiedSince);
            if (requestedLastModified != -1L && requestedLastModified == content.getLastModified()) {
                return true;
            }
        }
        return false;
    }

    private void replyWithContent(ContentRequest contentRequest, Content content, HttpServletResponse response) throws IOException {
        httpHeaders.setContentDisposition(response, contentRequest);
        httpHeaders.setContentResponseHeaders(response, content);

        Integer width = contentRequest.getWidth();
        Integer height = contentRequest.getHeight();
        if (width != null || height != null || contentRequest.getQuality() != null) {
            ResizerType resizerType = Optional.ofNullable(contentRequest.getResizerType()).orElse(defaultResizerType);
            if (resizerType != ResizerType.DB) {
                byte quality = Optional.ofNullable(contentRequest.getQuality()).orElse(defaultImageQuality);

                ImageProcessor imageProcessor = imageProcessors.get(resizerType);
                ImageParameters imageParameters = new ImageParameters();
                imageParameters.setQuality(quality);
                imageParameters.setSourceFormat(content.getExtension());
                if (width != null) {
                    imageParameters.setWidth(width);
                }
                if (height != null) {
                    imageParameters.setHeight(height);
                }

                imageProcessor.resize(content.getDataStream(), imageParameters, response.getOutputStream());
                return;
            }
        }
        IOUtils.copy(content.getDataStream(), response.getOutputStream());
    }


    private void replyWithHtml(HttpServletResponse response, String html, int statusCode) {
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(statusCode);
        try {
            response.getWriter().println(html);
        } catch (IOException e) {
            logger.error("Could not print html response '{}'", html, e);
        }
    }
}
