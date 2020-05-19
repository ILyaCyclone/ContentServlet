package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.model.HashAndLastModified;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class HashAndLastModifiedRowMapper {

    HashAndLastModified mapRow(ResultSet rs) throws SQLException {
        return new HashAndLastModified(rs.getString("hash"), Instant.ofEpochMilli(rs.getTimestamp("last_modified").getTime()).getEpochSecond());
    }

    HashAndLastModified mapRowLastModifiedSeconds(ResultSet rs) throws SQLException {
        return new HashAndLastModified(null, rs.getLong("last_modified_seconds"));
    }
}
