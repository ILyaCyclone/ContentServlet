package ru.miit.databasereader;

public class OracleDatabaseReaderConnectionException extends OracleDatabaseReaderException{
	
	private static final long serialVersionUID = 1L;

	private int errorCode;
	
    public OracleDatabaseReaderConnectionException(final String message)
    {
        this(0, "Сonnection is failed: " + message);
    }
 
    public OracleDatabaseReaderConnectionException(final int errorCode, final String message)
    {
        super(message);

        this.errorCode = errorCode;
    }
 
    public int getErrorCode()
    {
        return errorCode;
    }
    
}
