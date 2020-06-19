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

    private static final String contentDispositionHeaderName = "Content-Disposition";
    private static final String cacheControlHeaderName = "Cache-Control";

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
            if (isNumeric(requestedCacheControl)) {
                response.setHeader(cacheControlHeaderName, "public, max-age=" + requestedCacheControl);
            } else {
                response.setHeader(cacheControlHeaderName, requestedCacheControl);
            }
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
        response.setHeader("ETag", etag(content.getHash(), EtagValidation.WEAK));
        response.setContentType(content.getMimeType());
        // content-length is not needed
    }



    private String etag(String hash, EtagValidation etagValidation) {
        // check spec for strong/weak etag validators
        if (hash == null || hash.trim().length() == 0) return null;
        return (etagValidation == EtagValidation.WEAK ? "W/" : "") + '"' + hash + '"';
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

    private boolean isNumeric(String string) {
        if (string == null || string.trim().length() == 0) return false;
        return string.chars().allMatch(Character::isDigit);
    }

    private enum EtagValidation {
        STRONG, WEAK
    }
}
