package ru.unisuite.contentservlet.config;

public enum ResizerType {
    DB, THUMBNAILATOR, IMAGEMAGICK, IMAGINARY;

    public static ResizerType forValue(String s) {
        if (s == null) return null;
        if (s.equalsIgnoreCase("db")) return DB;
        if (s.equalsIgnoreCase("th")) return THUMBNAILATOR;
        if (s.equalsIgnoreCase("im") || s.equalsIgnoreCase("im4java")) return IMAGEMAGICK;
        if (s.equalsIgnoreCase("imaginary")) return IMAGINARY;
        return null;
    }
}
