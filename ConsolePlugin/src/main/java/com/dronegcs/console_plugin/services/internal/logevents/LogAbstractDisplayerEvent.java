package com.dronegcs.console_plugin.services.internal.logevents;

import com.dronegcs.gcsis.logger.Logger.Type;


/**
 * Used for passing events log event that should be display for the user in any com.dronegcs.gcsis.logger displayer clients
 * This is usually being using in our com.dronegcs.gcsis.logger displayer box in the GUI.
 * 
 * @author taljmars
 *
 */
public abstract class LogAbstractDisplayerEvent {
	
	private Type type;
	
	private String message;
	
	
	/**
	 * Constructor, get the log event type and the text message
	 * 
	 * @param type
	 * @param message
	 */
	public LogAbstractDisplayerEvent(Type type, String message) {
		this.type = type;
		this.message = message;
	}
	
	/**
	 * @return message
	 */
	public String getEntry() {
		return message;
	}
	
	/**
	 * @return log type
	 */
	public Type getType() {
		return type;
	}

}
