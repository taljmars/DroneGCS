package com.dronegcs.console_plugin.services.internal.logevents;

import com.generic_tools.logger.Logger.Type;

/**
 * Error event type
 *  
 * @author taljmars
 *
 */
public class LogErrorDisplayerEvent extends LogAbstractDisplayerEvent {
	
	/**
	 * @param message
	 */
	public LogErrorDisplayerEvent(String message) {
		super(Type.ERROR, message);
	}	
}
