package gui.is.services;

import logger.Logger.Type;

public class LogGeneralDisplayerEvent extends LogDisplayerEvent {
	
	public LogGeneralDisplayerEvent(String entry) {
		super(Type.GENERAL, entry);
	}
	
	public static LogDisplayerEvent log(String entry) {
		return new LogGeneralDisplayerEvent(entry);
	}
	
}
