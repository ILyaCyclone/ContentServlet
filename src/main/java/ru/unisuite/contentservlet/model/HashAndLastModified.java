package ru.unisuite.contentservlet.model;

public class HashAndLastModified {
    private final String hash;
    private final Long lastModified;

    public HashAndLastModified(String hash, Long lastModified) {
        this.hash = hash;
        this.lastModified = lastModified;
    }

    public String getHash() {
        return hash;
    }

    public Long getLastModified() {
        return lastModified;
    }
}
