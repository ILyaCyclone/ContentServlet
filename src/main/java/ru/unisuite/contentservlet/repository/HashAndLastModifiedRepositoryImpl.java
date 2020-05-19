package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.exception.DataAccessException;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.HashAndLastModified;

import javax.sql.DataSource;
import java.sql.*;
import java.util.function.Supplier;

public class HashAndLastModifiedRepositoryImpl implements HashAndLastModifiedRepository {
    private static final String COULD_NOT_GET_MESSAGE_FORMAT = "Could not get HashAndLastModified by {%s}";
    private static final String NOT_FOUND_MESSAGE_FORMAT = "HashAndLastModified not found by {%s}";

    private final DataSource dataSource;
    private final HashAndLastModifiedRowMapper rowMapper;

    public HashAndLastModifiedRepositoryImpl(DataSource dataSource, HashAndLastModifiedRowMapper hashAndLastModifiedRowMapper) {
        this.dataSource = dataSource;
        this.rowMapper = hashAndLastModifiedRowMapper;
    }

    @Override
    public HashAndLastModified getByIdWebMetaterm(Long idWebMetaterm) {
        Supplier<String> parametersStringSupplier = () -> "idWebMetaterm=" + idWebMetaterm;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareByIdWebMetatermStatement(conn, idWebMetaterm)) {
            return getInternal(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }

    @Override
    public HashAndLastModified getByMetatermAlias(String metatermAlias) {
        Supplier<String> parametersStringSupplier = () -> "metatermAlias=" + metatermAlias;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareByMetatermAliasStatement(conn, metatermAlias)) {
            return getInternal(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }

    @Override
    public HashAndLastModified getByIdFe(Long idFe, Long entryIdInPhotoalbum) {
        Supplier<String> parametersStringSupplier = () -> "idFe=" + idFe + ", entryIdInPhotoalbum=" + entryIdInPhotoalbum;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareByIdFeStatement(conn, idFe, entryIdInPhotoalbum)) {
            return getInternalLastModifiedSeconds(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }

    @Override
    public HashAndLastModified getByIdFileVersion(Long fileVersionId) {
        Supplier<String> parametersStringSupplier = () -> "fileVersionId=" + fileVersionId;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareByIdFileVersion(conn, fileVersionId)) {
            return getInternalLastModifiedSeconds(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }



    private HashAndLastModified getInternal(PreparedStatement stmt, Supplier<String> parametersStringSupplier) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.isBeforeFirst()) {
                throw notFoundException(parametersStringSupplier);
            }
            rs.next();
            return rowMapper.mapRow(rs);
        }
    }

    private HashAndLastModified getInternalLastModifiedSeconds(PreparedStatement stmt, Supplier<String> parametersStringSupplier) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.isBeforeFirst()) {
                throw notFoundException(parametersStringSupplier);
            }
            rs.next();
            return rowMapper.mapRowLastModifiedSeconds(rs);
        }
    }


    private PreparedStatement prepareByIdWebMetatermStatement(Connection conn, Long idWebMetaterm) throws SQLException {
        String query = "select lbd.hash_sh1 as hash, lbd.d_last as last_modified from large_binary_data_wp lbd, content_version_wp cv " +
                "where lbd.id_content_version = cv.id_content_version and cv.id_web_metaterm = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setLong(1, idWebMetaterm);
        return stmt;
    }

    private PreparedStatement prepareByMetatermAliasStatement(Connection conn, String metatermAlias) throws SQLException {
        String query = "select lbd.hash_sh1 as hash, lbd.d_last as last_modified from web_metaterm_wp wm, content_version_wp cv, large_binary_data_wp lbd " +
                "where cv.id_web_metaterm = wm.id_web_metaterm " +
                "and lbd.id_content_version = cv.id_content_version " +
                "and wm.alias =  = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, metatermAlias);
        return stmt;
    }

    private PreparedStatement prepareByIdFeStatement(Connection conn, Long idFe, Long idPhotoAlbum) throws SQLException {
        String query = "select cntsecond_last_modified as last_modified_seconds " +
                "from TABLE(cast(wpms_cm_kis_wp.PhotoScaleAsSet(Aid_e => ?, Aid_photo_album => ?) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setObject(1, idFe, Types.BIGINT);
        stmt.setObject(2, idPhotoAlbum, Types.BIGINT);
        return stmt;
    }

    private PreparedStatement prepareByIdFileVersion(Connection conn, Long idFileVersion) throws SQLException {
        String query = "select cntsecond_last_modified as last_modified_seconds " +
                "from TABLE(cast(wpms_cm_kis_wp.ImgVFScaleAsSet(Aid_version_file => ?) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setLong(1, idFileVersion);
        return stmt;
    }


    private DataAccessException couldNotGetDataAccessException(Supplier<String> parametersStringSupplier, SQLException e) {
        return new DataAccessException(String.format(COULD_NOT_GET_MESSAGE_FORMAT, parametersStringSupplier.get()), e);
    }

    private NotFoundException notFoundException(Supplier<String> parametersStringSupplier) {
        return new NotFoundException(String.format(NOT_FOUND_MESSAGE_FORMAT, parametersStringSupplier.get()));
    }
}
