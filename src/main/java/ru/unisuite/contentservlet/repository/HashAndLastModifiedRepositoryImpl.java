package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.exception.DataAccessException;
import ru.unisuite.contentservlet.exception.NotFoundException;
import ru.unisuite.contentservlet.model.HashAndLastModified;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

public class HashAndLastModifiedRepositoryImpl implements HashAndLastModifiedRepository {

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
            throw new DataAccessException("Could not get HashAndLastModified by {" + parametersStringSupplier.get() + '}', e);
        }
    }

    @Override
    public HashAndLastModified getByMetatermAlias(String metatermAlias) {
        Supplier<String> parametersStringSupplier = () -> "metatermAlias=" + metatermAlias;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = prepareByMetatermAliasStatement(conn, metatermAlias)) {
            return getInternal(stmt, parametersStringSupplier);
        } catch (SQLException e) {
            throw new DataAccessException("Could not get HashAndLastModified by {" + parametersStringSupplier.get() + '}', e);
        }
    }

    @Override
    public HashAndLastModified getByIdFe(Long idFe, Long entryIdInPhotoalbum) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HashAndLastModified getByIdFileVersion(Long fileVersionId) {
        throw new UnsupportedOperationException();
    }


    private HashAndLastModified getInternal(PreparedStatement stmt, Supplier<String> parametersStringSupplier) {
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.isBeforeFirst()) {
                throw new NotFoundException("HashAndLastModified not found by {" + parametersStringSupplier.get() + '}');
            }
            rs.next();
            return rowMapper.mapRow(rs);
        } catch (SQLException e) {
            throw new DataAccessException("Could not get HashAndLastModified by {" + parametersStringSupplier.get() + '}', e);
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
}
