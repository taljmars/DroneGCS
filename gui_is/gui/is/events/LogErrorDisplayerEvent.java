package gui.is.events;

import logger.Logger.Type;

public class LogErrorDisplayerEvent extends LogAbstractDisplayerEvent {
	
	public LogErrorDisplayerEvent(String entry) {
		super(Type.ERROR, entry);
	}
	
	public static LogAbstractDisplayerEvent log(String entry) {
		return new LogErrorDisplayerEvent(entry);
	}
	
}
