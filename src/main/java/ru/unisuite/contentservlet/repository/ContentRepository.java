package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.model.Content;

public interface ContentRepository {

    Content getContentByIdWebMetaterm(Long idWebMetaterm, Integer width, Integer height);

    Content getContentByMetatermAlias(String metatermAlias, Integer width, Integer height);

    Content getContentByIdFe(Long idFe, Long idPhotoAlbum, Integer width, Integer height);

    Content getContentByIdFileVersion(Long idFileVersion, Integer width, Integer height);

}
