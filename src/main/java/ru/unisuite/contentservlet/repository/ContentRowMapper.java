package ru.unisuite.contentservlet.repository;

import ru.unisuite.contentservlet.model.Content;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContentRowMapper {
    public Content mapRow(ResultSet rs) throws SQLException {
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
}
