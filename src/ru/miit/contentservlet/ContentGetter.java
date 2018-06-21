package ru.miit.contentservlet;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.miit.cache.Cache;
import ru.miit.cacheexception.CacheGetException;
import ru.miit.databasereader.DatabaseReader;
import ru.miit.databasereader.OracleDatabaseReader;
import ru.miit.databasereader.OracleDatabaseReaderException;

public class ContentGetter {
	
	private final Logger loggerContentGetter = ContentLogger.getLogger(ContentGetter.class.getName());

	public static String mask = "[^/]*[^/]";
	
	public static String getFileName(final String AURI) {
		Pattern pattern = Pattern.compile(mask);
		Matcher matcher = pattern.matcher(AURI);
		String rS = null;
		while (matcher.find()) {
			rS = matcher.group(matcher.groupCount());
		}
		return rS;
	}

	public void getObject(final RequestParameters requestParameters, OutputStream os, HttpServletResponse response,
			Cache cache) throws CacheGetException, OracleDatabaseReaderException {
		
		// Создание id объекта в кэше
		NameCreator nameCreator = new NameCreator();
		String idInCache = nameCreator.createWithParameters(requestParameters);

		if (requestParameters.getWebMetaId() == null && requestParameters.getFileVersionId() == null
				&& requestParameters.getClientId() == null && requestParameters.getEntryIdInPhotoalbum() == null) {

			loggerContentGetter.log(Level.WARNING, "Id of requested object does not contains required parameters. ");
			throw new IllegalArgumentException("Id of requested object does not contains required parameters. ");

		} else {

			boolean foundInCache = ContentServlet.USE_CACHE && cache.isUp && cache.exists(idInCache)
					&& cache.getHashById(idInCache).equals("someHash");

			if (foundInCache) {
				cache.get(idInCache, os, response);
				// увеличение попаданий в кэш
				cache.increaseHits();

			} else {

				Map<String, Object> queryParameters = new HashMap<>();

				DatabaseReader databaseReader = new OracleDatabaseReader();

				if (requestParameters.webMetaId != null) {

					queryParameters.put(RequestParameters.webMetaIdParamName, requestParameters.getWebMetaId());
					queryParameters.put(RequestParameters.widthParamName, requestParameters.getWidth());
					queryParameters.put(RequestParameters.heightParamName, requestParameters.getHeight());
					
					databaseReader.getBinaryDataByMetaId(queryParameters, os, response, cache, idInCache);

				} else {
					if (requestParameters.fileVersionId != null) {

						queryParameters.put(RequestParameters.fileVersionIdParamName, requestParameters.getFileVersionId());
						queryParameters.put(RequestParameters.widthParamName, requestParameters.getWidth());
						queryParameters.put(RequestParameters.heightParamName, requestParameters.getHeight());

						databaseReader.getBinaryDataByFileVersionId(queryParameters, os, response, cache, idInCache);

					} else {

						if (requestParameters.clientId != null || requestParameters.entryIdInPhotoalbum != null) {

							queryParameters.put(RequestParameters.clientIdParamName, requestParameters.getClientId());
							queryParameters.put(RequestParameters.entryIdInPhotoalbumParamName,
									requestParameters.getEntryIdInPhotoalbum());
							queryParameters.put(RequestParameters.widthParamName, requestParameters.getWidth());
							queryParameters.put(RequestParameters.heightParamName, requestParameters.getHeight());

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

	public String getCodeData(final int WebMetaId) throws OracleDatabaseReaderException {

		String answer = null;

		DatabaseReader databaseReader = new OracleDatabaseReader();
		answer = databaseReader.getCodeData(WebMetaId);

		return answer;

	}

	public void getResTestListData(PrintWriter printWriter) throws OracleDatabaseReaderException {

		DatabaseReader databaseReader = new OracleDatabaseReader();

		databaseReader.getResTestListData(printWriter);

	}

}
