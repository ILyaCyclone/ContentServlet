package ru.miit.contentservlet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ContentLogger {

	public static Level logLevel;
	public static String logFileName = "appLog.%g.txt";
	public static String logFolderName;
	public static String pattern;
	public static int limit;

	public static void initLogManager(ContentServletProperties contentServletProperties) {

		logLevel = contentServletProperties.getLogLevel();
		logFolderName = contentServletProperties.getPattern();
		File logFolder = new File(logFolderName);
		
		if(!logFolder.exists())
            logFolder.mkdirs();
		
		pattern = logFolder + File.separator + logFileName;
		limit = contentServletProperties.getLimit();

//		Logger rootLogger = LogManager.getLogManager().getLogger("");
		
		Logger mainLogger = Logger.getLogger(ContentLogger.class.getName());
		
		mainLogger.setLevel(logLevel);
		
		Handler handler;
		try {
			handler = new FileHandler(pattern, limit, 5, true);
			Formatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
		} catch (SecurityException | IOException e) {
			throw new RuntimeException(e);
		}

		mainLogger.addHandler(handler);
		
		//
		// Handler[] handlers = rootLogger.getHandlers();
		// if (handlers.length > 0 && rootLogger.getHandlers()[0] instanceof
		// ConsoleHandler) {
		// rootLogger.removeHandler(rootLogger.getHandlers()[0]);
		// }
		// Arrays.stream(rootLogger.getHandlers()).forEach(h ->
		// h.setLevel(Level.parse(logLevel)));

	}

	public static Logger getLogger(String className) {

		Logger logger = Logger.getLogger(ContentLogger.class.getName() + "." + className);

		return logger;

	}
}
