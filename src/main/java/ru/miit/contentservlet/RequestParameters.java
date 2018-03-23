package ru.miit.contentservlet;

import java.util.Map;

public class RequestParameters {

	public Integer contentDisposition;
	public Integer contentType;
	
	public Integer webMetaId;
	public Integer fileVersionId;
	public Integer clientId;
	public Integer entryIdInPhotoalbum;
	public String width;
	public String height;
	
	public static final String webMetaIdParamName = "webMetaId";
	public static final String fileVersionIdParamName = "fileVersionId";
	public static final String clientIdParamName = "clientId";
	public static final String entryIdInPhotoalbumParamName = "entryIdInPhotoalbum";
	public static final String widthParamName = "width";
	public static final String heightParamName = "height";
	
	
	public RequestParameters(Map<String, String[]> parametersMap) throws NumberFormatException {

		contentDisposition = getIntValue(parametersMap, ServletParamName.contentDisposition);
		contentType = getIntValue(parametersMap, ServletParamName.contentType);
	
		webMetaId = getIntValue(parametersMap, ServletParamName.webMetaId);
		fileVersionId = getIntValue(parametersMap, ServletParamName.fileVersionId);
		clientId = getIntValue(parametersMap, ServletParamName.clientId);
		entryIdInPhotoalbum = getIntValue(parametersMap,ServletParamName.entryIdInPhotoalbum);
		width = getStringValue(parametersMap, ServletParamName.width);
		height = getStringValue(parametersMap, ServletParamName.height);

	}
	
	
	public Integer getContentDisposition() {
		return contentDisposition;
	}

	public Integer getContentType() {
		return contentType;
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
	
	public String getStringValue(final Map<String, String[]> parametersMap, final String parameterName) {

		String value = null;
		if (parametersMap.containsKey(parameterName)) {
			value = parametersMap.get(parameterName)[0];
		}

		return value;
	}

	public Integer getIntValue(final Map<String, String[]> parametersMap, final String parameterName) throws NumberFormatException {

		Integer value = null;
		if (parametersMap.containsKey(parameterName)) {
			value = Integer.parseInt(parametersMap.get(parameterName)[0]);
		}

		return value;
	}
}
