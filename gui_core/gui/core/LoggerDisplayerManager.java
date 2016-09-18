package gui.core;

import java.sql.Timestamp;
import java.util.Date;

import logger.Logger;
import gui.is.LoggerDisplayerHandler;

public class LoggerDisplayerManager {
	
	private LoggerDisplayerHandler loggerDisplayer = null;
	private int maxDisplayerLines = 0;
	
	public LoggerDisplayerManager(LoggerDisplayerHandler loggerDisplayer, int maxDisplayerLines) {
		this.loggerDisplayer = loggerDisplayer;	
		this.maxDisplayerLines = maxDisplayerLines;
	}
	
	public static enum Type {
		GENERAL,
		ERROR,
		INCOMING,
		OUTGOING
	};
	
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
	
	public static String generateDesignedMessege(String cmd, Type t, boolean no_date)
	{
		String newcontent = "";
		
		String ts_string = "";
		if (!no_date) { 
			Date date = new Date();
			Timestamp ts = new Timestamp(date.getTime());
			ts_string = "[" + ts.toString() + "]";
		}
		/*
		 * Currently i am converting NL char to space and comma sep.
		 */
		cmd = cmd.replace("\n", ",");
		cmd = cmd.trim();
		String[] lines = cmd.split("\n");
		for (int i = 0 ; i < lines.length ; i++ ){
			if (lines[i].length() == 0)
				continue;

			switch (t) {
				case GENERAL:
					newcontent = ("<font color=\"black\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case OUTGOING:
					newcontent = ("<font color=\"blue\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case INCOMING:
					newcontent = ("<font color=\"green\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case ERROR:
					newcontent = ("<font color=\"red\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				default:
					newcontent = ("<font color=\"red\">" + ts_string + " Unrecognized: " + lines[i] + "</font>" + "<br/>");
					break;
			}
		}
		
		return newcontent;
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
		else
			content = content.substring(idx);
		
		String futureText = "<html>";
		
		String newcontent = generateDesignedMessege(cmd, t, no_date);
		
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
