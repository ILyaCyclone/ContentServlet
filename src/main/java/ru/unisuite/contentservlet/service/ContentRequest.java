package ru.unisuite.contentservlet.service;

import ru.unisuite.contentservlet.config.ResizerType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContentRequest {
    private Long idWebMetaterm;
    private String metatermAlias;
    private Long idFe;
    private Long entryIdInPhotoalbum;
    private Long idPropose;
    private Long fileVersionId;

    private Integer contentDisposition;
    private String filename;

    private ResizerType resizerType;
    private Integer width;
    private Integer height;
    private Byte quality;

    private String cacheControl;
    private Boolean noCache;
    private Boolean privateCache;

    public boolean hasRequiredParameters() {
        return Stream.of(idWebMetaterm, metatermAlias
                , idFe, entryIdInPhotoalbum
                , fileVersionId
                , idPropose)
                .anyMatch(Objects::nonNull);
    }

    public Map<String, Object> values() {
        Map<String, Object> values = new LinkedHashMap<>();
        if (idWebMetaterm != null) values.put("idWebMetaterm", idWebMetaterm);
        if (metatermAlias != null) values.put("metatermAlias", metatermAlias);
        if (idFe != null) values.put("idFe", idFe);
        if (entryIdInPhotoalbum != null) values.put("entryIdInPhotoalbum", entryIdInPhotoalbum);
        if (fileVersionId != null) values.put("fileVersionId", fileVersionId);

        if (contentDisposition != null) values.put("contentDisposition", contentDisposition);
        if (filename != null) values.put("filename", filename);

        if (resizerType != null) values.put("resizerType", resizerType);
        if (width != null) values.put("width", width);
        if (height != null) values.put("height", height);
        if (quality != null) values.put("quality", quality);

        if (cacheControl != null) values.put("cacheControl", cacheControl);
        if (noCache != null) values.put("noCache", noCache);
        if (privateCache != null) values.put("private", privateCache);
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
    public Long getIdWebMetaterm() {
        return idWebMetaterm;
    }

    public void setIdWebMetaterm(Long idWebMetaterm) {
        this.idWebMetaterm = idWebMetaterm;
    }

    public String getMetatermAlias() {
        return metatermAlias;
    }

    public void setMetatermAlias(String metatermAlias) {
        this.metatermAlias = metatermAlias;
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

    public Long getIdPropose() {
        return idPropose;
    }

    public void setIdPropose(Long idPropose) {
        this.idPropose = idPropose;
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

    public ResizerType getResizerType() {
        return resizerType;
    }

    public void setResizerType(ResizerType resizerType) {
        this.resizerType = resizerType;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    public Boolean getPrivateCache() {
        return privateCache;
    }

    public void setPrivateCache(Boolean privateCache) {
        this.privateCache = privateCache;
    }
}
