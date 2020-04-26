package ru.unisuite.contentservlet.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.unisuite.contentservlet.exception.DataAccessException;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.Content;

import javax.sql.DataSource;
import java.sql.*;
import java.util.function.Supplier;

public class ContentRepositoryImpl implements ContentRepository {
    private final Logger logger = LoggerFactory.getLogger(ContentRepositoryImpl.class.getName());

    private final DataSource dataSource;
    private final ContentRowMapper contentRowMapper;

    public ContentRepositoryImpl(DataSource dataSource, ContentRowMapper contentRowMapper) {
        this.dataSource = dataSource;
        this.contentRowMapper = contentRowMapper;
    }


    @Override
    public Content getContentByIdWebMetaterm(Long idWebMetaterm, Integer width, Integer height) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareContentByMetatermStatement(conn, idWebMetaterm, null, width, height)) {
            return getContentInternal(stmt, () -> "idWebMetaterm=" + idWebMetaterm);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Content getContentByMetatermAlias(String metatermAlias, Integer width, Integer height) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareContentByMetatermStatement(conn, null, metatermAlias, width, height)) {
            return getContentInternal(stmt, () -> "metatermAlias=" + metatermAlias);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Content getContentByIdFe(Long idFe, Long idPhotoAlbum, Integer width, Integer height) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareContentByIdFeStatement(conn, idFe, idPhotoAlbum, width, height)) {
            return getContentInternal(stmt, () -> "idFe=" + idFe + ", idPhotoAlbum=" + idPhotoAlbum);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Content getContentByIdFileVersion(Long idFileVersion, Integer width, Integer height) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareContentByIdFileVersion(conn, idFileVersion, width, height)) {
            return getContentInternal(stmt, () -> "idFileVersion=" + idFileVersion);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private Content getContentInternal(PreparedStatement stmt, Supplier<String> parametersStringSupplier) {
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.isBeforeFirst()) {
                throw new NotFoundException("Content not found by {" + parametersStringSupplier.get() + '}');
            }
            rs.next();
            return contentRowMapper.mapRow(rs);
        } catch (SQLException e) {
            throw new DataAccessException("Could not get Content by {" + parametersStringSupplier.get() + '}', e);
        }
    }


    @Override
    public int getDefaultImageQuality() {
        return 80; // because why not??
    }


    private PreparedStatement prepareContentByMetatermStatement(Connection conn, Long idWebMetaterm, String metatermAlias, Integer width, Integer height) throws SQLException {
        String query = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension " +
                "from TABLE(cast(wpms_fp_wp.ImgScaleAsSet(Aid_web_metaterm => ?, A_alias => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setLong(1, idWebMetaterm);
        stmt.setString(2, metatermAlias);
        setIntPreparedStatementParameter(stmt, 3, width);
        setIntPreparedStatementParameter(stmt, 4, height);
        return stmt;
    }

    private PreparedStatement prepareContentByIdFeStatement(Connection conn, Long idFe, Long idPhotoAlbum, Integer width, Integer height) throws SQLException {
        String query = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension " +
                "from TABLE(cast(wpms_cm_kis_wp.PhotoScaleAsSet(Aid_e => ?, Aid_photo_album => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setLong(1, idFe);
        stmt.setLong(2, idPhotoAlbum);
        setIntPreparedStatementParameter(stmt, 3, width);
        setIntPreparedStatementParameter(stmt, 4, height);
        return stmt;
    }

    private PreparedStatement prepareContentByIdFileVersion(Connection conn, Long idFileVersion, Integer width, Integer height) throws SQLException {
        String query = "select data_binary, bsize, cntsecond_last_modified, filename, mime, extension " +
                "from TABLE(cast(wpms_cm_kis_wp.ImgVFScaleAsSet(Aid_version_file => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setLong(1, idFileVersion);
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
}
