package ru.unisuite.contentservlet.web;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

/**
 * http://logback.qos.ch/manual/mdc.html
 * ch.qos.logback.classic.helpers.MDCInsertingServletFilter
 */
//@WebFilter("/*")
@WebFilter("/get/*")
public class MDCFilter implements Filter {
    private Random random;
    private static final String REQUEST_ID_HEADER = "X-REQUEST-ID";
    private static final String REQUEST_ID_MDC_KEY = "req.id";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        random = new Random();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String requestId = ((HttpServletRequest) request).getHeader(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = String.valueOf(random.nextInt(Integer.MAX_VALUE));
        }
        MDC.put(REQUEST_ID_MDC_KEY, requestId);

        try {
            chain.doFilter(request, response);

            ((HttpServletResponse) response).setHeader(REQUEST_ID_HEADER, requestId);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void destroy() {
    }
}
