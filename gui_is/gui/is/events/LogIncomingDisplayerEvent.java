package gui.is.events;

import logger.Logger.Type;

public class LogIncomingDisplayerEvent extends LogAbstractDisplayerEvent {
	
	public LogIncomingDisplayerEvent(String entry) {
		super(Type.INCOMING, entry);
	}
	
	public static LogAbstractDisplayerEvent log(String entry) {
		return new LogIncomingDisplayerEvent(entry);
	}
	
}
