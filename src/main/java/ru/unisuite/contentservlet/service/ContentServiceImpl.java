package ru.unisuite.contentservlet.service;

import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.repository.ContentRepository;
import ru.unisuite.contentservlet.repository.DatabaseQueryParameters;
import ru.unisuite.contentservlet.repository.DatabaseReaderException;
import ru.unisuite.scf4j.Cache;
import ru.unisuite.scf4j.exception.SCF4JCacheGetException;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final boolean persistentCacheEnabled;
    private final NameCreator cacheFilenameCreator; // nullable


    public ContentServiceImpl(ContentRepository contentRepository, boolean persistentCacheEnabled, NameCreator cacheFilenameCreator) {
        this.contentRepository = contentRepository;
        this.persistentCacheEnabled = persistentCacheEnabled;
        this.cacheFilenameCreator = cacheFilenameCreator;
    }


    @Override
    public void getObject(ContentRequest contentRequest, OutputStream os, HttpServletResponse response,
                          Cache persistantCache)
            throws SCF4JCacheGetException, DatabaseReaderException, NotFoundException {

        if (persistentCacheEnabled) {
            // Создание id объекта в кэше
            String idInCache = cacheFilenameCreator.forContentRequest(contentRequest);

            boolean cacheHit = persistentCacheEnabled && persistantCache.connectionIsUp()
                    && persistantCache.exists(idInCache) && persistantCache.get(idInCache, os, response);

            if (cacheHit) {
                // увеличение попаданий в кэш
                persistantCache.increaseHits();
                return;
            }

            DatabaseQueryParameters queryParameters = new DatabaseQueryParameters(contentRequest.getWebMetaId(),
                    contentRequest.getWebMetaAlias(), contentRequest.getFileVersionId(),
                    contentRequest.getIdFe(), contentRequest.getEntryIdInPhotoalbum(),
                    contentRequest.getWidth(), contentRequest.getHeight(), contentRequest.getQuality());

            if (queryParameters.getWebMetaId() != null || contentRequest.getWebMetaAlias() != null) {

                contentRepository.getBinaryDataByMeta(queryParameters, os, response, persistantCache, idInCache);

            } else {

                if (queryParameters.getFileVersionId() != null) {

                    contentRepository.getBinaryDataByFileVersionId(queryParameters, os, response, persistantCache,
                            idInCache);

                } else {

                    if (queryParameters.getIdFe() != null || contentRequest.getEntryIdInPhotoalbum() != null) {

                        contentRepository.getBinaryDataByClientId(queryParameters, os, response, persistantCache,
                                idInCache);

                    }
                }
            }

            // увеличение промахов количество в кэш
            if (persistentCacheEnabled && persistantCache.connectionIsUp())
                persistantCache.increaseMisses();
        }

    }



    @Override
    public String getCodeData(long webMetaId) throws DatabaseReaderException {
        return contentRepository.getCodeData(webMetaId);
    }

}
