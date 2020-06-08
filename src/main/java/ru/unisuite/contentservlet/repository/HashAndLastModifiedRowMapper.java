package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.model.HashAndLastModified;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HashAndLastModifiedRowMapper {

    HashAndLastModified mapRowWithHash(ResultSet rs) throws SQLException {
        return new HashAndLastModified(rs.getString("hash"), rs.getLong("last_modified_seconds"));
    }

    HashAndLastModified mapRowWithoutHash(ResultSet rs) throws SQLException {
        return new HashAndLastModified(null, rs.getLong("last_modified_seconds"));
    }
}
