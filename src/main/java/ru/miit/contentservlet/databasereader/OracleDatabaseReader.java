package ru.miit.contentservlet.databasereader;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import ru.miit.cache.Cache;
import ru.miit.contentservlet.ContentLogger;
import ru.miit.contentservlet.ContentServlet;

public class OracleDatabaseReader implements DatabaseReader {
	
	private final Logger loggerDatabaseReader = ContentLogger.getLogger(OracleDatabaseReader.class.getName());

	private static final String DATASOURCE_NAME = "java:comp/env/jdbc/ds_basic";
	
	@Override
	public DataSource getDataSource() { //Обрабатывать местно

		Context initialContext = null;
		try {
			initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup(DATASOURCE_NAME);
			return dataSource;
		} catch (NamingException e) {
			loggerDatabaseReader.log(Level.SEVERE, e.toString());
			return null;
		} finally {
			if (initialContext != null) {
				try {
					initialContext.close();
				} catch (NamingException e) {
					loggerDatabaseReader.log(Level.WARNING, "InitialContext wasn't closed. " + e.toString());
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

			setParameterInt(preparedStatement, 1, String.valueOf(webMetaId));

			try (ResultSet resultSet = (ResultSet) preparedStatement.executeQuery()) {
				resultSet.next();
				codeData = resultSet.getString(DatabaseReaderParamName.rstr);
			}

		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage());
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
			throw new OracleDatabaseReaderException(e.getMessage());
		}
	}
	
	@Override
	public void getBinaryDataByMetaId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache) throws OracleDatabaseReaderException {
		
		final String getBinaryDataByMetaIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_fp_wp.ImgScaleAsSet(Aid_web_metaterm => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection
						.prepareStatement(getBinaryDataByMetaIdSQL)) {

			Object webMetaId = queryParameters.get(DatabaseReaderParamName.webMetaId);
			Object width = queryParameters.get(DatabaseReaderParamName.width);
			Object height = queryParameters.get(DatabaseReaderParamName.height);

			int i = 1;
			setParameterInt(preparedStatement, i++, webMetaId);
			setParameterStr(preparedStatement, i++, width);
			setParameterStr(preparedStatement, i++, height);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				fetchDataFromResultSet(resultSet, osServlet, response, cache, idInCache);

			}
		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage());
		}

	}

	@Override
	public void getBinaryDataByFileVersionId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache) throws OracleDatabaseReaderException {

		final String getBinaryDataByFileVersionIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_cm_kis_wp.ImgVFScaleAsSet(Aid_version_file => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";
		
		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection
						.prepareStatement(getBinaryDataByFileVersionIdSQL)) {

			Object fileVersionId = queryParameters.get(DatabaseReaderParamName.fileVersionId);
			Object width = queryParameters.get(DatabaseReaderParamName.width);
			Object height = queryParameters.get(DatabaseReaderParamName.height);

			int i = 1;
			setParameterInt(preparedStatement, i++, fileVersionId);
			setParameterStr(preparedStatement, i++, width);
			setParameterStr(preparedStatement, i++, height);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				fetchDataFromResultSet(resultSet, osServlet, response, cache, idInCache);

			}
		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage());
		}

	}

	@Override
	public void getBinaryDataByClientId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache) throws OracleDatabaseReaderException {

		final String getBinaryDataByClientIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_cm_kis_wp.PhotoScaleAsSet(Aid_e => ?, Aid_photo_album => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";
		
		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection
						.prepareStatement(getBinaryDataByClientIdSQL)) {

			Object clientId = queryParameters.get(DatabaseReaderParamName.clientId);
			Object entryIdInPhotoalbum = queryParameters.get(DatabaseReaderParamName.entryIdInPhotoalbum);
			Object width = queryParameters.get(DatabaseReaderParamName.width);
			Object height = queryParameters.get(DatabaseReaderParamName.height);

			int i = 1;
			setParameterInt(preparedStatement, i++, clientId);
			setParameterInt(preparedStatement, i++, entryIdInPhotoalbum);
			setParameterStr(preparedStatement, i++, width);
			setParameterStr(preparedStatement, i++, height);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				fetchDataFromResultSet(resultSet, osServlet, response, cache, idInCache);

			}
		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage());
		}

	}

	@Override
	public void setParameterInt(PreparedStatement preparedStatement, int filed, Object value) throws SQLException {
		if (value == null) {
			preparedStatement.setNull(filed, 0);
		} else {
			preparedStatement.setInt(filed, Integer.parseInt(value.toString()));
		}
	}

	@Override
	public void setParameterStr(PreparedStatement preparedStatement, int filed, Object value) throws SQLException {
		if (value == null) {
			preparedStatement.setNull(filed, 0);
		} else {
			preparedStatement.setString(filed, value.toString());
		}
	}

	@Override
	public void writeToStream(Blob blobData, OutputStream os) throws OracleDatabaseReaderServletOSException {

		int length;
		int bufSize = 4096;
		byte buffer[] = new byte[bufSize];
		try (InputStream is = blobData.getBinaryStream()) {
			while ((length = is.read(buffer, 0, bufSize)) != -1) {
				os.write(buffer, 0, length);
			}
			os.flush();
		} catch (IOException | SQLException e) {
			throw new OracleDatabaseReaderServletOSException(e.getMessage());
		}

	}

	@Override
	public synchronized void writeToTwoStreams(Blob blobData, OutputStream os1, FileOutputStream os2)
			throws OracleDatabaseReaderServletOSException {

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
			throw new OracleDatabaseReaderServletOSException(e.getMessage());
		}

	}

	@Override
	public void fetchDataFromResultSet(ResultSet resultSet, OutputStream osServlet, HttpServletResponse response,
			Cache cache, String idInCache)
			throws SQLException, OracleDatabaseReaderServletOSException, OracleDatabaseReaderException {
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
			try (FileOutputStream cacheOs = cache.getFileOutputStream(mimeType, idInCache)) {
				cache.putAsync(idInCache, parameters);
				cache.writeToTwoStreams(idInCache, blobObject, osServlet, cacheOs);

			} catch (IOException e) {
				throw new OracleDatabaseReaderException(e.getMessage());
			}

		} else {
			try {
				writeToStream(blobObject, osServlet);
			} catch (OracleDatabaseReaderServletOSException e) {
				throw new OracleDatabaseReaderException(e.getMessage());
			}
		}

	}

}
