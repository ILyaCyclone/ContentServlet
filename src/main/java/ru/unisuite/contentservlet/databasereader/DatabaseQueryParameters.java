package ru.unisuite.contentservlet.databasereader;

public class DatabaseQueryParameters {

	private Integer webMetaId;
	private Integer fileVersionId;
	private Integer clientId;
	private Integer entryIdInPhotoalbum;

	private String width;
	private String height;

	public DatabaseQueryParameters(Integer webMetaId, Integer fileVersionId, Integer clientId,
			Integer entryIdInPhotoalbum, String width, String height) {

		this.webMetaId = webMetaId;
		this.fileVersionId = fileVersionId;
		this.clientId = clientId;
		this.entryIdInPhotoalbum = entryIdInPhotoalbum;
		this.width = width;
		this.height = height;

	}

	public Integer getWebMetaId() {
		return webMetaId;
	}

	public Integer getFileVersionId() {
		return fileVersionId;
	}

	public Integer getClientId() {
		return clientId;
	}

	public Integer getEntryIdInPhotoalbum() {
		return entryIdInPhotoalbum;
	}

	public String getWidth() {
		return width;
	}

	public String getHeight() {
		return height;
	}

}
