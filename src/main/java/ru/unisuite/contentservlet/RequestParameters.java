package ru.unisuite.contentservlet;

import java.util.Map;

class RequestParameters {

	private Integer contentDisposition;
	private Integer contentType;

	private Integer webMetaId;
	private String webMetaAlias;
	private Integer fileVersionId;
	private Integer clientId;
	private Integer entryIdInPhotoalbum;
	private Integer width;
	private Integer height;
	private boolean noCache;

	public static final String webMetaIdParamName = "webMetaId";
	public static final String webMetaAliasParamName = "webMetaAlias";
	public static final String fileVersionIdParamName = "fileVersionId";
	public static final String clientIdParamName = "clientId";
	public static final String entryIdInPhotoalbumParamName = "entryIdInPhotoalbum";
	public static final String widthParamName = "width";
	public static final String heightParamName = "height";

	public RequestParameters(Map<String, String[]> parametersMap) throws NumberFormatException {

		contentDisposition = getIntValue(parametersMap, ServletParamName.contentDisposition);
		contentType = getIntValue(parametersMap, ServletParamName.contentType);

		webMetaId = getIntValue(parametersMap, ServletParamName.webMetaId);
		webMetaAlias = getStringValue(parametersMap, ServletParamName.webMetaAlias);
		fileVersionId = getIntValue(parametersMap, ServletParamName.fileVersionId);
		clientId = getIntValue(parametersMap, ServletParamName.clientId);
		entryIdInPhotoalbum = getIntValue(parametersMap, ServletParamName.entryIdInPhotoalbum);
		width = getIntValue(parametersMap, ServletParamName.width);
		height = getIntValue(parametersMap, ServletParamName.height);

		noCache = parametersMap.containsKey(ServletParamName.cacheControl);

	}

	public Integer getContentDisposition() {
		return contentDisposition;
	}

	public Integer getContentType() {
		return contentType;
	}

	public boolean getCacheControl() {
		return noCache;
	}
	
	public String getWebMetaAlias() {
		return webMetaAlias;
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

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public String getStringValue(final Map<String, String[]> parametersMap, final String parameterName) {

		String value = null;
		if (parametersMap.containsKey(parameterName)) {
			value = parametersMap.get(parameterName)[0];
		}

		return value;
	}

	public Integer getIntValue(final Map<String, String[]> parametersMap, final String parameterName)
			throws NumberFormatException {

		Integer value = null;
		if (parametersMap.containsKey(parameterName)) {

			value = Integer.parseInt(parametersMap.get(parameterName)[0]);

		}

		return value;
	}
	
}
