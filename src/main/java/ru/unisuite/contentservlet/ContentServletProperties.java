package ru.unisuite.contentservlet;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

class ContentServletProperties {
	
	private final static String CONFIG_FILE_NAME = "ContentServletConfig.xml";

	private boolean useCache;

	public ContentServletProperties() throws ContentServletPropertiesException {

		File xmlFile = new File(this.getClass().getClassLoader().getResource(CONFIG_FILE_NAME).getFile());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document document = null;
		try {
			db = dbf.newDocumentBuilder();
			document = db.parse(xmlFile);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ContentServletPropertiesException(e.getMessage(), e);
		}

		document.getDocumentElement().normalize();

		boolean useCache;

		try {
			useCache = Boolean.parseBoolean(document.getDocumentElement().getElementsByTagName(ServletParamName.useCache)
					.item(0).getTextContent().toString());
		} catch (NullPointerException e) {
			useCache = false;
//			rootLogger.log(Level.WARNING, " useCache value not found. By default useCache value  was set false. ");
		}

		this.useCache = useCache;

	}

	public boolean isUseCache() {
		return useCache;
	}

}
