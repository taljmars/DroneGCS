package gui.is.services;

import java.util.HashSet;
import java.util.Set;

import logger.Logger;
import logger.Logger.Type;

public class LoggerDisplayerManager {
		
	private Set<LoggerDisplayerListener> loggerDisplayerListeners = null;
	private static LoggerDisplayerManager instance = null;
	private static Object lock = new Object();
	
	private LoggerDisplayerManager() {
		loggerDisplayerListeners = new HashSet<LoggerDisplayerListener>();
	}
	
	private static LoggerDisplayerManager LazyInit() {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new LoggerDisplayerManager();
				}
			}
		}
		
		return instance;
	}
	
	public static void addLoggerDisplayerListener(LoggerDisplayerListener loggerDisplayer) {
		LazyInit();
		instance.loggerDisplayerListeners.add(loggerDisplayer);
	}
	
	public static void removeLoggerDisplayerListener(LoggerDisplayerListener loggerDisplayer, int maxDisplayerLines) {
		LazyInit();
		instance.loggerDisplayerListeners.remove(loggerDisplayer);
	}
	
	public static void addGeneralMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.GENERAL);
	}
	
	public static void addErrorMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.ERROR);
	}
	
	public static void addOutgoingMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.OUTGOING);
	}
	
	public static void addIncommingMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.INCOMING);
	}
	
	public static void addMessegeToDisplay(String cmd, Type t) {
		addMessegeToDisplay(cmd, t, false);
	}
	
	public synchronized static void addMessegeToDisplay(String cmd, Type t, boolean no_date) {
		
		String newcontent = Logger.generateDesignedMessege(cmd, t, no_date);
		
		for (LoggerDisplayerListener loggerDisplayer : instance.loggerDisplayerListeners) {
			String alltext = loggerDisplayer.getDisplayedLoggerText();
			String content = "";
			if (!alltext.isEmpty())
				content = alltext.substring(alltext.indexOf("<body>") + "<body>".length(), alltext.indexOf("</body>"));
			int idx = content.indexOf("<font");
			if (idx == -1) {
				content = "";
			}
			else {
				content = content.substring(idx);
			}
			
			content = (newcontent + content);
	
			// To Screen
			String futureText = "<html>";
			int maxDisplayerLines = loggerDisplayer.getMaxLoggerDisplayedLines();
			String[] sz = content.split("</font>", maxDisplayerLines);
			for (int i = 0 ; i < Math.min(maxDisplayerLines - 1, sz.length) ; i++) {
				futureText += (sz[i] + "</font>");
			}
			
			futureText += "</html>";
			loggerDisplayer.setDisplayedLoggerText(futureText);
		}
		
		Logger.LogDesignedMessege(newcontent);
	}

}
