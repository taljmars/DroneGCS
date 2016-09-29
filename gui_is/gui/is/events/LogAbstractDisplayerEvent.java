package gui.is.events;

import logger.Logger.Type;

public abstract class LogAbstractDisplayerEvent {
	
	private Type type;
	
	private String entry;
	
	public LogAbstractDisplayerEvent(Type type, String entry) {
		this.type = type;
		this.entry = entry;
	}
	
	public String getEntry() {
		return entry;
	}
	
	public Type getType() {
		return type;
	}

}
