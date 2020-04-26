package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.model.HashAndLastModified;

public interface HashAndLastModifiedRepository {

    HashAndLastModified getByIdWebMetaterm(Long idWebMetaterm);

    HashAndLastModified getByMetatermAlias(String metatermAlias);

    HashAndLastModified getByIdFe(Long idFe, Long entryIdInPhotoalbum);

    HashAndLastModified getByIdFileVersion(Long fileVersionId);

}
