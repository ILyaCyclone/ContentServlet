package ru.unisuite.contentservlet.service;

import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.contentservlet.repository.DatabaseReaderException;
import ru.unisuite.scf4j.Cache;
import ru.unisuite.scf4j.exception.SCF4JCacheGetException;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.PrintWriter;

public interface ContentService {
    void getObject(ContentRequest contentRequest, OutputStream os, HttpServletResponse response,
                   Cache persistantCache)
            throws SCF4JCacheGetException, DatabaseReaderException, NotFoundException;

    String getHtmlImgCode(long webMetaId) throws DatabaseReaderException;



    Content getContentByIdWebMetaterm(long idWebMetaterm, Integer width, Integer height, Byte quality);
}
