package ru.unisuite.contentservlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

class ContentServletProperties {
	public ContentServletProperties() throws ContentServletPropertiesException {
		// initFromXml();
		initFromProperties();
	}

	private static Logger logger = LoggerFactory.getLogger(ContentServletProperties.class.getName());

	private final static String CONFIG_FILE_NAME = "ContentServletConfig.xml";

	private boolean useCache;

	private void initFromProperties() throws ContentServletPropertiesException {

		String filename = "content.properties";
		try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(filename)) {

			if (input == null) {
				String errorMessage = "Unable to load " + filename;
				logger.error(errorMessage);
				throw new ContentServletPropertiesException(errorMessage);
			}

			Properties prop = new Properties();
			prop.load(input);

			Boolean useCache = Boolean.valueOf(prop.getProperty("ru.unisuite.contentservlet.usecache"));

			this.useCache = useCache;

		} catch (IOException e) {
			// e.printStackTrace();
			String errorMessage = "Unable to load " + filename;
			logger.error(errorMessage, e);
			throw new ContentServletPropertiesException(errorMessage, e);
		}

	}

	@SuppressWarnings("unused")
	private void initFromXml() throws ContentServletPropertiesException {
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
			useCache = Boolean.parseBoolean(document.getDocumentElement()
					.getElementsByTagName(ServletParamName.useCache).item(0).getTextContent().toString());
		} catch (NullPointerException e) {
			useCache = false;
			// rootLogger.log(Level.WARNING, " useCache value not found. By default useCache
			// value was set false. ");
		}

		this.useCache = useCache;
	}

	public boolean isUseCache() {
		return useCache;
	}

}
