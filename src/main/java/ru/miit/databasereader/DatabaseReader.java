package ru.miit.databasereader;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;

import oracle.jdbc.OraclePreparedStatement;
import ru.miit.cache.Cache;

public interface DatabaseReader {

	Connection getConnection() throws OracleDatabaseReaderConnectionException, NamingException;

	String getCodeData(final int webMetaId) throws OracleDatabaseReaderException;

	void getResTestListData(PrintWriter printWriter) throws OracleDatabaseReaderException;

	void getBinaryDataByMetaId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, final boolean cacheIsUp, String idInCache)
			throws OracleDatabaseReaderException, OracleDatabaseReaderServletOSException;

	void getBinaryDataByFileVersionId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, final boolean cacheIsUp, String idInCache)
			throws OracleDatabaseReaderException, OracleDatabaseReaderServletOSException;

	void getBinaryDataByClientId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, final boolean cacheIsUp, String idInCache)
			throws OracleDatabaseReaderException, OracleDatabaseReaderServletOSException;

	void setParameterInt(OraclePreparedStatement preparedStatement, String filed, Object value) throws SQLException;

	void setParameterStr(OraclePreparedStatement preparedStatement, String filed, Object value) throws SQLException;

	void writeToStream(Blob blobData, OutputStream os) throws OracleDatabaseReaderServletOSException;

	void writeToTwoStreams(Blob blobData, OutputStream os1, FileOutputStream os2)
			throws OracleDatabaseReaderServletOSException;
	
	public void fetchDataFromResultSet(ResultSet resultSet, OutputStream osServlet,
			HttpServletResponse response, Cache cache, final boolean cacheIsUp, String idInCache) throws SQLException, OracleDatabaseReaderServletOSException, OracleDatabaseReaderException ;
}
