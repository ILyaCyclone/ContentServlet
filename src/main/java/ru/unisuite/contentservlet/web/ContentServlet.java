package ru.unisuite.contentservlet.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.service.ContentRequest;
import ru.unisuite.contentservlet.service.ContentService;
import ru.unisuite.contentservlet.util.ListRoundRobin;
import ru.unisuite.imageprocessing.ImageProcessor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@WebServlet({"/*", "/private/*"})
//@WebServlet({ApplicationInitializer.CONTENT_URL_PATTERN, "/get/private/*"})
public class ContentServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(ContentServlet.class.getName());
    private static final String contentTypeHTML = "text/html; charset=UTF-8";

    private final RequestMapper requestMapper;
    private final ListRoundRobin<ServeContent> serveContents;

    ContentServlet(ApplicationConfig applicationConfig) {
        this.requestMapper = new RequestMapper();

        ContentService contentService = applicationConfig.contentService();

        Map<ResizerType, ImageProcessor> imageProcessors = applicationConfig.getImageProcessors();
        ResizerType defaultResizerType = applicationConfig.getResizerType();
        byte defaultImageQuality = applicationConfig.getDefaultImageQuality();

        MeterRegistry meterRegistry = applicationConfig.getMeterRegistry();

        HttpHeaders httpHeaders = new HttpHeaders(applicationConfig);


        List<ServeContent> serveContentImplementations = new ArrayList<>();
        Timer.Builder serverContentTimerBuilder = Timer.builder("contentservlet_serve_content")
                .publishPercentiles(0.5, 0.75, 0.9, 0.95);
        serveContentImplementations.add(new ServeContentTimed(
                new ServeContentInOneDBCall(contentService, httpHeaders, imageProcessors, defaultImageQuality, defaultResizerType)
                , serverContentTimerBuilder.tags("db_calls", "1").register(meterRegistry)
        ));
        serveContentImplementations.add(new ServeContentTimed(
                new ServeContentInTwoDBCalls(contentService, httpHeaders, imageProcessors, defaultImageQuality, defaultResizerType)
                , serverContentTimerBuilder.tags("db_calls", "2").register(meterRegistry)
        ));
        serveContents = new ListRoundRobin<>(serveContentImplementations);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (logger.isTraceEnabled())
            logger.trace("request URI {}", String.join("?", request.getRequestURI(), request.getQueryString()));

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
        ServeContent serveContent = serveContents.getNext();
        serveContent.serveContentRequest(contentRequest, request, response);
    }


    private void serveHelp(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getServletContext().getRequestDispatcher("/help").forward(request, response);
        } catch (ServletException | IOException e) {
            logger.error("Could not forward to help page {requestURI='{}', queryString='{}'}", request.getRequestURI(), request.getQueryString(), e);
        }
    }


    private void replyWithHtml(HttpServletResponse response, String html, int statusCode) {
        response.setContentType(contentTypeHTML);
        response.setStatus(statusCode);
        try {
            response.getWriter().println(html);
        } catch (IOException e) {
            logger.error("Could not print html response code {} '{}'", statusCode, html, e);
        }
    }
}
