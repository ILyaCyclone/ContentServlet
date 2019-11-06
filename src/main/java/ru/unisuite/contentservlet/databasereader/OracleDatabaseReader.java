package ru.unisuite.contentservlet.databasereader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.unisuite.contentservlet.ContentServlet;
import ru.unisuite.imageresizer.ImageResizer;
import ru.unisuite.imageresizer.ImageResizerFactory;
import ru.unisuite.scf4j.Cache;

public class OracleDatabaseReader implements DatabaseReader {

	public OracleDatabaseReader(String datasourceName) {
		this.datasourceName = datasourceName;
	}

	private final Logger logger = LoggerFactory.getLogger(OracleDatabaseReader.class.getName());

	private String datasourceName;

	private static final ZoneId GMT = ZoneId.of("GMT");
	private static final DateTimeFormatter LAST_MODIFIED_FORMATTER = DateTimeFormatter
			.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(GMT);

	private DataSource getDataSource() throws DatabaseReaderException {

		Context initialContext = null;
		try {
			initialContext = new InitialContext();
			return (DataSource) initialContext.lookup(datasourceName);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			throw new DatabaseReaderException("Unable to lookup datasource by name", e);
		} finally {
			if (initialContext != null) {
				try {
					initialContext.close();
				} catch (NamingException e) {
					logger.warn("InitialContext wasn't closed. " + e.toString(), e);
				}
			}
		}
	}

