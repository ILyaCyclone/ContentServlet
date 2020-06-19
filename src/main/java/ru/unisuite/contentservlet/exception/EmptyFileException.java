package ru.unisuite.contentservlet.exception;

public class EmptyFileException extends RuntimeException {
    public EmptyFileException() {
        super();
    }

    public EmptyFileException(String message) {
        super(message);
    }
}
