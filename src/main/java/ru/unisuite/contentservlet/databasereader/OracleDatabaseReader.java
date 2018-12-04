package ru.unisuite.contentservlet.databasereader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.unisuite.cache.Cache;
import ru.unisuite.contentservlet.ContentServlet;

public class OracleDatabaseReader implements DatabaseReader {
	
	public OracleDatabaseReader (String datasourceName) {
		
		this.datasourceName = datasourceName;
		
	}

	private final Logger logger = LoggerFactory.getLogger(OracleDatabaseReader.class.getName());

	private String datasourceName;

	@Override
	public DataSource getDataSource() {

		Context initialContext = null;
		try {
			initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup(datasourceName);
			return dataSource;
		} catch (NamingException e) {
			logger.error(e.toString(), e);
			return null;
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
	public String getCodeData(final int webMetaId) throws OracleDatabaseReaderException {

		final String getCodeDataSQL = "select wpms_cm_wp.get_ContentURL(cv.id_web_metaterm, null, 4) rStr from content_version_wp cv where cv.id_web_metaterm = ?";

		String codeData;

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(getCodeDataSQL)) {

			setParameterInt(preparedStatement, 1, webMetaId);

			try (ResultSet resultSet = (ResultSet) preparedStatement.executeQuery()) {
				resultSet.next();
				codeData = resultSet.getString(DatabaseReaderParamName.rstr);
			}

		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage(), e);
		}
		return codeData;
	}

