package ru.miit.databasereader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import ru.miit.cache.Cache;

public class OracleDatabaseReader implements DatabaseReader {

	private static final String DATASOURCE_NAME = "java:comp/env/jdbc/ds_basic";
	private static final String HikariDATASOURCE_NAME = "java:comp/env/jdbc/OracleHikari";

	@Override
	public Connection getOracleConnection() throws OracleDatabaseReaderConnectionException, NamingException {

		Context initialContext = null;
		try {
			initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup(DATASOURCE_NAME);
			Connection connection = (Connection) dataSource.getConnection();// .unwrap(OracleConnection.class);
			return connection;
		} catch (NamingException | SQLException e) {
			e.printStackTrace();
			throw new OracleDatabaseReaderConnectionException(e.getMessage());
		} finally {
			if (initialContext != null) {
				initialContext.close();

			}
		}

	}

	@Override
	public Connection getHikariConnection() throws OracleDatabaseReaderConnectionException, NamingException {

		Context initialContext = null;
		try {
			initialContext = new InitialContext();
			try (HikariDataSource dataSource = (HikariDataSource) initialContext.lookup(HikariDATASOURCE_NAME)) {

				Connection connection = (Connection) dataSource.getConnection();// .unwrap(OracleConnection.class);
				return connection;

			}
		} catch (NamingException | SQLException e) {
			e.printStackTrace();
			throw new OracleDatabaseReaderConnectionException(e.getMessage());
		} finally {
			if (initialContext != null) {
				initialContext.close();

			}
		}

	}

	private static final String getCodeDataSQL = "select wpms_cm_wp.get_ContentURL(cv.id_web_metaterm, null, 4) rStr from content_version_wp cv where cv.id_web_metaterm = :webMetaId ";

	@Override
	public String getCodeData(final int webMetaId) throws OracleDatabaseReaderException {

		String codeData;

		try (Connection connection = getOracleConnection();
				OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
				OraclePreparedStatement preparedStatement = (OraclePreparedStatement) oracleConnection
						.prepareStatement(getCodeDataSQL)) {

			setParameterInt(preparedStatement, "webMetaId", String.valueOf(webMetaId));

			try (ResultSet resultSet = (ResultSet) preparedStatement.executeQuery()) {
				resultSet.next();
				codeData = resultSet.getString(DatabaseReaderParamName.rstr);
			}

		} catch (SQLException | OracleDatabaseReaderConnectionException | NamingException e) {
			throw new OracleDatabaseReaderException(e.getMessage());
		}
		return codeData;
	}

	private static final String getResTestListDataSQL = "select wpms_cm_wp.get_ContentURL(t.id_web_metaterm, null, 2) cnt from actual_content_version_wp t\n";

