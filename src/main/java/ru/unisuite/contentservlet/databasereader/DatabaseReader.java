package ru.unisuite.contentservlet.databasereader;

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

import ru.unisuite.cache.Cache;

public interface DatabaseReader {

	DataSource getDataSource();

	String getCodeData(final int webMetaId) throws DatabaseReaderException;

	void getResTestListData(PrintWriter printWriter) throws DatabaseReaderException;

	void getBinaryDataByMeta(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException;

	void getBinaryDataByFileVersionId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException;

	void getBinaryDataByClientId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException;

	public void setParameterInt(PreparedStatement preparedStatement, int filed, Integer value) throws SQLException;

	public void setParameterStr(PreparedStatement preparedStatement, int filed, String value) throws SQLException;

	void writeToStream(Blob blobData, OutputStream os) throws DatabaseReaderWriteToStreamException;

	void writeToTwoStreams(Blob blobData, OutputStream os1, FileOutputStream os2)
			throws DatabaseReaderWriteToStreamException;

	public void fetchDataFromResultSet(ResultSet resultSet, OutputStream osServlet, HttpServletResponse response,
			Cache cache, String idInCache)
			throws SQLException, DatabaseReaderException;
}
