package ru.unisuite.contentservlet.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.config.ResizerType;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.imageresizer.ImageResizer;
import ru.unisuite.imageresizer.ImageResizerFactory;
import ru.unisuite.scf4j.Cache;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ContentRepositoryImpl implements ContentRepository {
    private final Logger logger = LoggerFactory.getLogger(ContentRepositoryImpl.class.getName());

    private final DataSource dataSource;
    private final boolean persistentCacheEnabled;

    private static final ZoneId GMT = ZoneId.of("GMT");
    private static final DateTimeFormatter LAST_MODIFIED_FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(GMT);


    public ContentRepositoryImpl(DataSource dataSource, boolean persistentCacheEnabled) {
        this.dataSource = dataSource;
        this.persistentCacheEnabled = persistentCacheEnabled;
    }



    @Override
    public String getHtmlImgCode(long webMetaId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = prepareCodeDataStatement(connection, webMetaId);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepareCodeDataStatement(Connection connection, long idWebMetaterm) throws SQLException {
        String getCodeDataSQL = "select wpms_cm_wp.get_ContentURL(cv.id_web_metaterm, null, 4) " +
                "from content_version_wp cv " +
                "where cv.id_web_metaterm = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(getCodeDataSQL);
        preparedStatement.setLong(1, idWebMetaterm);
        return preparedStatement;
    }

    @Override
    public int getDefaultImageQuality() throws DatabaseReaderException, NotFoundException {

//		final String getDefaultImageQualitySQL = "select 80 imagequality from dual";
//
//		try (Connection connection = dataSource.getConnection();
//				PreparedStatement preparedStatement = connection.prepareStatement(getDefaultImageQualitySQL)) {
//
//			try (ResultSet resultSet = preparedStatement.executeQuery()) {
//
//				if (!resultSet.isBeforeFirst()) {
//					throw new NotFoundException("Image quality value in DB is empty. ");
//				} else {
//					resultSet.next();
//					return resultSet.getInt(DatabaseReaderParamName.imageQuality);
//				}
//			}
//		} catch (SQLException e) {
//			throw new DatabaseReaderException(e.getMessage(), e);
//		}
        return 80; // because why not??
    }


    public Content getContentByIdWebMetaterm(long idWebMetaterm) {
        return getContentByIdWebMetaterm(idWebMetaterm, null, null);
    }

    public Content getContentByIdWebMetaterm(long idWebMetaterm, Integer width, Integer height) {
        try {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = prepareContentByIdWebMetatermStatement(conn, idWebMetaterm, width, height);
                 ResultSet rs = stmt.executeQuery();
            ) {
                if (!rs.isBeforeFirst()) {
                    throw new NotFoundException("Content not found {idWebMetaterm=" + idWebMetaterm + '}');
                }
                rs.next();
                return mapResultSetToContent(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepareContentByIdWebMetatermStatement(Connection conn, long idWebMetaterm, Integer width, Integer height) throws SQLException {
        String query = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension " +
                "from TABLE(cast(wpms_fp_wp.ImgScaleAsSet(Aid_web_metaterm => ?, AScaleWidth => ?, AScaleHeight => ?, A_alias => null) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setLong(1, idWebMetaterm);
        setIntPreparedStatementParameter(stmt, 2, width);
        setIntPreparedStatementParameter(stmt, 3, height);
        return stmt;
    }

    private void setIntPreparedStatementParameter(PreparedStatement stmt, int parameterIndex, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(parameterIndex, value);
        } else {
            stmt.setNull(parameterIndex, Types.INTEGER);
        }
    }

    private Content mapResultSetToContent(ResultSet rs) throws SQLException {
        Content content = new Content();
        Blob blob = rs.getBlob("data_binary");
        content.setDataStream(blob.getBinaryStream());
        content.setSize(blob.length());
        content.setLastModified(rs.getLong("cntsecond_last_modified"));
        content.setMimeType(rs.getString("mime"));
        content.setFilename(rs.getString("filename"));
        content.setExtension(rs.getString("extension"));
        return content;
    }


    @Override
    public void getBinaryDataByMeta(DatabaseQueryParameters queryParameters, OutputStream osServlet,
                                    HttpServletResponse response, Cache persistantCache, String idInCache)
            throws DatabaseReaderException, NotFoundException {

        final String getBinaryDataByMetaSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension " +
                "from TABLE(cast(wpms_fp_wp.ImgScaleAsSet(Aid_web_metaterm => ?, AScaleWidth => ?, AScaleHeight => ?, A_alias => ?) as wpt_t_data_img_wp))";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getBinaryDataByMetaSQL)) {

            Object[] parametersArray = {queryParameters.getWebMetaId(), null, null, queryParameters.getWebMetaAlias()};

            try (ResultSet resultSet = executeWithParameters(preparedStatement, parametersArray)) {

                if (!resultSet.isBeforeFirst()) {
                    throw new NotFoundException("No content found for these parameters. ");
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
            throws DatabaseReaderException, NotFoundException {

        final String getBinaryDataByFileVersionIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension " +
                "from TABLE(cast(wpms_cm_kis_wp.ImgVFScaleAsSet(Aid_version_file => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getBinaryDataByFileVersionIdSQL)) {

            Object[] parameters = {queryParameters.getFileVersionId(), null, null};
            try (ResultSet resultSet = executeWithParameters(preparedStatement, parameters)) {
                if (!resultSet.isBeforeFirst()) {
                    throw new NotFoundException("No content found for these parameters. ");
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
            throws DatabaseReaderException, NotFoundException {

        final String getBinaryDataByClientIdSQL = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension " +
                "from TABLE(cast(wpms_cm_kis_wp.PhotoScaleAsSet(Aid_e => ?, Aid_photo_album => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getBinaryDataByClientIdSQL)) {

            Object[] parametersArray = {queryParameters.getIdFe(), queryParameters.getEntryIdInPhotoalbum(), null, null};

            try (ResultSet resultSet = executeWithParameters(preparedStatement, parametersArray)) {

                if (!resultSet.isBeforeFirst()) {
                    throw new NotFoundException("No content found for these parameters. ");
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
            preparedStatement.setObject(i + 1, parametersArray[i]);
        }
        return preparedStatement.executeQuery();
    }

    private void writeToStream(InputStream is, OutputStream os) throws DatabaseReaderWriteToStreamException {

        int length;
        int bufSize = 4096;
        byte[] buffer = new byte[bufSize];
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
            throws SQLException, NotFoundException, DatabaseReaderException {

        resultSet.next();

        int blobSize = resultSet.getInt(DatabaseReaderParamName.bsize);

        if (blobSize == 0) {
            throw new NotFoundException("ContentLength is empty. ");
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
        if (persistentCacheEnabled && persistantCache.connectionIsUp()) {
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
