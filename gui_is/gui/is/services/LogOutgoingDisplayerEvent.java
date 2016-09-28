package gui.is.services;

import logger.Logger.Type;

public class LogOutgoingDisplayerEvent extends LogDisplayerEvent {
	
	public LogOutgoingDisplayerEvent(String entry) {
		super(Type.ERROR, entry);
	}
	
	public static LogDisplayerEvent log(String entry) {
		return new LogOutgoingDisplayerEvent(entry);
	}
	
}
