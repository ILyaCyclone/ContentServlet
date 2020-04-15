package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.scf4j.Cache;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

public interface ContentRepository {

    String getHtmlImgCode(long webMetaId) throws DatabaseReaderException;

    int getDefaultImageQuality() throws DatabaseReaderException, NotFoundException;

    void getBinaryDataByMeta(DatabaseQueryParameters queryParameters, OutputStream osServlet,
                             HttpServletResponse response, Cache persistantCache, String idInCache)
            throws DatabaseReaderException, NotFoundException;

    void getBinaryDataByFileVersionId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
                                      HttpServletResponse response, Cache persistantCache, String idInCache)
            throws DatabaseReaderException, NotFoundException;

    void getBinaryDataByClientId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
                                 HttpServletResponse response, Cache persistantCache, String idInCache)
            throws DatabaseReaderException, NotFoundException;


    Content getContentByIdWebMetaterm(long idWebMetaterm, Integer width, Integer height);
}