	@Override
	public void getResTestListData(PrintWriter printWriter) throws OracleDatabaseReaderException {

		String data;

		try (Connection connection = getOracleConnection();
				OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
				OraclePreparedStatement preparedStatement = (OraclePreparedStatement) oracleConnection
						.prepareStatement(getResTestListDataSQL);
				OracleResultSet resultSet = (OracleResultSet) preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				data = resultSet.getString(DatabaseReaderParamName.cnt);
				if (data != null) {
					printWriter.println("<p>" + data + "</p>");
				}
			}

		} catch (OracleDatabaseReaderConnectionException | SQLException | NamingException e) {
			throw new OracleDatabaseReaderException(e.getMessage());
		}
	}

	private static final String getBinaryDataByMetaIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_fp_wp.ImgScaleAsSet(:webMetaId, :width, :height) as wpt_t_data_img_wp))";

	@Override
	public void getBinaryDataByMetaId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache) throws OracleDatabaseReaderException {

		try (Connection connection = getOracleConnection();
				OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
				OraclePreparedStatement preparedStatement = (OraclePreparedStatement) oracleConnection
						.prepareStatement(getBinaryDataByMetaIdSQL)) {

			Object webMetaId = queryParameters.get(DatabaseReaderParamName.webMetaId);
			Object width = queryParameters.get(DatabaseReaderParamName.width);
			Object height = queryParameters.get(DatabaseReaderParamName.height);

			setParameterInt(preparedStatement, "webMetaId", webMetaId);
			setParameterStr(preparedStatement, "width", width);
			setParameterStr(preparedStatement, "height", height);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				fetchDataFromResultSet(resultSet, osServlet, response, cache, idInCache);

			}
		} catch (SQLException | OracleDatabaseReaderConnectionException | NamingException e) {
			throw new OracleDatabaseReaderException(e.getMessage());
		}

	}

	private static final String getBinaryDataByFileVersionIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_cm_kis_wp.ImgVFScaleAsSet(:fileVersionId, :width, :height) as wpt_t_data_img_wp))";

	@Override
	public void getBinaryDataByFileVersionId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache) throws OracleDatabaseReaderException {

		try (Connection connection = getOracleConnection();
				OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
				OraclePreparedStatement preparedStatement = (OraclePreparedStatement) oracleConnection
						.prepareStatement(getBinaryDataByFileVersionIdSQL)) {

			Object fileVersionId = queryParameters.get(DatabaseReaderParamName.fileVersionId);
			Object width = queryParameters.get(DatabaseReaderParamName.width);
			Object height = queryParameters.get(DatabaseReaderParamName.height);

			setParameterInt(preparedStatement, "fileVersionId", fileVersionId);
			setParameterStr(preparedStatement, "width", width);
			setParameterStr(preparedStatement, "height", height);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				fetchDataFromResultSet(resultSet, osServlet, response, cache, idInCache);

			}
		} catch (SQLException | OracleDatabaseReaderConnectionException | NamingException e) {
			throw new OracleDatabaseReaderException(e.getMessage());
		}

	}

	private final static String getBinaryDataByClientIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_cm_kis_wp.PhotoScaleAsSet(:clientId, :entryIdInPhotoalbum, :width, :height) as wpt_t_data_img_wp))";

	@Override
	public void getBinaryDataByClientId(Map<String, Object> queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache cache, String idInCache) throws OracleDatabaseReaderException {

		try (Connection connection = getOracleConnection();
				OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
				OraclePreparedStatement preparedStatement = (OraclePreparedStatement) oracleConnection
						.prepareStatement(getBinaryDataByClientIdSQL)) {

			Object clientId = queryParameters.get(DatabaseReaderParamName.clientId);
			Object entryIdInPhotoalbum = queryParameters.get(DatabaseReaderParamName.entryIdInPhotoalbum);
			Object width = queryParameters.get(DatabaseReaderParamName.width);
			Object height = queryParameters.get(DatabaseReaderParamName.height);

			setParameterInt(preparedStatement, "clientId", clientId);
			setParameterInt(preparedStatement, "entryIdInPhotoalbum", entryIdInPhotoalbum);
			setParameterStr(preparedStatement, "width", width);
			setParameterStr(preparedStatement, "height", height);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				fetchDataFromResultSet(resultSet, osServlet, response, cache, idInCache);

			}
		} catch (SQLException | OracleDatabaseReaderConnectionException | NamingException e) {
			throw new OracleDatabaseReaderException(e.getMessage());
		}

	}

	@Override
	public void setParameterInt(OraclePreparedStatement preparedStatement, String filed, Object value)
			throws SQLException {
		if (value == null) {
			preparedStatement.setNullAtName(filed, 0);
		} else {
			preparedStatement.setIntAtName(filed, Integer.parseInt(value.toString()));
		}
	}

	@Override
	public void setParameterStr(OraclePreparedStatement preparedStatement, String filed, Object value)
			throws SQLException {
		if (value == null) {
			preparedStatement.setNullAtName(filed, 0);
		} else {
			preparedStatement.setStringAtName(filed, value.toString());
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

		Blob blobObject = resultSet.getBlob(DatabaseReaderParamName.dataBinary);

		if (cache.isUp) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(DatabaseReaderParamName.contentType, mimeType);
			parameters.put(DatabaseReaderParamName.type, mimeType);
			parameters.put(DatabaseReaderParamName.size, blobSize);
			parameters.put(DatabaseReaderParamName.hash, "someHash");
			try (FileOutputStream cacheOs = cache.getFileOutputStream(mimeType, idInCache)) {

				writeToTwoStreams(blobObject, osServlet, cacheOs);
				cache.putAsync(idInCache, parameters);

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
