package ru.unisuite.contentservlet;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.unisuite.cache.Cache;
import ru.unisuite.cache.cacheexception.CacheGetException;
import ru.unisuite.contentservlet.databasereader.DatabaseQueryParameters;
import ru.unisuite.contentservlet.databasereader.DatabaseReader;
import ru.unisuite.contentservlet.databasereader.DatabaseReaderException;
import ru.unisuite.contentservlet.databasereader.DatabaseReaderNoDataException;
import ru.unisuite.contentservlet.databasereader.OracleDatabaseReader;

public class ContentGetter {
	
	private final Logger logger = Logger.getLogger(ContentGetter.class.getName());
 
	private DatabaseReader databaseReader = new OracleDatabaseReader();
	
	private final static String MASK = "[^/]*[^/]";
	
	public static String getFileName(final String AURI) {
		Pattern pattern = Pattern.compile(MASK);
		Matcher matcher = pattern.matcher(AURI);
		String rS = null;
		while (matcher.find()) {
			rS = matcher.group(matcher.groupCount());
		}
		return rS;
	}

	public void getObject(final RequestParameters requestParameters, OutputStream os, HttpServletResponse response,
			Cache cache) throws CacheGetException, DatabaseReaderException, DatabaseReaderNoDataException {
		
		// Создание id объекта в кэше
		NameCreator nameCreator = new NameCreator();
		String idInCache = nameCreator.createWithParameters(requestParameters);

		if (requestParameters.getWebMetaId() == null && requestParameters.getWebMetaAlias() == null &&requestParameters.getFileVersionId() == null
				&& requestParameters.getClientId() == null && requestParameters.getEntryIdInPhotoalbum() == null) {

			logger.log(Level.WARNING, "Id of requested object does not contains required parameters. ");
			throw new IllegalArgumentException("Id of requested object does not contains required parameters. ");

		} else {

			boolean foundInCache = ContentServlet.USE_CACHE && cache.isUp && cache.exists(idInCache)
					&& cache.getHashById(idInCache).equals("someHash");

			if (foundInCache) {
				cache.get(idInCache, os, response);
				// увеличение попаданий в кэш
				cache.increaseHits();

			} else {
				
				DatabaseQueryParameters queryParameters = new DatabaseQueryParameters(requestParameters.getWebMetaId(), requestParameters.getWebMetaAlias(), requestParameters.getFileVersionId(), requestParameters.getClientId(), requestParameters.getEntryIdInPhotoalbum(), requestParameters.getWidth(), requestParameters.getHeight());

				if (queryParameters.getWebMetaId() != null || requestParameters.getWebMetaAlias() != null) {
					
					databaseReader.getBinaryDataByMeta(queryParameters, os, response, cache, idInCache);

				} else {
					
					if (queryParameters.getFileVersionId() != null) {

						databaseReader.getBinaryDataByFileVersionId(queryParameters, os, response, cache, idInCache);

					} else {

						if (queryParameters.getClientId() != null || requestParameters.getEntryIdInPhotoalbum() != null) {

							databaseReader.getBinaryDataByClientId(queryParameters, os, response, cache, idInCache);

						}
					}
				}
				
				// увеличение промахов количество в кэш
				if (ContentServlet.USE_CACHE && cache.isUp)
					cache.increaseMisses();
			}

		}
	}

	public String getHeader(final HttpServletRequest request, final Integer contentDisposition) {

		String fileName = getFileName(request.getRequestURI());

		if (contentDisposition == null) {
			return "filename=" + fileName;
		} else {

			switch (contentDisposition) {
			case 1:
				return "attachment; filename=" + fileName;
			case 2:
				return "inline; filename=" + fileName;
			default:
				return "filename=" + fileName;
			}
		}

	}

	public String getCodeData(final int WebMetaId) throws DatabaseReaderException {

		String answer = null;

		DatabaseReader databaseReader = new OracleDatabaseReader();
		answer = databaseReader.getCodeData(WebMetaId);

		return answer;

	}

	public void getResTestListData(PrintWriter printWriter) throws DatabaseReaderException {

		DatabaseReader databaseReader = new OracleDatabaseReader();

		databaseReader.getResTestListData(printWriter);

	}

}
