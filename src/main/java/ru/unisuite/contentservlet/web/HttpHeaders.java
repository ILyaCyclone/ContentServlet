package ru.unisuite.contentservlet.web;

import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.contentservlet.service.ContentRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Hypertext Transfer Protocol (HTTP/1.1): Conditional Requests - https://tools.ietf.org/html/rfc7232
// https://www.baeldung.com/etags-for-rest-with-spring
class HttpHeaders {

    private final String httpCacheControlDefaultValue;

    private final static String contentDispositionHeaderName = "Content-Disposition";
    private final static String cacheControlHeaderName = "Cache-Control";

    HttpHeaders(ApplicationConfig applicationConfig) {
        this.httpCacheControlDefaultValue = applicationConfig.getCacheControl();
    }



    public String getEtag(HttpServletRequest request) {
        return request.getHeader("If-None-Match");
    }

    public String getModifiedSince(HttpServletRequest request) {
        return request.getHeader("If-Modified-Since");
    }


    public void setCacheControl(HttpServletResponse response, ContentRequest contentRequest) {
        String requestedCacheControl = contentRequest.getCacheControl();
        if (requestedCacheControl != null) {
            response.setHeader(cacheControlHeaderName, requestedCacheControl);
        } else {
            if (Boolean.TRUE.equals(contentRequest.getPrivateCache())) {
                response.setHeader(cacheControlHeaderName, "private");
            } else {
                if (Boolean.TRUE.equals(contentRequest.getNoCache())) {
                    response.setHeader(cacheControlHeaderName, "no-cache");
                } else {
                    response.setHeader(cacheControlHeaderName, httpCacheControlDefaultValue);
                }
            }
        }
    }

    public void setContentDisposition(HttpServletResponse response, ContentRequest contentRequest) {
        response.setHeader(contentDispositionHeaderName, calculateContentDisposition(contentRequest));
    }

    public void setContentResponseHeaders(HttpServletResponse response, Content content) {
        response.setHeader("Last-Modified", getHttpLastModified(content));
        response.setHeader("ETag", weakEtag(content.getHash()));
        response.setContentType(content.getMimeType());
        // content-length is not needed
    }



    private String weakEtag(String etag) {
        // check spec for strong/weak etag validators
        if (etag == null || etag.trim().length() == 0) return null;
        if (etag.startsWith("W/\"")) return etag;
        return "W/\"" + etag + '"';
    }


    private String getHttpLastModified(Content content) {
        return content.getLastModified() != null ? HttpDateFormatter.format(content.getLastModified()) : null;
    }

    private String calculateContentDisposition(ContentRequest contentRequest) {
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
}
