package ru.miit.contentservlet;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class ContentServletProperties {

	Logger rootLogger = LogManager.getLogManager().getLogger(""); // Это корневой логгер, самый главный. Сделан по причине еще отсуствия логгера приложения, но необходимости логировать ошибки при инициализации параметров
	
	public final static String CONFIGLOCATION_NAME = "java:comp/env/contentServlet/configFileileLocation";
	
	private final static Level defaultLogLevel = Level.WARNING;
	private final static int defaultLogLimit = 1000000;

	public boolean useCache;

	public Level logLevel;
	public String logFolder;
	public Integer logLimit;

	public ContentServletProperties() throws ContentServletPropertiesException {

		File xmlFile = new File(getContentServletFileLocation());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document document = null;
		try {
			db = dbf.newDocumentBuilder();
			document = db.parse(xmlFile);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ContentServletPropertiesException(e.getMessage());
		}

		document.getDocumentElement().normalize();

		boolean useCache;

		try {
			useCache = Boolean.getBoolean(document.getDocumentElement().getElementsByTagName(ServletParamName.useCache)
					.item(0).getTextContent());
		} catch (NullPointerException e) {
			useCache = false;
			rootLogger.log(Level.WARNING, " useCache value not found. By default useCache value  was set false. ");
		}

		this.useCache = useCache;

		Element element = (Element) document.getDocumentElement().getElementsByTagName(ServletParamName.logger).item(0);
		
		try {
			String logLevelString = element.getElementsByTagName(ServletParamName.logLevel).item(0).getTextContent().toString();
			logLevel = Level.parse(logLevelString);
		} catch (NullPointerException | IllegalArgumentException e) {
			rootLogger.log(Level.WARNING, " Problems with logLevel initialization. Check configFile. By default logLevel value was set to WARNING. ");
			logLevel = defaultLogLevel;
		}
		
		try {
			logFolder = element.getElementsByTagName(ServletParamName.logFolder).item(0).getTextContent().toString();
		} catch (NullPointerException e) {
			rootLogger.log(Level.SEVERE, "logFolder is null. Check config file. ");
		}
		
		
		try {
			logLimit = Integer.parseInt(element.getElementsByTagName(ServletParamName.logLimit).item(0).getTextContent().toString());
		} catch (NullPointerException | IllegalArgumentException e) {
			rootLogger.log(Level.WARNING, " Problems with logLimit initialization. Check config file. By default logLimit value was set to 1000000. ");
			logLimit = defaultLogLimit;
		}

	}

	public boolean isUseCache() {
		return useCache;
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public String getPattern() {
		return logFolder;
	}

	public Integer getLimit() {
		return logLimit;
	}

	private String getContentServletFileLocation() {

		Context initialContext = null;

		String configFileLocation = null;
		try {
			initialContext = new InitialContext();
			configFileLocation = (String) initialContext.lookup(CONFIGLOCATION_NAME);
			return configFileLocation;
		} catch (NamingException e) {
			throw new RuntimeException(e);
		} finally {
			if (initialContext != null) {
				try {
					initialContext.close();
				} catch (NamingException e) {
					throw new RuntimeException(e);
				}

			}
		}

	}

}
