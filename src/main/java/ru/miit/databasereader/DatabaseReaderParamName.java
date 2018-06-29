package ru.miit.databasereader;

public class DatabaseReaderParamName {

	private DatabaseReaderParamName() {
		new AssertionError(DatabaseReaderParamName.class.getName() + " shouldn't be initialised");
	}

	final static public String rstr = "rStr", cnt = "cnt", lastModified = "cntsecond_last_modified",
			filename = "filename", dataBinary = "data_binary", contentType = "contentType", size = "size",
			mime = "mime", hash = "hash", bsize = "bsize", extension = "extension", general = "general",
			webMetaId = "webMetaId", fileVersionId = "fileVersionId", clientId = "clientId",
			entryIdInPhotoalbum = "entryIdInPhotoalbum", width = "width", height = "height", type = "type";

}