	@Override
	public void getResTestListData(PrintWriter printWriter) throws OracleDatabaseReaderException {

		final String getResTestListDataSQL = "select wpms_cm_wp.get_ContentURL(t.id_web_metaterm, null, 2) cnt from actual_content_version_wp t\n";

		String data;

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection
						.prepareStatement(getResTestListDataSQL);
				ResultSet resultSet = (ResultSet) preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				data = resultSet.getString(DatabaseReaderParamName.cnt);
				if (data != null) {
					printWriter.println("<p>" + data + "</p>");
				}
			}

		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage(), e);
		}
	}

	@Override
	public void getBinaryDataByMeta(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache)
			throws OracleDatabaseReaderException, DatabaseReaderNoDataException {

		final String getBinaryDataByMetaSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_fp_wp.ImgScaleAsSet(Aid_web_metaterm => ?, AScaleWidth => ?, AScaleHeight => ?, A_alias => ?) as wpt_t_data_img_wp))";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection
						.prepareStatement(getBinaryDataByMetaSQL)) {

			int i = 1;

			setParameterInt(preparedStatement, i++, queryParameters.getWebMetaId());
			setParameterStr(preparedStatement, i++, queryParameters.getWidth());
			setParameterStr(preparedStatement, i++, queryParameters.getHeight());
			setParameterStr(preparedStatement, i++, queryParameters.getWebMetaAlias());
			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (!resultSet.isBeforeFirst()) {
					throw new DatabaseReaderNoDataException("No content found for these parameters. ");
				} else {
					fetchDataFromResultSet(resultSet, osServlet, response, cache, idInCache);
				}

			}
		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage(), e);
		}

	}

	@Override
	public void getBinaryDataByFileVersionId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache)
			throws OracleDatabaseReaderException, DatabaseReaderNoDataException {

		final String getBinaryDataByFileVersionIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_cm_kis_wp.ImgVFScaleAsSet(Aid_version_file => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection
						.prepareStatement(getBinaryDataByFileVersionIdSQL)) {

			int i = 1;
			setParameterInt(preparedStatement, i++, queryParameters.getFileVersionId());
			setParameterStr(preparedStatement, i++, queryParameters.getWidth());
			setParameterStr(preparedStatement, i++, queryParameters.getHeight());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (!resultSet.isBeforeFirst()) {
					throw new DatabaseReaderNoDataException("No content found for these parameters. ");
				} else {
					fetchDataFromResultSet(resultSet, osServlet, response, cache, idInCache);
				}

			}
		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage(), e);
		}

	}

	@Override
	public void getBinaryDataByClientId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache)
			throws OracleDatabaseReaderException, DatabaseReaderNoDataException {

		final String getBinaryDataByClientIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_cm_kis_wp.PhotoScaleAsSet(Aid_e => ?, Aid_photo_album => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection
						.prepareStatement(getBinaryDataByClientIdSQL)) {

			int i = 1;
			setParameterInt(preparedStatement, i++, queryParameters.getClientId());
			setParameterInt(preparedStatement, i++, queryParameters.getEntryIdInPhotoalbum());
			setParameterStr(preparedStatement, i++, queryParameters.getWidth());
			setParameterStr(preparedStatement, i++, queryParameters.getHeight());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (!resultSet.isBeforeFirst()) {
					throw new DatabaseReaderNoDataException("No content found for these parameters. ");
				} else {
					fetchDataFromResultSet(resultSet, osServlet, response, cache, idInCache);
				}

			}
		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage(), e);
		}

	}

	@Override
	public void setParameterInt(PreparedStatement preparedStatement, int field, Integer value) throws SQLException {
		if (value == null) {
			preparedStatement.setNull(field, 0);
		} else {
			preparedStatement.setInt(field, value);
		}
	}

	@Override
	public void setParameterStr(PreparedStatement preparedStatement, int field, String value) throws SQLException {
		if (value == null) {
			preparedStatement.setNull(field, 0);
		} else {
			preparedStatement.setString(field, value);
		}
	}

	@Override
	public void writeToStream(Blob blobData, OutputStream os) throws DatabaseReaderWriteToStreamException {

		int length;
		int bufSize = 4096;
		byte buffer[] = new byte[bufSize];
		try (InputStream is = blobData.getBinaryStream()) {
			while ((length = is.read(buffer, 0, bufSize)) != -1) {
				os.write(buffer, 0, length);
			}
			os.flush();
		} catch (IOException | SQLException e) {
			throw new DatabaseReaderWriteToStreamException(e.getMessage(), e);
		}

	}

	@Override
	public synchronized void writeToTwoStreams(Blob blobData, OutputStream os1, FileOutputStream os2)
			throws DatabaseReaderWriteToStreamException {

		int length;
		int bufSize = 4096;
		byte buffer[] = new byte[bufSize];
		try (InputStream is = blobData.getBinaryStream();
				FileChannel filechannel = os2.getChannel();
				FileLock lock = filechannel.lock(0, Long.MAX_VALUE, false)) {
			while ((length = is.read(buffer, 0, bufSize)) != -1) {
				os1.write(buffer, 0, length);
				os2.write(buffer, 0, length);
			}
			os1.flush();
			os2.flush();
		} catch (IOException | SQLException e) {
			throw new DatabaseReaderWriteToStreamException(e.getMessage(), e);
		}

	}

	@Override
	public void fetchDataFromResultSet(ResultSet resultSet, OutputStream osServlet, HttpServletResponse response,
			Cache cache, String idInCache) throws SQLException, OracleDatabaseReaderException {
		resultSet.next();

		int blobSize = resultSet.getInt(DatabaseReaderParamName.bsize);

		Long lastModifiedTime = resultSet.getLong(DatabaseReaderParamName.lastModified);

		String fileName = resultSet.getString(DatabaseReaderParamName.filename);

		String fileExtension = resultSet.getString(DatabaseReaderParamName.extension);

		String mimeType = resultSet.getString(DatabaseReaderParamName.mime);

		response.setContentType(mimeType);
		response.setContentLength(blobSize);
		response.setHeader("Last-Modified", lastModifiedTime.toString());

		Blob blobObject = resultSet.getBlob(DatabaseReaderParamName.dataBinary);

		if (ContentServlet.USE_CACHE && cache.isUp) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(DatabaseReaderParamName.contentType, mimeType);
			parameters.put(DatabaseReaderParamName.type, mimeType);
			parameters.put(DatabaseReaderParamName.size, blobSize);
			parameters.put(DatabaseReaderParamName.hash, "someHash");

			try (OutputStream isFromCache = cache.openStream(idInCache)) {
				
				if (isFromCache != null) {
					cache.writeToTwoStreams(idInCache, blobObject, osServlet, isFromCache);
					cache.putAsync(idInCache, parameters);
				} else {
					writeToStream(blobObject, osServlet);
				}
				
			} catch (IOException | DatabaseReaderWriteToStreamException e) {
				throw new OracleDatabaseReaderException(e.getMessage(), e);
			}
			
		} else {
			try {
				writeToStream(blobObject, osServlet);
			} catch (DatabaseReaderWriteToStreamException e) {
				throw new OracleDatabaseReaderException(e.getMessage(), e);
			}
		}

	}

}
