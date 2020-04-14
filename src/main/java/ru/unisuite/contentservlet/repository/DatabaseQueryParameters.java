package ru.unisuite.contentservlet.repository;

public class DatabaseQueryParameters {

    private final Long webMetaId;
    private final String webMetaAlias;
    private final Long fileVersionId;
    private final Long idFe;
    private final Long entryIdInPhotoalbum;

    private final Integer width;
    private final Integer height;

    private final int quality;

    public DatabaseQueryParameters(Long webMetaId, String webMetaAlias, Long fileVersionId, Long idFe,
                                   Long entryIdInPhotoalbum, Integer width, Integer height, int quality) {
        this.webMetaId = webMetaId;
        this.webMetaAlias = webMetaAlias;
        this.fileVersionId = fileVersionId;
        this.idFe = idFe;
        this.entryIdInPhotoalbum = entryIdInPhotoalbum;
        this.width = width;
        this.height = height;
        this.quality = quality;
    }

    public Long getWebMetaId() {
        return webMetaId;
    }

    public String getWebMetaAlias() {
        return webMetaAlias;
    }

    public Long getFileVersionId() {
        return fileVersionId;
    }

    public Long getIdFe() {
        return idFe;
    }

    public Long getEntryIdInPhotoalbum() {
        return entryIdInPhotoalbum;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public int getQuality() {
        return quality;
    }
}
