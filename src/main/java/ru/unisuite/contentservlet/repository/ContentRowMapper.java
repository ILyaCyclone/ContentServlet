package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.model.Content;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContentRowMapper {
    Content mapRow(ResultSet rs) throws SQLException {
        return mapRow(rs, ColumnSet.WITH_HASH);
    }

    Content mapRowWithoutHash(ResultSet rs) throws SQLException {
        return mapRow(rs, ColumnSet.WITHOUT_HASH);
    }

    private Content mapRow(ResultSet rs, ColumnSet columnSet) throws SQLException {
        Content content = new Content();
        Blob blob = rs.getBlob("data_binary");
        content.setDataStream(blob.getBinaryStream());
        content.setSize(blob.length());
        content.setLastModified(rs.getLong("cntsecond_last_modified"));
        if(columnSet == ColumnSet.WITH_HASH) {
            content.setHash(rs.getString("hash"));
        }
        content.setMimeType(rs.getString("mime"));
        content.setFilename(rs.getString("filename"));
        content.setExtension(rs.getString("extension"));
        return content;
    }

    private enum ColumnSet {
        WITH_HASH, WITHOUT_HASH
    }
}
