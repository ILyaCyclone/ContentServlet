package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.exception.DataAccessException;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.HashAndLastModified;

import javax.sql.DataSource;
import java.sql.*;
import java.util.function.Supplier;

public class HashAndLastModifiedRepositoryImpl implements HashAndLastModifiedRepository {
    private static final String SELECT_COLUMNS = "hash, cntsecond_last_modified as last_modified_seconds";

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
             PreparedStatement stmt = prepareContentByMetatermStatement(conn, idWebMetaterm, null)) {
            return getInternal(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }

    @Override
    public HashAndLastModified getByMetatermAlias(String metatermAlias) {
        Supplier<String> parametersStringSupplier = () -> "metatermAlias=" + metatermAlias;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareContentByMetatermStatement(conn, null, metatermAlias)) {
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
            return getInternal(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }

    @Override
    public HashAndLastModified getByIdPropose(long idPropose) {
        Supplier<String> parametersStringSupplier = () -> "idPropose=" + idPropose;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareByIdPropose(conn, idPropose)) {
            return getInternal(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }

    @Override
    public HashAndLastModified getByIdFileVersion(Long fileVersionId) {
        Supplier<String> parametersStringSupplier = () -> "fileVersionId=" + fileVersionId;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareByIdFileVersion(conn, fileVersionId)) {
            return getInternal(stmt, parametersStringSupplier);
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


    @SuppressWarnings("java:S2095") // suppress SonarLint unclosed PreparedStatement
    private PreparedStatement prepareContentByMetatermStatement(Connection conn, Long idWebMetaterm, String metatermAlias) throws SQLException {
        String query = "select " + SELECT_COLUMNS + " from TABLE(cast(" +
                "wpms_fp_wp.ImgScaleAsSet(Aid_web_metaterm => ?, A_alias => ?, AScaleWidth => null, AScaleHeight => null) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setObject(1, idWebMetaterm, Types.BIGINT);
        stmt.setString(2, metatermAlias);
        return stmt;
    }

    @SuppressWarnings("java:S2095") // suppress SonarLint unclosed PreparedStatement
    private PreparedStatement prepareByIdFeStatement(Connection conn, Long idFe, Long idPhotoAlbum) throws SQLException {
        String query = "select " + SELECT_COLUMNS + " from TABLE(cast(" +
                "wpms_cm_kis_wp.PhotoScaleAsSet(Aid_e => ?, Aid_photo_album => ?, AScaleWidth => null, AScaleHeight => null) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setObject(1, idFe, Types.BIGINT);
        stmt.setObject(2, idPhotoAlbum, Types.BIGINT);
        return stmt;
    }

    @SuppressWarnings("java:S2095") // suppress SonarLint unclosed PreparedStatement
    private PreparedStatement prepareByIdPropose(Connection conn, long idPropose) throws SQLException {
        String query = "select " + SELECT_COLUMNS + " from TABLE(cast(" +
                "wpms_cm_kis_wp.Photo_Propose_Fit(A_id_pa_propose => ?, AScaleWidth => null, AScaleHeight => null) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setLong(1, idPropose);
        return stmt;
    }

    @SuppressWarnings("java:S2095") // suppress SonarLint unclosed PreparedStatement
    private PreparedStatement prepareByIdFileVersion(Connection conn, Long idFileVersion) throws SQLException {
        String query = "select " + SELECT_COLUMNS + " from TABLE(cast(" +
                "wpms_cm_kis_wp.ImgVFScaleAsSet(Aid_version_file => ?, AScaleWidth => null, AScaleHeight => null) as wpt_t_data_img_wp))";

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
