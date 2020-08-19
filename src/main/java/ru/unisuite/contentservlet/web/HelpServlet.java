package ru.unisuite.contentservlet.web;

import org.apache.commons.io.IOUtils;
import ru.unisuite.contentservlet.config.ApplicationConfig;
import ru.unisuite.contentservlet.config.BuildProperties;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@WebServlet({"/", "/help"})
public class HelpServlet extends HttpServlet {
    private static final String HELP_FILE = "/help.txt";

    private String buildOutput;

    @Override
    public void init() {
        ApplicationConfig applicationConfig = (ApplicationConfig) getServletContext().getAttribute("applicationConfig");
        BuildProperties buildProperties = applicationConfig.getBuildProperties();

        if (buildProperties != null) {
            //@formatter:off
            buildOutput = "\n\nVersion Info"
                    + "\nbuild date: "     + buildProperties.getBuildDate()
                    + "\nbranch: "         + buildProperties.getBranch()
                    + "\ncommit date: "    + buildProperties.getCommitDate()
                    + "\ncommit id: "      + buildProperties.getCommitId()
                    + "\ncommit message: " + buildProperties.getShortMessage()
                    + "\ndirty: "          + buildProperties.getDirty();
            //@formatter:on
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        respondWithHelp(response);
    }

    private void respondWithHelp(HttpServletResponse response) throws IOException {
        OutputStream out = response.getOutputStream();
        try (InputStream is = getServletContext().getResourceAsStream(HELP_FILE)) {
            IOUtils.copy(is, out);
        }

        if (buildOutput != null) {
            out.write(buildOutput.getBytes(StandardCharsets.UTF_8));
        }
    }
}
