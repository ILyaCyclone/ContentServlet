package ru.miit.contentservlet.databasereader;

class OracleDatabaseReaderServletOSException extends OracleDatabaseReaderException {

	private static final long serialVersionUID = 1L;

	private int errorCode;
	
    public OracleDatabaseReaderServletOSException(final String message)
    {
        this(0, "Content sending to client is failed: " + message);
    }
 
    public OracleDatabaseReaderServletOSException(final int errorCode, final String message)
    {
        super(message);

        this.errorCode = errorCode;
    }
 
    public int getErrorCode()
    {
        return errorCode;
    }
	
}
