package ru.unisuite.contentservlet.databasereader;

import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import ru.unisuite.scf4j.Cache;

public interface DatabaseReader {

	String getCodeData(final int webMetaId) throws DatabaseReaderException;

	void getResTestListData(PrintWriter printWriter) throws DatabaseReaderException;
	
	int getDefaultImageQuality() throws DatabaseReaderException, DatabaseReaderNoDataException;

	void getBinaryDataByMeta(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException;

	void getBinaryDataByFileVersionId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException;

	void getBinaryDataByClientId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException;
}
