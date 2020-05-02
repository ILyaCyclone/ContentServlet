package ru.unisuite.contentservlet.web;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import ru.unisuite.contentservlet.config.ApplicationConfig;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//@WebFilter("/*")
//@WebFilter("/get/*")
public class CustomMetricsFilter implements Filter {
    private static final String PREFIX = "custom_";
    private static final String HTTP_RESPONSE_COUNTER_NAME = PREFIX + "http_response";

    private Counter receivedRequests;

    private Counter responseOkCounter;
    private Counter responseNotModifiedCounter;
    private Counter responseBadRequestCounter;
    private Counter responseNotFoundCounter;
    private Counter responseInternalServerErrorCounter;

    private MeterRegistry meterRegistry;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationConfig applicationConfig = (ApplicationConfig) filterConfig.getServletContext().getAttribute("applicationConfig");
        this.meterRegistry = applicationConfig.getMeterRegistry();

        this.receivedRequests = this.meterRegistry.counter(PREFIX + "received_requests");
        //@formatter:off
        this.responseOkCounter                  = this.meterRegistry.counter(HTTP_RESPONSE_COUNTER_NAME, "status", "200");
        this.responseNotModifiedCounter         = this.meterRegistry.counter(HTTP_RESPONSE_COUNTER_NAME, "status", "304");
        this.responseBadRequestCounter          = this.meterRegistry.counter(HTTP_RESPONSE_COUNTER_NAME, "status", "401");
        this.responseNotFoundCounter            = this.meterRegistry.counter(HTTP_RESPONSE_COUNTER_NAME, "status", "404");
        this.responseInternalServerErrorCounter = this.meterRegistry.counter(HTTP_RESPONSE_COUNTER_NAME, "status", "500");
        //@formatter:on
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        receivedRequests.increment();
//        try {
        chain.doFilter(request, response);
//        } finally {
//            finishedRequests.increment();
//        }

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        counterForResponseStatus(httpServletResponse.getStatus()).increment();
    }

    private Counter counterForResponseStatus(int status) {
        switch (status) {
            //@formatter:off
            case 200: return responseOkCounter;
            case 304: return responseNotModifiedCounter;
            case 401: return responseBadRequestCounter;
            case 404: return responseNotFoundCounter;
            case 500: return responseInternalServerErrorCounter;
            default: return  meterRegistry.counter(PREFIX + "http_response", "status", Integer.toString(status));
            //@formatter:on
        }
    }


    @Override
    public void destroy() {
    }
}
