package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.exception.DataAccessException;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.Content;

import javax.sql.DataSource;
import java.sql.*;
import java.util.function.Supplier;

public class ContentRepositoryImpl implements ContentRepository {
    private static final String SELECT_COLUMNS = "data_binary, bsize, hash, cntsecond_last_modified, filename, mime, extension";

    private static final String COULD_NOT_GET_MESSAGE_FORMAT = "Could not get content by {%s}";
    private static final String NOT_FOUND_MESSAGE_FORMAT = "Content not found by {%s}";

    private final DataSource dataSource;
    private final ContentRowMapper contentRowMapper;

    public ContentRepositoryImpl(DataSource dataSource, ContentRowMapper contentRowMapper) {
        this.dataSource = dataSource;
        this.contentRowMapper = contentRowMapper;
    }


    @Override
    public Content getContentByIdWebMetaterm(Long idWebMetaterm, Integer width, Integer height) {
        Supplier<String> parametersStringSupplier = () -> "idWebMetaterm=" + idWebMetaterm;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareContentByMetatermStatement(conn, idWebMetaterm, null, width, height)) {
            return getContentInternal(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }

    @Override
    public Content getContentByMetatermAlias(String metatermAlias, Integer width, Integer height) {
        Supplier<String> parametersStringSupplier = () -> "metatermAlias=" + metatermAlias;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareContentByMetatermStatement(conn, null, metatermAlias, width, height)) {
            return getContentInternal(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }

    @Override
    public Content getContentByIdFe(Long idFe, Long idPhotoAlbum, Integer width, Integer height) {
        Supplier<String> parametersStringSupplier = () -> "idFe=" + idFe + ", idPhotoAlbum=" + idPhotoAlbum;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareContentByIdFeStatement(conn, idFe, idPhotoAlbum, width, height)) {
            return getContentInternal(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }

    @Override
    public Content getContentByIdFileVersion(Long idFileVersion, Integer width, Integer height) {
        Supplier<String> parametersStringSupplier = () -> "idFileVersion=" + idFileVersion;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareContentByIdFileVersion(conn, idFileVersion, width, height)) {
            return getContentInternal(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }


    private Content getContentInternal(PreparedStatement stmt, Supplier<String> parametersStringSupplier) {
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.isBeforeFirst()) {
                throw notFoundException(parametersStringSupplier);
            }
            rs.next();
            return contentRowMapper.mapRow(rs);
        } catch (SQLException e) {
            throw couldNotGetDataAccessException(parametersStringSupplier, e);
        }
    }


    @SuppressWarnings("java:S2095") // suppress SonarLint unclosed PreparedStatement
    private PreparedStatement prepareContentByMetatermStatement(Connection conn, Long idWebMetaterm, String metatermAlias, Integer width, Integer height) throws SQLException {
        String query = "select " + SELECT_COLUMNS +
                " from TABLE(cast(wpms_fp_wp.ImgScaleAsSet(Aid_web_metaterm => ?, A_alias => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setObject(1, idWebMetaterm, Types.BIGINT);
        stmt.setString(2, metatermAlias);
        stmt.setObject(3, width, Types.INTEGER);
        stmt.setObject(4, height, Types.INTEGER);
        return stmt;
    }

    @SuppressWarnings("java:S2095") // suppress SonarLint unclosed PreparedStatement
    private PreparedStatement prepareContentByIdFeStatement(Connection conn, Long idFe, Long idPhotoAlbum, Integer width, Integer height) throws SQLException {
        String query = "select " + SELECT_COLUMNS +
                " from TABLE(cast(wpms_cm_kis_wp.PhotoScaleAsSet(Aid_e => ?, Aid_photo_album => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setObject(1, idFe, Types.BIGINT);
        stmt.setObject(2, idPhotoAlbum, Types.BIGINT);
        stmt.setObject(3, width, Types.INTEGER);
        stmt.setObject(4, height, Types.INTEGER);
        return stmt;
    }

    @SuppressWarnings("java:S2095") // suppress SonarLint unclosed PreparedStatement
    private PreparedStatement prepareContentByIdFileVersion(Connection conn, Long idFileVersion, Integer width, Integer height) throws SQLException {
        String query = "select " + SELECT_COLUMNS +
                " from TABLE(cast(wpms_cm_kis_wp.ImgVFScaleAsSet(Aid_version_file => ?, AScaleWidth => ?, AScaleHeight => ?) as wpt_t_data_img_wp))";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setLong(1, idFileVersion);
        stmt.setObject(2, width, Types.INTEGER);
        stmt.setObject(3, height, Types.INTEGER);
        return stmt;
    }


    private DataAccessException couldNotGetDataAccessException(Supplier<String> parametersStringSupplier, SQLException e) {
        return new DataAccessException(String.format(COULD_NOT_GET_MESSAGE_FORMAT, parametersStringSupplier.get()), e);
    }

    private NotFoundException notFoundException(Supplier<String> parametersStringSupplier) {
        return new NotFoundException(String.format(NOT_FOUND_MESSAGE_FORMAT, parametersStringSupplier.get()));
    }
}
