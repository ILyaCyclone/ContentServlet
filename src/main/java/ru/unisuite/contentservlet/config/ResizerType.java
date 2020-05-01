package ru.unisuite.contentservlet.config;

public enum ResizerType {
    DB, THUMBNAILATOR, IMAGEMAGICK;

    public static ResizerType forValue(String s) {
        if (s == null) return null;
        if (s.equalsIgnoreCase("db")) return DB;
        if (s.equalsIgnoreCase("th")) return THUMBNAILATOR;
        if (s.equalsIgnoreCase("im")) return IMAGEMAGICK;
        return null;
    }
}
