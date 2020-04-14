package ru.unisuite.contentservlet.service;

import ru.unisuite.contentservlet.exception.NotFoundException;
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

    String getCodeData(long webMetaId) throws DatabaseReaderException;
}
