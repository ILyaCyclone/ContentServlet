package ru.unisuite.contentservlet.web;

class RequestParamName {
    private RequestParamName() {
    }

    // ---------- required parameters section ----------
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

    // ---------- image section ----------
    // "db" or "app"
    public static final String resizerType = "resizer";
    public static final String[] width = {"SWidth", "width", "w"};
    public static final String[] height = {"SHeight", "height", "h"};
    public static final String[] quality = {"quality", "q"};

    // ---------- cache section ----------
    // set response Cache-Control header value
    public static final String cache = "cache";
    // set response Cache-Control header to "no-cache"
    public static final String noCache = "no_cache";
    // set response Cache-Control header to private
    public static final String priv = "private";

    // ---------- other ----------
    // http Content-Disposition: 1 - attachment 2 - inline, default - filename=
    public static final String contentDisposition = "cd";
}
