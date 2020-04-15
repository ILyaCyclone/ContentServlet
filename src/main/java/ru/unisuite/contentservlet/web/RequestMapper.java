package ru.unisuite.contentservlet.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.service.ContentRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * map HttpServletRequest to {@code ru.unisuite.contentservlet.service.ContentRequest}
 */
public class RequestMapper {
    private final Logger logger = LoggerFactory.getLogger(RequestMapper.class.getName());

    public ContentRequest mapHttpServletRequest(HttpServletRequest httpServletRequest) {
        Map<String, String[]> parametersMap = httpServletRequest.getParameterMap();

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setContentDisposition(getIntValue(parametersMap, ServletParamName.contentDisposition));
        contentRequest.setContentType(getIntValue(parametersMap, ServletParamName.contentType));

        contentRequest.setWebMetaId(getLongValue(parametersMap, ServletParamName.webMetaId));
        contentRequest.setWebMetaAlias(getStringValue(parametersMap, ServletParamName.webMetaAlias));
        contentRequest.setFileVersionId(getLongValue(parametersMap, ServletParamName.fileVersionId));
        contentRequest.setIdFe(getLongValue(parametersMap, ServletParamName.clientId));
        contentRequest.setEntryIdInPhotoalbum(getLongValue(parametersMap, ServletParamName.entryIdInPhotoalbum));
        contentRequest.setWidth(getIntValue(parametersMap, ServletParamName.width));
        contentRequest.setHeight(getIntValue(parametersMap, ServletParamName.height));

        contentRequest.setNoCache(parametersMap.containsKey(ServletParamName.cacheControl));

        Integer requestedQuality = getIntValue(parametersMap, ServletParamName.quality);

        if (requestedQuality != null) {
            if (requestedQuality >= 0 && requestedQuality <= 100) {
                contentRequest.setQuality(requestedQuality.byteValue());
            } else {
                logger.warn("Incorrect quality in request " + parametersMap + ". Default quality will be used.");
            }
        }

        contentRequest.setFilename(getRequestedFilename(httpServletRequest));

        return contentRequest;
    }

    private final Pattern filenamePattern = Pattern.compile("[^/]*[^/]");
    private String getRequestedFilename(HttpServletRequest httpServletRequest) {
        String uri = httpServletRequest.getRequestURI();
        Matcher matcher = filenamePattern.matcher(uri);
        String rS = null;
        while (matcher.find()) {
            rS = matcher.group(matcher.groupCount());
        }
        return rS;
    }



    private String getStringValue(Map<String, String[]> parametersMap, String parameterName) {
        return parametersMap.containsKey(parameterName) ? parametersMap.get(parameterName)[0] : null;
    }
    private Integer getIntValue(Map<String, String[]> parametersMap, String parameterName) {
        return parametersMap.containsKey(parameterName) ? Integer.parseInt(parametersMap.get(parameterName)[0]) : null;
    }
    private Long getLongValue(Map<String, String[]> parametersMap, String parameterName) {
        return parametersMap.containsKey(parameterName) ? Long.parseLong(parametersMap.get(parameterName)[0]) : null;
    }
}
