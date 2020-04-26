package ru.unisuite.contentservlet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.contentservlet.model.HashAndLastModified;
import ru.unisuite.contentservlet.repository.ContentRepository;
import ru.unisuite.contentservlet.repository.HashAndLastModifiedRepository;

public class ContentServiceImpl implements ContentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    private final ContentRepository contentRepository;
    private final HashAndLastModifiedRepository hashAndLastModifiedRepository;

    private final ResizerType defaultResizerType;

    public ContentServiceImpl(ContentRepository contentRepository
            , HashAndLastModifiedRepository hashAndLastModifiedRepository
            , ResizerType defaultResizerType) {
        this.contentRepository = contentRepository;
        this.hashAndLastModifiedRepository = hashAndLastModifiedRepository;
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

        if (contentRequest.getFileVersionId() != null) {
            return contentRepository.getContentByIdFileVersion(contentRequest.getFileVersionId(), effectiveWidth, effectiveHeight);
        }

        throw new IllegalArgumentException("ContentRequest does not determine content " + contentRequest);
    }



    @Override
    public HashAndLastModified getHashAndLastModified(ContentRequest contentRequest) {
        if (contentRequest.getIdWebMetaterm() != null) {
            return hashAndLastModifiedRepository.getByIdWebMetaterm(contentRequest.getIdWebMetaterm());
        }

        if (contentRequest.getMetatermAlias() != null) {
            return hashAndLastModifiedRepository.getByMetatermAlias(contentRequest.getMetatermAlias());
        }

        if (contentRequest.getIdFe() != null || contentRequest.getEntryIdInPhotoalbum() != null) {
            //TODO get from hashAndLastModifiedRepository
//            return hashAndLastModifiedRepository.getByIdFe(contentRequest.getIdFe(), contentRequest.getEntryIdInPhotoalbum());
            Content content = contentRepository.getContentByIdFe(contentRequest.getIdFe(), contentRequest.getEntryIdInPhotoalbum()
                    , contentRequest.getWidth(), contentRequest.getHeight());
            return new HashAndLastModified(content.getHash(), content.getLastModified());
        }

        if (contentRequest.getFileVersionId() != null) {
            //TODO get from hashAndLastModifiedRepository
//            return hashAndLastModifiedRepository.getByIdFileVersion(contentRequest.getFileVersionId());
            Content content = contentRepository.getContentByIdFileVersion(contentRequest.getFileVersionId()
                    , contentRequest.getWidth(), contentRequest.getHeight());
            return new HashAndLastModified(content.getHash(), content.getLastModified());
        }

        throw new IllegalArgumentException("ContentRequest does not determine content " + contentRequest);
    }
}
