package gui.is.events;

import logger.Logger.Type;

public class LogOutgoingDisplayerEvent extends LogAbstractDisplayerEvent {
	
	public LogOutgoingDisplayerEvent(String entry) {
		super(Type.OUTGOING, entry);
	}
	
	public static LogAbstractDisplayerEvent log(String entry) {
		return new LogOutgoingDisplayerEvent(entry);
	}
	
}
