package ru.unisuite.contentservlet.web;

class RequestParamName {
    private RequestParamName() {
    }

    // ID web метатермина версии контента
    public static final String webMetaId = "id_wm";
    // алиас метатермина версии контента
    public static final String webMetaAlias = "alias_wm";
    // ID версии файла КИС
    public static final String fileVersionId = "id_vf";
    // ID человека для чтения его фотографии
    public static final String idFe = "id_fe";
    // ID записи в таблице фотоальбом
    public static final String entryIdInPhotoalbum = "id_fpa";

    // Content-Disposition контента 1 - attachment 2 - inline null - null
    public static final String contentDisposition = "cd";
    // "db" or "app"
    public static final String resizerType = "resizer";
    // значение ширины для масштабирования изображения
    public static final String[] width = new String[]{"SWidth", "width", "h"};
    // значение высоты для масштабирования изображения
    public static final String[] height = new String[]{"SHeight", "height", "h"};

    public static final String[] quality = new String[]{"quality", "q"};

    public static final String noCache = "no_cache";
}
