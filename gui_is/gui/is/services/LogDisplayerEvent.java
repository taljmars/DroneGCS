package gui.is.services;

import logger.Logger.Type;

public abstract class LogDisplayerEvent {
	
	private Type type;
	
	private String entry;
	
	public LogDisplayerEvent(Type type, String entry) {
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
