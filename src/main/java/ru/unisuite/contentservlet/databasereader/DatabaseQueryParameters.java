package ru.unisuite.contentservlet.databasereader;

public class DatabaseQueryParameters {

	private Integer webMetaId;
	private String webMetaAlias;
	private Integer fileVersionId;
	private Integer clientId;
	private Integer entryIdInPhotoalbum;

	private Integer width;
	private Integer height;
	
	private int quality;

	public DatabaseQueryParameters(Integer webMetaId, String webMetaAlias, Integer fileVersionId, Integer clientId,
			Integer entryIdInPhotoalbum, Integer width, Integer height, int quality) {

		this.webMetaId = webMetaId;
		this.webMetaAlias = webMetaAlias;
		this.fileVersionId = fileVersionId;
		this.clientId = clientId;
		this.entryIdInPhotoalbum = entryIdInPhotoalbum;
		this.width = width;
		this.height = height;
		this.quality = quality;

	}

	public Integer getWebMetaId() {
		return webMetaId;
	}

	public String getWebMetaAlias() {
		return webMetaAlias;
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

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}
	
	public int getQuality() {
		return quality;
	}

}
