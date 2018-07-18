package ru.miit.contentservlet.databasereader;

public class DatabaseReaderWriteToStreamException extends DatabaseReaderException {

	private static final long serialVersionUID = 1L;

	private int errorCode;
	
    public DatabaseReaderWriteToStreamException(final String message)
    {
        this(0, "Problems with fetching an object from DB: " + message);
    }
 
    public DatabaseReaderWriteToStreamException(final int errorCode, final String message)
    {
        super(message);

        this.errorCode = errorCode;
    }
 
    public int getErrorCode()
    {
        return errorCode;
    }
	
}