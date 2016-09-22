package gui.core.dashboard;

import logger.Logger;
import logger.Logger.Type;
import gui.is.LoggerDisplayerHandler;

public class LoggerDisplayerManager {
	
	private LoggerDisplayerHandler loggerDisplayer = null;
	private int maxDisplayerLines = 0;
	
	public LoggerDisplayerManager(LoggerDisplayerHandler loggerDisplayer, int maxDisplayerLines) {
		this.loggerDisplayer = loggerDisplayer;	
		this.maxDisplayerLines = maxDisplayerLines;
	}
	
	public void addGeneralMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.GENERAL);
	}
	
	public void addErrorMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.ERROR);
	}
	
	public void addOutgoingMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.OUTGOING);
	}
	
	public void addIncommingMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.INCOMING);
	}
	
	public void addMessegeToDisplay(String cmd, Type t) {
		addMessegeToDisplay(cmd, t, false);
	}
	
	public synchronized void addMessegeToDisplay(String cmd, Type t, boolean no_date) {

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
		
		String futureText = "<html>";
		
		String newcontent = Logger.generateDesignedMessege(cmd, t, no_date);
		
		Logger.LogDesignedMessege(newcontent);
		
		content = (newcontent + content);

		// To Screen
		String[] sz = content.split("</font>", maxDisplayerLines);
		for (int i = 0 ; i < Math.min(maxDisplayerLines - 1, sz.length) ; i++) {
			futureText += (sz[i] + "</font>");
		}
		
		futureText += "</html>";
		loggerDisplayer.setDisplayedLoggerText(futureText);
	}

}
