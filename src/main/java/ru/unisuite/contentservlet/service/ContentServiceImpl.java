package ru.unisuite.contentservlet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.contentservlet.repository.ContentRepository;

public class ContentServiceImpl implements ContentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    private final ContentRepository contentRepository;

    private final ResizerType defaultResizerType;

    public ContentServiceImpl(ContentRepository contentRepository
            , ResizerType defaultResizerType
            , boolean persistentCacheEnabled, NameCreator cacheFilenameCreator) {
        this.contentRepository = contentRepository;
        this.defaultResizerType = defaultResizerType;
    }


    @Override
    public Content getContent(ContentRequest contentRequest) {
        ResizerType resizerType = contentRequest.getResizerType() != null ? contentRequest.getResizerType() : defaultResizerType;
        boolean sendDimensionToDB = resizerType == ResizerType.DB;
        Integer effectiveWidth = sendDimensionToDB ? contentRequest.getWidth() : null;
        Integer effectiveHeight = sendDimensionToDB ? contentRequest.getHeight() : null;

        if (contentRequest.getIdWebMetaterm() != null) {
            return contentRepository.getContentByIdWebMetaterm(contentRequest.getIdWebMetaterm(), effectiveWidth, effectiveHeight);
        }

        if (contentRequest.getMetatermAlias() != null) {
            return contentRepository.getContentByMetatermAlias(contentRequest.getMetatermAlias(), effectiveWidth, effectiveHeight);
        }

        if (contentRequest.getIdFe() != null || contentRequest.getEntryIdInPhotoalbum() != null) {
            return contentRepository.getContentByIdFe(contentRequest.getIdFe(), contentRequest.getEntryIdInPhotoalbum(), effectiveWidth, effectiveHeight);
        }

        if(contentRequest.getFileVersionId() != null) {
            return contentRepository.getContentByIdFileVersion(contentRequest.getFileVersionId(), effectiveWidth, effectiveHeight);
        }

        throw new IllegalArgumentException("ContentRequest does not determine content "+contentRequest);
    }

}
