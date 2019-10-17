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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bea.core.repackaged.springframework.beans.propertyeditors.ZoneIdEditor;

import ru.unisuite.contentservlet.ContentServlet;
import ru.unisuite.imageresizer.ImageResizer;
import ru.unisuite.imageresizer.ImageResizerFactory;
import ru.unisuite.scf4j.Cache;

public class OracleDatabaseReader implements DatabaseReader {
	
	public OracleDatabaseReader (String datasourceName) {
		
		this.datasourceName = datasourceName;
		this.imageResizerFactory = new ImageResizerFactory();
		
	}

	public Long lastTime;
	
	private final Logger logger = LoggerFactory.getLogger(OracleDatabaseReader.class.getName());
	
	private ImageResizerFactory imageResizerFactory;

	private String datasourceName;

	@Override
	public DataSource getDataSource() throws OracleDatabaseReaderException {

		Context initialContext = null;
		try {
			initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup(datasourceName);
			return dataSource;
		} catch (Exception e) {
			logger.error(e.toString(), e);
			throw new OracleDatabaseReaderException("Unable to lookup datasource by name", e);
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
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws OracleDatabaseReaderException, DatabaseReaderNoDataException {
		
		final String getBinaryDataByMetaSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_fp_wp.ImgScaleAsSet(Aid_web_metaterm => ?, AScaleWidth => ?, AScaleHeight => ?, A_alias => ?) as wpt_t_data_img_wp))";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection
						.prepareStatement(getBinaryDataByMetaSQL)) {

			int i = 1;

			setParameterInt(preparedStatement, i++, queryParameters.getWebMetaId());
			setParameterStr(preparedStatement, i++, null);
			setParameterStr(preparedStatement, i++, null);
			setParameterStr(preparedStatement, i++, queryParameters.getWebMetaAlias());
			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (!resultSet.isBeforeFirst()) {
					throw new DatabaseReaderNoDataException("No content found for these parameters. ");
				} else {
					fetchDataFromResultSet(resultSet, osServlet, response, persistantCache, idInCache, queryParameters.getWidth(), queryParameters.getHeight(), queryParameters.getQuality());
				}

			}
		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage(), e);
		}

	}

	@Override
	public void getBinaryDataByFileVersionId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws OracleDatabaseReaderException, DatabaseReaderNoDataException {

		final String getBinaryDataByFileVersionIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_cm_kis_wp.ImgVFScaleAsSet(Aid_version_file => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection
						.prepareStatement(getBinaryDataByFileVersionIdSQL)) {

			int i = 1;
			setParameterInt(preparedStatement, i++, queryParameters.getFileVersionId());
			setParameterStr(preparedStatement, i++, null);
			setParameterStr(preparedStatement, i++, null);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (!resultSet.isBeforeFirst()) {
					throw new DatabaseReaderNoDataException("No content found for these parameters. ");
				} else {
					fetchDataFromResultSet(resultSet, osServlet, response, persistantCache, idInCache, queryParameters.getWidth(), queryParameters.getHeight(), queryParameters.getQuality());
				}

			}
		} catch (SQLException e) {
			throw new OracleDatabaseReaderException(e.getMessage(), e);
		}

	}

	@Override
	public void getBinaryDataByClientId(DatabaseQueryParameters queryParameters, OutputStream osServlet,
			HttpServletResponse response, Cache persistantCache, String idInCache)
			throws OracleDatabaseReaderException, DatabaseReaderNoDataException {

		final String getBinaryDataByClientIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension from TABLE(cast(wpms_cm_kis_wp.PhotoScaleAsSet(Aid_e => ?, Aid_photo_album => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement preparedStatement = (PreparedStatement) connection
						.prepareStatement(getBinaryDataByClientIdSQL)) {

			int i = 1;
			setParameterInt(preparedStatement, i++, queryParameters.getClientId());
			setParameterInt(preparedStatement, i++, queryParameters.getEntryIdInPhotoalbum());
			setParameterStr(preparedStatement, i++, null);
			setParameterStr(preparedStatement, i++, null);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (!resultSet.isBeforeFirst()) {
					throw new DatabaseReaderNoDataException("No content found for these parameters. ");
				} else {
					fetchDataFromResultSet(resultSet, osServlet, response, persistantCache, idInCache, queryParameters.getWidth(), queryParameters.getHeight(), queryParameters.getQuality());
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
	public void writeToStream(InputStream is, OutputStream os) throws DatabaseReaderWriteToStreamException {

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
			Cache persistantCache, String idInCache, Integer width, Integer height, Integer quality) throws SQLException, OracleDatabaseReaderException, DatabaseReaderNoDataException {
		
		resultSet.next();

		int blobSize = resultSet.getInt(DatabaseReaderParamName.bsize);
		
		if (blobSize == 0) {
			throw new DatabaseReaderNoDataException("ContentLength is empty. ");
		}
		Long lastModifiedTime = resultSet.getLong(DatabaseReaderParamName.lastModified);

		String fileName = resultSet.getString(DatabaseReaderParamName.filename);

		String fileExtension = resultSet.getString(DatabaseReaderParamName.extension);

		String mimeType = resultSet.getString(DatabaseReaderParamName.mime);
		
		response.setContentType(mimeType);
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
	    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		response.setHeader("Last-Modified", sdf.format(new Date(lastModifiedTime * 1000)).toString());

		Blob blobObject = resultSet.getBlob(DatabaseReaderParamName.dataBinary);
		if (ContentServlet.USE_CACHE && persistantCache.connectionIsUp()) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(DatabaseReaderParamName.contentType, mimeType);
			parameters.put(DatabaseReaderParamName.type, mimeType);
			parameters.put(DatabaseReaderParamName.size, blobSize);
			parameters.put(DatabaseReaderParamName.hash, "someHash");

			try (OutputStream isFromCache = persistantCache.openStream(idInCache)) {
				
				if (isFromCache != null) {
					persistantCache.writeToTwoStreams(idInCache, blobObject, osServlet, isFromCache); //maybe better to use TeeOutputStream
					persistantCache.putAsync(idInCache, parameters);
				} else {
					try (InputStream blobIs = blobObject.getBinaryStream()) {
						response.setContentLengthLong(blobSize);
						writeToStream(blobIs, osServlet);
					}
					
				}
				
			} catch (IOException | DatabaseReaderWriteToStreamException e) {
				throw new OracleDatabaseReaderException(e.getMessage(), e);
			}

		} else {
			try (InputStream blobIs = blobObject.getBinaryStream()) {
				
				ImageResizer resizer = imageResizerFactory.getImageResizer();
				
				float floatQuality;
				if (quality != null && quality >= 0 && quality <= 100) {
					floatQuality = quality.floatValue() / 100;
				} else {
					floatQuality = 1;
				}
				
				if (width != null && height !=null) {
				
					resizer.resize(blobIs, width, height, osServlet, floatQuality);
				} else {
					if (width != null) {
						resizer.resizeByWidth(blobIs, width, osServlet, blobSize, floatQuality);
					} else {
						if (height != null) {
							resizer.resizeByHeight(blobIs, height, osServlet, floatQuality);
						} else {
							response.setContentLengthLong(blobSize);
							writeToStream(blobIs, osServlet);
						}
					}
				}
			} catch (IOException | DatabaseReaderWriteToStreamException e) {
				throw new OracleDatabaseReaderException(e.getMessage(), e);
			}
		}

	}

}
