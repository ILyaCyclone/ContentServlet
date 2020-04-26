package ru.unisuite.contentservlet.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.service.ContentRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * map HttpServletRequest to {@link ru.unisuite.contentservlet.service.ContentRequest}
 */
public class RequestMapper {
    private final Logger logger = LoggerFactory.getLogger(RequestMapper.class.getName());

    public ContentRequest mapHttpServletRequest(HttpServletRequest httpServletRequest) {
        Map<String, String[]> httpServletRequestParameters = httpServletRequest.getParameterMap();
        Map<String, String> params = httpServletRequestParameters.entrySet().stream()
                .collect(HashMap::new, (map, e) -> map.put(e.getKey(), e.getValue()[0]), HashMap::putAll);

        ContentRequest contentRequest = new ContentRequest();

        // required parameters
        contentRequest.setIdWebMetaterm(getLongValue(params, RequestParamName.webMetaId));
        contentRequest.setMetatermAlias(params.get(RequestParamName.webMetaAlias));
        contentRequest.setFileVersionId(getLongValue(params, RequestParamName.fileVersionId));
        contentRequest.setIdFe(getLongValue(params, RequestParamName.idFe));
        contentRequest.setEntryIdInPhotoalbum(getLongValue(params, RequestParamName.entryIdInPhotoalbum));

        contentRequest.setContentDisposition(getIntValue(params, RequestParamName.contentDisposition));

        String resizerParam = params.get(RequestParamName.resizerType);
        if (resizerParam != null) {
            //TODO make safe
            contentRequest.setResizerType(ResizerType.valueOf(resizerParam.toUpperCase()));
        }
        contentRequest.setWidth(getIntValue(params, RequestParamName.width));
        contentRequest.setHeight(getIntValue(params, RequestParamName.height));

        Integer requestedQuality = getIntValue(params, RequestParamName.quality);
        if (requestedQuality != null) {
            if (requestedQuality >= 0 && requestedQuality <= 100) {
                contentRequest.setQuality(requestedQuality.byteValue());
            } else {
                logger.warn("Incorrect quality in request " + params + ". Default quality will be used.");
            }
        }

        if (params.containsKey(RequestParamName.noCache)) {
            contentRequest.setNoCache(params.containsKey(RequestParamName.noCache));
        } else {
            if ("no-cache".equals(httpServletRequest.getHeader("Cache-Control"))) {
                contentRequest.setNoCache(true);
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



    private Long getLongValue(Map<String, String> params, String parameterName) {
        return params.containsKey(parameterName) ? Long.parseLong(params.get(parameterName)) : null;
    }

    private Integer getIntValue(Map<String, String> params, String parameterName) {
        return params.containsKey(parameterName) ? Integer.parseInt(params.get(parameterName)) : null;
    }

    private Integer getIntValue(Map<String, String> params, String[] parameterNames) {
        return getParameter(params, parameterNames, Integer::parseInt);
    }

    private <T> T getParameter(Map<String, String> params, String[] parameterNames, Function<String, T> mappingFunction) {
        return Stream.of(parameterNames)
                .filter(params::containsKey)
                .map(params::get)
                .findFirst()
                .map(mappingFunction)
                .orElse(null);
    }
}
