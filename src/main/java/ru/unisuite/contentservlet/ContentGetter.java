package ru.unisuite.contentservlet;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.unisuite.contentservlet.databasereader.DatabaseQueryParameters;
import ru.unisuite.contentservlet.databasereader.DatabaseReader;
import ru.unisuite.contentservlet.databasereader.DatabaseReaderException;
import ru.unisuite.contentservlet.databasereader.DatabaseReaderNoDataException;
import ru.unisuite.contentservlet.databasereader.OracleDatabaseReader;
import ru.unisuite.scf4j.Cache;
import ru.unisuite.scf4j.exception.SCF4JCacheGetException;

public class ContentGetter {

	public ContentGetter(ContentServletProperties properties) {

		databaseReader = new OracleDatabaseReader(properties.getDatasourceName());
		cacheControl = properties.getCacheControl();
	}

	private DatabaseReader databaseReader;

	private String cacheControl;

	private final static String MASK = "[^/]*[^/]";

	private static String getFileName(final String AURI) {
		Pattern pattern = Pattern.compile(MASK);
		Matcher matcher = pattern.matcher(AURI);
		String rS = null;
		while (matcher.find()) {
			rS = matcher.group(matcher.groupCount());
		}
		return rS;
	}

	public void getObject(final RequestParameters requestParameters, OutputStream os, HttpServletResponse response,
			Cache persistantCache)
			throws SCF4JCacheGetException, DatabaseReaderException, DatabaseReaderNoDataException {

		// Создание id объекта в кэше
		NameCreator nameCreator = new NameCreator();
		String idInCache = nameCreator.createWithParameters(requestParameters);

		boolean foundInCache = ContentServlet.USE_CACHE && persistantCache.connectionIsUp()
				&& persistantCache.exists(idInCache) && persistantCache.get(idInCache, os, response);

		if (foundInCache) {
			// увеличение попаданий в кэш
			persistantCache.increaseHits();

		} else {

			DatabaseQueryParameters queryParameters = new DatabaseQueryParameters(requestParameters.getWebMetaId(),
					requestParameters.getWebMetaAlias(), requestParameters.getFileVersionId(),
					requestParameters.getClientId(), requestParameters.getEntryIdInPhotoalbum(),
					requestParameters.getWidth(), requestParameters.getHeight(), requestParameters.getQuality());

			if (queryParameters.getWebMetaId() != null || requestParameters.getWebMetaAlias() != null) {

				databaseReader.getBinaryDataByMeta(queryParameters, os, response, persistantCache, idInCache);

			} else {

				if (queryParameters.getFileVersionId() != null) {

					databaseReader.getBinaryDataByFileVersionId(queryParameters, os, response, persistantCache,
							idInCache);

				} else {

					if (queryParameters.getClientId() != null || requestParameters.getEntryIdInPhotoalbum() != null) {

						databaseReader.getBinaryDataByClientId(queryParameters, os, response, persistantCache,
								idInCache);

					}
				}
			}

			// увеличение промахов количество в кэш
			if (ContentServlet.USE_CACHE && persistantCache.connectionIsUp())
				persistantCache.increaseMisses();
		}

	}

	public String getContentDisposition(final HttpServletRequest request, final Integer contentDisposition) {

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

	public String getCacheControl(final boolean noCache) {

		String cacheControl;

		if (noCache) {
			cacheControl = "no-cache";
		} else {
			cacheControl = "max-age=" + this.cacheControl;
		}

		return cacheControl;
	}

	public String getCodeData(final int WebMetaId) throws DatabaseReaderException {

		return databaseReader.getCodeData(WebMetaId);

	}

	public void getResTestListData(PrintWriter printWriter) throws DatabaseReaderException {

		databaseReader.getResTestListData(printWriter);

	}

	public int getDefaultImageQuality() throws DatabaseReaderException, DatabaseReaderNoDataException {

		return databaseReader.getDefaultImageQuality();

	}

}
