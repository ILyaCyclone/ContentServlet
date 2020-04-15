package ru.unisuite.contentservlet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ResizerType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ContentRequest {
    private final Logger logger = LoggerFactory.getLogger(ContentRequest.class.getName());

    private Long webMetaId;
    private String webMetaAlias;
    private Long idFe; // former clientId
    private Long entryIdInPhotoalbum;
    private Long fileVersionId;

    private Integer contentDisposition;
    private Integer contentType;

    private ResizerType resizerType;
    private Integer width;
    private Integer height;
    private Boolean noCache;
    private Byte quality;

    private String filename;


    public boolean isEmpty() {
        return webMetaId == null && webMetaAlias == null && fileVersionId == null
                && idFe == null && entryIdInPhotoalbum == null;
    }

    public Map<String, Object> values() {
        Map<String, Object> values = new LinkedHashMap<>();
        if (webMetaId != null) values.put("webMetaId", webMetaId);
        if (webMetaAlias != null) values.put("webMetaAlias", webMetaAlias);
        if (idFe != null) values.put("idFe", idFe);
        if (entryIdInPhotoalbum != null) values.put("entryIdInPhotoalbum", entryIdInPhotoalbum);
        if (fileVersionId != null) values.put("fileVersionId", fileVersionId);

        if (filename != null) values.put("filename", filename);

        if (contentType != null) values.put("contentType", contentType);
        if (contentDisposition != null) values.put("contentDisposition", contentDisposition);

        if (width != null) values.put("width", width);
        if (height != null) values.put("height", height);
        if (noCache != null) values.put("noCache", noCache);
        if (quality != null) values.put("quality", quality);
        return values;
    }


    @Override
    public String toString() {
        String valuesStringified = values().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + decorateToStringValue(entry.getValue()))
                .collect(Collectors.joining(", "));

        return "ContentRequest{" + valuesStringified + '}';
    }

    private String decorateToStringValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return '\'' + (String) value + '\'';
        return value.toString();
    }


    // accessors
    public Long getWebMetaId() {
        return webMetaId;
    }

    public void setWebMetaId(Long webMetaId) {
        this.webMetaId = webMetaId;
    }

    public String getWebMetaAlias() {
        return webMetaAlias;
    }

    public void setWebMetaAlias(String webMetaAlias) {
        this.webMetaAlias = webMetaAlias;
    }

    public Long getIdFe() {
        return idFe;
    }

    public void setIdFe(Long idFe) {
        this.idFe = idFe;
    }

    public Long getEntryIdInPhotoalbum() {
        return entryIdInPhotoalbum;
    }

    public void setEntryIdInPhotoalbum(Long entryIdInPhotoalbum) {
        this.entryIdInPhotoalbum = entryIdInPhotoalbum;
    }

    public Long getFileVersionId() {
        return fileVersionId;
    }

    public void setFileVersionId(Long fileVersionId) {
        this.fileVersionId = fileVersionId;
    }

    public Integer getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(Integer contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public Integer getContentType() {
        return contentType;
    }

    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Boolean getNoCache() {
        return noCache;
    }

    public void setNoCache(Boolean noCache) {
        this.noCache = noCache;
    }

    public Byte getQuality() {
        return quality;
    }

    public void setQuality(Byte quality) {
        this.quality = quality;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
