package gui.is.services;

import logger.Logger.Type;

public class LogIncomingDisplayerEvent extends LogDisplayerEvent {
	
	public LogIncomingDisplayerEvent(String entry) {
		super(Type.ERROR, entry);
	}
	
	public static LogDisplayerEvent log(String entry) {
		return new LogIncomingDisplayerEvent(entry);
	}
	
}
