package ru.unisuite.contentservlet.databasereader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import ru.unisuite.scf4j.Cache;

public interface DatabaseReader {

	DataSource getDataSource() throws DatabaseReaderException;

	String getCodeData(final int webMetaId) throws DatabaseReaderException;

	void getResTestListData(PrintWriter printWriter) throws DatabaseReaderException;

	void getBinaryDataByMeta(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException;

	void getBinaryDataByFileVersionId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException;

	void getBinaryDataByClientId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException;

	public void setParameterInt(PreparedStatement preparedStatement, int filed, Integer value) throws SQLException;

	public void setParameterStr(PreparedStatement preparedStatement, int filed, String value) throws SQLException;

	void writeToStream(InputStream is, OutputStream os) throws DatabaseReaderWriteToStreamException;

	void writeToTwoStreams(Blob blobData, OutputStream os1, FileOutputStream os2)
			throws DatabaseReaderWriteToStreamException;

	public void fetchDataFromResultSet(ResultSet resultSet, OutputStream osServlet, HttpServletResponse response,
			Cache persistantCache, String idInCache, String width, String height) throws SQLException, DatabaseReaderException, DatabaseReaderNoDataException, IOException;
}
