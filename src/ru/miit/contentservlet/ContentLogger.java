package ru.miit.contentservlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ContentLogger {

	private static final String DataSourceLogLevel = "java:comp/env/servletContentLogger/level";
	private static final String DataSourceFileLocation = "java:comp/env/servletContentLogger/fileLocation";

	public static Logger logger;

	public static void initLogManager() {

		try {
			LogManager.getLogManager().readConfiguration(ContentServlet.class.getResourceAsStream("logging.properties"));
		} catch (SecurityException | IOException | NullPointerException e) {
			e.printStackTrace();
		}

//		Logger rootLogger = LogManager.getLogManager().getLogger("");
//		rootLogger.removeHandler(rootLogger.getHandlers()[0]);
//		Arrays.stream(rootLogger.getHandlers()).forEach(h -> h.setLevel(Level.parse(getLevel())));
//
//		System.out.println(getLevel());

	}

	public static Logger getLogger(String className) {

		
//		logger = Logger.getLogger(className);
//
//		if (logger != null && logger.getHandlers().length == 0) {
//
//			FileHandler handler = null;
//
//			try {
//				handler = new FileHandler(getLogFileLocation(), 1024 * 1024, 10, true);
//				Formatter formatter = new SimpleFormatter();
//				handler.setFormatter(formatter);
//			} catch (SecurityException | IOException e) {
//				throw new RuntimeException(e);
//			}
//
//			logger.addHandler(handler);
//		}

		return Logger.getLogger(className);

	}

	private static String getLevel() {

		Context initialContext = null;
		String level = null;
		try {
			initialContext = new InitialContext();
			level = initialContext.lookup(DataSourceLogLevel).toString();
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

		return level;

	}
	
	private static String getLogFileLocation() {

		Context initialContext = null;
		String level = null;
		try {
			initialContext = new InitialContext();
			level = initialContext.lookup(DataSourceFileLocation).toString();
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

		return level;

	}

}
