package ru.miit.contentservlet.databasereader;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import ru.miit.cache.Cache;

public interface DatabaseReader {

	DataSource getDataSource();

	String getCodeData(final int webMetaId) throws OracleDatabaseReaderException;

	void getResTestListData(PrintWriter printWriter) throws OracleDatabaseReaderException;

	void getBinaryDataByMetaId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache)
			throws OracleDatabaseReaderException, OracleDatabaseReaderServletOSException;

	void getBinaryDataByFileVersionId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache)
			throws OracleDatabaseReaderException, OracleDatabaseReaderServletOSException;

	void getBinaryDataByClientId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache)
			throws OracleDatabaseReaderException, OracleDatabaseReaderServletOSException;

	public void setParameterInt(PreparedStatement preparedStatement, int filed, Object value) throws SQLException;

	public void setParameterStr(PreparedStatement preparedStatement, int filed, Object value) throws SQLException;

	void writeToStream(Blob blobData, OutputStream os) throws OracleDatabaseReaderServletOSException;

	void writeToTwoStreams(Blob blobData, OutputStream os1, FileOutputStream os2)
			throws OracleDatabaseReaderServletOSException;

	public void fetchDataFromResultSet(ResultSet resultSet, OutputStream osServlet, HttpServletResponse response,
			Cache cache, String idInCache)
			throws SQLException, OracleDatabaseReaderServletOSException, OracleDatabaseReaderException;
}
