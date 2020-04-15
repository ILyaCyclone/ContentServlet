package ru.unisuite.contentservlet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.contentservlet.repository.ContentRepository;
import ru.unisuite.contentservlet.repository.DatabaseQueryParameters;
import ru.unisuite.contentservlet.repository.DatabaseReaderException;
import ru.unisuite.imageresizer.ImageResizerFactory;
import ru.unisuite.scf4j.Cache;
import ru.unisuite.scf4j.exception.SCF4JCacheGetException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class ContentServiceImpl implements ContentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    private final ContentRepository contentRepository;

    private final ResizerType resizerType;

    private final boolean persistentCacheEnabled;
    private final NameCreator cacheFilenameCreator; // nullable


    public ContentServiceImpl(ContentRepository contentRepository
            , ResizerType resizerType
            , boolean persistentCacheEnabled, NameCreator cacheFilenameCreator) {
        this.contentRepository = contentRepository;
        this.resizerType = resizerType;
        this.persistentCacheEnabled = persistentCacheEnabled;
        this.cacheFilenameCreator = cacheFilenameCreator;
    }



    @Override
    public Content getContentByIdWebMetaterm(long idWebMetaterm, Integer width, Integer height, Byte quality) {
        if (resizerType == ResizerType.DB) {
            return contentRepository.getContentByIdWebMetaterm(idWebMetaterm, width, height);
        }
        if (resizerType == ResizerType.THUMBNAILATOR) {
            // resize will be called in a separate service
            return contentRepository.getContentByIdWebMetaterm(idWebMetaterm, null, null);
        }
        // we don't know about resizerType
        return contentRepository.getContentByIdWebMetaterm(idWebMetaterm, width, height);


//        if (resizerType == ResizerType.DB) {
//            return contentRepository.getContentByIdWebMetaterm(idWebMetaterm, width, height);
//        }
//        if (resizerType == ResizerType.THUMBNAILATOR) {
//            Content content = contentRepository.getContentByIdWebMetaterm(idWebMetaterm, null, null);
//            byte defaultQuality = 80;
//
//            try {
//                // https://stackoverflow.com/questions/5778658/how-to-convert-outputstream-to-inputstream
//                PipedOutputStream out = new PipedOutputStream();
//                PipedInputStream in = new PipedInputStream(out);
//                Thread pipedThread = new Thread((Runnable) () -> {
//                    try {
//                        ImageResizerFactory.getImageResizer().resize(content.getDataStream(), width, height, out
//                                , (int) (quality != null ? quality : defaultQuality));
//                    } catch (IOException e) {
//                        logger.error("Could not resize image {idWebMetaterm=" + idWebMetaterm + ", width=" + width + ", height+" + height + '}');
//                    } finally {
//                        try {
//                            out.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//                pipedThread.start();
//                pipedThread.join();
//                content.setDataStream(in);
//                // TODO how to set content-length? the internet says content-length is managed automatically
//            } catch (IOException | InterruptedException e) {
//                logger.error("Could not resize image {idWebMetaterm=" + idWebMetaterm + ", width=" + width + ", height+" + height + '}');
//            }
//            return content;
//        }
//        throw new RuntimeException("Unknown resizerType='"+resizerType+'\'');
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
    public String getHtmlImgCode(long webMetaId) throws DatabaseReaderException {
        return contentRepository.getHtmlImgCode(webMetaId);
    }

}
