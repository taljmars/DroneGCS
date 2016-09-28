package gui.is.services;

import logger.Logger.Type;

public class LogErrorDisplayerEvent extends LogDisplayerEvent {
	
	public LogErrorDisplayerEvent(String entry) {
		super(Type.ERROR, entry);
	}
	
	public static LogDisplayerEvent log(String entry) {
		return new LogErrorDisplayerEvent(entry);
	}
	
}
