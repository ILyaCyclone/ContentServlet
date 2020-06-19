package ru.unisuite.contentservlet.web;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * http://logback.qos.ch/manual/mdc.html
 * ch.qos.logback.classic.helpers.MDCInsertingServletFilter
 */
//@WebFilter("/*")
@WebFilter("/get/*")
public class MDCFilter implements Filter {
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "req.id";

    private static final int RANDOM_REQUEST_ID_LEFT_BOUND = 97; // letter 'a'
    private static final int RANDOM_REQUEST_ID_RIGHT_BOUND = 122; // letter 'z'

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no action needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String requestId = httpServletRequest.getHeader(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = randomString(10);
        }
        MDC.put(REQUEST_ID_MDC_KEY, requestId);

        try {
            chain.doFilter(request, response);

            httpServletResponse.setHeader(REQUEST_ID_HEADER, requestId);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void destroy() {
        // no action needed
    }

    private String randomString(int length) {
        return ThreadLocalRandom.current().ints(RANDOM_REQUEST_ID_LEFT_BOUND
                , RANDOM_REQUEST_ID_RIGHT_BOUND + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