	@Override
	public String getCodeData(final int webMetaId) throws DatabaseReaderException {

		final String getCodeDataSQL = "select wpms_cm_wp.get_ContentURL(cv.id_web_metaterm, null, 4) rStr from content_version_wp cv where cv.id_web_metaterm = ?";

		String codeData;

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(getCodeDataSQL)) {

			preparedStatement.setObject(1, webMetaId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				resultSet.next();
				codeData = resultSet.getString(DatabaseReaderParamName.rstr);
			}
		} catch (SQLException e) {
			throw new DatabaseReaderException(e.getMessage(), e);
		}
		return codeData;
	}

	@Override
	public void getResTestListData(PrintWriter printWriter) throws DatabaseReaderException {

		final String getResTestListDataSQL = "select wpms_cm_wp.get_ContentURL(t.id_web_metaterm, null, 2) cnt from actual_content_version_wp t\n";

		String data;

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(getResTestListDataSQL);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				data = resultSet.getString(DatabaseReaderParamName.cnt);
				if (data != null) {
					printWriter.println("<p>" + data + "</p>");
				}
			}
		} catch (SQLException e) {
			throw new DatabaseReaderException(e.getMessage(), e);
		}
	}

	@Override
	public int getDefaultImageQuality() throws DatabaseReaderException, DatabaseReaderNoDataException {

		final String getDefaultImageQualitySQL = "select 84 imagequality from dual";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(getDefaultImageQualitySQL)) {

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (!resultSet.isBeforeFirst()) {
					throw new DatabaseReaderNoDataException("Image quality value in DB is empty. ");
				} else {
					resultSet.next();
					return resultSet.getInt(DatabaseReaderParamName.imageQuality);
				}
			}
		} catch (SQLException e) {
			throw new DatabaseReaderException(e.getMessage(), e);
		}
	}

	@Override
	public void getBinaryDataByMeta(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException {

		final String getBinaryDataByMetaSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_fp_wp.ImgScaleAsSet(Aid_web_metaterm => ?, AScaleWidth => ?, AScaleHeight => ?, A_alias => ?) as wpt_t_data_img_wp))";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(getBinaryDataByMetaSQL)) {

			Object[] parametersArray = { queryParameters.getWebMetaId(), null, null, queryParameters.getWebMetaAlias() };
			
			try (ResultSet resultSet = executeWithParameters(preparedStatement, parametersArray)) {

				if (!resultSet.isBeforeFirst()) {
					throw new DatabaseReaderNoDataException("No content found for these parameters. ");
				} else {
					fetchDataFromResultSet(resultSet, osServlet, response, persistantCache, idInCache,
							queryParameters.getWidth(), queryParameters.getHeight(), queryParameters.getQuality());
				}
			}
		} catch (SQLException e) {
			throw new DatabaseReaderException(e.getMessage(), e);
		}
	}

	@Override
	public void getBinaryDataByFileVersionId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException {

		final String getBinaryDataByFileVersionIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_cm_kis_wp.ImgVFScaleAsSet(Aid_version_file => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(getBinaryDataByFileVersionIdSQL)) {

			Object[] parametersArray = { queryParameters.getFileVersionId(), null, null };
			
			try (ResultSet resultSet = executeWithParameters(preparedStatement, parametersArray)) {

				if (!resultSet.isBeforeFirst()) {
					throw new DatabaseReaderNoDataException("No content found for these parameters. ");
				} else {
					fetchDataFromResultSet(resultSet, osServlet, response, persistantCache, idInCache,
							queryParameters.getWidth(), queryParameters.getHeight(), queryParameters.getQuality());
				}
			}
		} catch (SQLException e) {
			throw new DatabaseReaderException(e.getMessage(), e);
		}
	}

	@Override
	public void getBinaryDataByClientId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws DatabaseReaderException, DatabaseReaderNoDataException {

		final String getBinaryDataByClientIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_cm_kis_wp.PhotoScaleAsSet(Aid_e => ?, Aid_photo_album => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(getBinaryDataByClientIdSQL)) {

			Object[] parametersArray = { queryParameters.getClientId(), queryParameters.getEntryIdInPhotoalbum(), null, null };
			
			try (ResultSet resultSet = executeWithParameters(preparedStatement, parametersArray)) {

				if (!resultSet.isBeforeFirst()) {
					throw new DatabaseReaderNoDataException("No content found for these parameters. ");
				} else {
					fetchDataFromResultSet(resultSet, osServlet, response, persistantCache, idInCache,
							queryParameters.getWidth(), queryParameters.getHeight(), queryParameters.getQuality());
				}
			}
		} catch (SQLException e) {
			throw new DatabaseReaderException(e.getMessage(), e);
		}
	}
	
	private ResultSet executeWithParameters(PreparedStatement preparedStatement, Object[] parametersArray) throws SQLException {
		
		for (int i = 0; i < parametersArray.length; i++) {
			preparedStatement.setObject(i+1, parametersArray[i]);
		}
		return preparedStatement.executeQuery();
	}

	private void writeToStream(InputStream is, OutputStream os) throws DatabaseReaderWriteToStreamException {

		int length;
		int bufSize = 4096;
		byte buffer[] = new byte[bufSize];
		try {
			while ((length = is.read(buffer, 0, bufSize)) != -1) {
				os.write(buffer, 0, length);
			}
			os.flush();
		} catch (IOException e) {
			throw new DatabaseReaderWriteToStreamException(e.getMessage(), e);
		}
	}

	private void fetchDataFromResultSet(ResultSet resultSet, OutputStream osServlet, HttpServletResponse response,
			Cache persistantCache, String idInCache, Integer width, Integer height, int quality)
			throws SQLException, DatabaseReaderNoDataException, DatabaseReaderException {

		resultSet.next();

		int blobSize = resultSet.getInt(DatabaseReaderParamName.bsize);

		if (blobSize == 0) {
			throw new DatabaseReaderNoDataException("ContentLength is empty. ");
		}

		// String fileName = resultSet.getString(DatabaseReaderParamName.filename);
		// String fileExtension =
		// resultSet.getString(DatabaseReaderParamName.extension);
		Long lastModifiedTime = resultSet.getLong(DatabaseReaderParamName.lastModified);
		String mimeType = resultSet.getString(DatabaseReaderParamName.mime);
		response.setContentType(mimeType);

		Instant instant = Instant.ofEpochSecond(lastModifiedTime);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, GMT);
		response.setHeader("Last-Modified", LAST_MODIFIED_FORMATTER.format(localDateTime));

		Blob blobObject = resultSet.getBlob(DatabaseReaderParamName.dataBinary);
		if (ContentServlet.USE_CACHE && persistantCache.connectionIsUp()) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(DatabaseReaderParamName.contentType, mimeType);
			parameters.put(DatabaseReaderParamName.type, mimeType);
			parameters.put(DatabaseReaderParamName.size, blobSize);
			parameters.put(DatabaseReaderParamName.hash, "someHash");

			try (OutputStream isFromCache = persistantCache.openStream(idInCache)) {

				if (isFromCache != null) {
					persistantCache.writeToTwoStreams(idInCache, blobObject, osServlet, isFromCache); // maybe better to
																										// use
																										// TeeOutputStream
					persistantCache.putAsync(idInCache, parameters);
				} else {
					try (InputStream blobIs = blobObject.getBinaryStream()) {
						response.setContentLengthLong(blobSize);
						writeToStream(blobIs, osServlet);
					}

				}

			} catch (IOException | DatabaseReaderWriteToStreamException e) {
				throw new DatabaseReaderException(e.getMessage(), e);
			}

		} else {
			try (InputStream blobIs = blobObject.getBinaryStream()) {

				ImageResizer resizer = ImageResizerFactory.getImageResizer();

				if (width != null && height != null) {

					resizer.resize(blobIs, width, height, osServlet, quality);
				} else {
					if (width != null) {
						resizer.resizeByWidth(blobIs, width, osServlet, quality);
					} else {
						if (height != null) {
							resizer.resizeByHeight(blobIs, height, osServlet, quality);
						} else {
							response.setContentLengthLong(blobSize);
							writeToStream(blobIs, osServlet);
						}
					}
				}
			} catch (IOException | DatabaseReaderWriteToStreamException e) {
				throw new DatabaseReaderException(e.getMessage(), e);
			}
		}

	}

}
