package gui.is.events;

import logger.Logger.Type;

public class LogGeneralDisplayerEvent extends LogAbstractDisplayerEvent {
	
	public LogGeneralDisplayerEvent(String entry) {
		super(Type.GENERAL, entry);
	}
	
	public static LogAbstractDisplayerEvent log(String entry) {
		return new LogGeneralDisplayerEvent(entry);
	}
	
}
