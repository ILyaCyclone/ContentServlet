package ru.unisuite.contentservlet;

class ServletParamName {

	private ServletParamName() {
	}

	// Названия параметров из БД
	// ID web метатермина версии контента
	public static final String webMetaId = "id_wm";

	public static final String webMetaAlias = "alias_wm";
	/*
	 * Content-Disposition контента 1 - attachment 2 - inline null - null
	 */
	public static final String contentDisposition = "cd";
	/*
	 * Content Type 1 - iframe (возвращает html) null - null
	 */
	public static final String contentType = "ct";
	// ID версии файла К�?С
	public static final String fileVersionId = "id_vf";
	// ID человека для чтения его фотографии
	public static final String clientId = "id_fe";
	// ID записи в таблице фотоальбом
	public static final String entryIdInPhotoalbum = "id_fpa";

	// значение ширины для масштабирования изображения
	public static final String width = "SWidth";
	// значение высоты для масштабирования изображения
	public static final String height = "SHeight";
	
	public static final String quality = "quality";
	
	public static final String cacheControl = "no_cache";

	public static final String cacheID = "id";

	public static final String useCache = "useCache";

	public static final String logger = "logger";

	public static final String logLevel = "logLevel";

	public static final String logFolder = "logFolder";

	public static final String logLimit = "logLimit";
}
