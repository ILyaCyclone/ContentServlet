package ru.unisuite.contentservlet.model;

import java.io.InputStream;

public class Content {
    private InputStream dataStream;
    private Long size;
    private Long lastModified;
    private String hash;
    private String mimeType;
    private String filename;
    private String extension;

    public InputStream getDataStream() {
        return dataStream;
    }

    public void setDataStream(InputStream dataStream) {
        this.dataStream = dataStream;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
