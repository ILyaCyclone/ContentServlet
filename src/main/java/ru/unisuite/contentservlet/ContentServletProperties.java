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
		initFromProperties();
	}

	private static Logger logger = LoggerFactory.getLogger(ContentServletProperties.class.getName());

	private final static String CONFIG_FILE_NAME = "content.properties";

	private boolean useCache;
	private String datasourceName;
	private String cacheControl;

	private void initFromProperties() throws ContentServletPropertiesException {

		try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {

			if (input == null) {
				String errorMessage = "Unable to load " + CONFIG_FILE_NAME;
				logger.error(errorMessage);
				throw new ContentServletPropertiesException(errorMessage);
			}

			Properties prop = new Properties();
			prop.load(input);

			Boolean useCache = Boolean.valueOf(prop.getProperty("ru.unisuite.contentservlet.usecache"));

			String datasourceName = prop.getProperty("ru.unisuite.contentservlet.jndi.datasource.name");

			String cacheControl = prop.getProperty("ru.unisuite.contentservlet.cachecontrol");

			this.useCache = useCache;
			this.datasourceName = datasourceName;
			this.cacheControl = cacheControl;

		} catch (IOException e) {
			String errorMessage = "Unable to load " + CONFIG_FILE_NAME;
			logger.error(errorMessage, e);
			throw new ContentServletPropertiesException(errorMessage, e);
		}

	}

	@SuppressWarnings("unused")
	private void initFromXml() throws ContentServletPropertiesException {
		File xmlFile = new File(this.getClass().getClassLoader().getResource(CONFIG_FILE_NAME).getFile());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document document;
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
					.getElementsByTagName(ServletParamName.useCache).item(0).getTextContent());
		} catch (NullPointerException e) {
			useCache = false;
			// rootLogger.log(Level.WARNING, " useCache value not found. By default useCache
			// value was set false. ");
		}

		this.useCache = useCache;
	}

	public String getDatasourceName() {
		return datasourceName;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public String getCacheControl() {
		return cacheControl;
	}

}
