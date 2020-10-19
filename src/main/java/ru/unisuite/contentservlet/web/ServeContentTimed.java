package ru.unisuite.contentservlet.web;

import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.service.ContentRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServeContentTimed implements ServeContent {
    private static final Logger logger = LoggerFactory.getLogger(ServeContentTimed.class);

    private final ServeContent serveContent;
    private final Timer timer;

    public ServeContentTimed(ServeContent serveContent, Timer timer) {
        this.serveContent = serveContent;
        this.timer = timer;
    }

    @Override
    public void serveContentRequest(ContentRequest contentRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        timer.record(() -> {
            try {
                serveContent.serveContentRequest(contentRequest, request, response);
            } catch (IOException e) {
                logger.error("Exception in timer record with serveContent class {}", serveContent.getClass(), e);
                throw new RuntimeException(e);
            }
        });
    }
}
