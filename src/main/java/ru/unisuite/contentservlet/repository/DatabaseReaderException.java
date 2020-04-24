package ru.unisuite.contentservlet.repository;

@Deprecated
public class DatabaseReaderException extends Exception {
    private static final long serialVersionUID = 1L;

    private int errorCode;

    public DatabaseReaderException(final String message) {
        this(0, "Problems with fetching an object from DB: " + message);
    }

    public DatabaseReaderException(final int errorCode, final String message) {
        super(message);

        this.errorCode = errorCode;
    }

    public DatabaseReaderException(Throwable cause) {
        super(cause);
    }

    public DatabaseReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getErrorCode() {
        return errorCode;
    }

}
