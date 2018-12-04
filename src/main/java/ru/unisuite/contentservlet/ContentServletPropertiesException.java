package ru.unisuite.contentservlet;

class ContentServletPropertiesException extends Exception {

	private static final long serialVersionUID = 1L;

	private int errorCode;

	public ContentServletPropertiesException(final String message) {
		this(0, "See configuration file: " + message);
	}

	public ContentServletPropertiesException(final int errorCode, final String message) {
		super(message);

		this.errorCode = errorCode;
	}

	public ContentServletPropertiesException(String message, Throwable cause) {
		super("See configuration file: " + message, cause);
	}

	public int getErrorCode() {
		return errorCode;
	}

}
