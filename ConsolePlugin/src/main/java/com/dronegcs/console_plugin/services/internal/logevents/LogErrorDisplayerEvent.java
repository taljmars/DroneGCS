package com.dronegcs.console_plugin.services.internal.logevents;

import com.dronegcs.gcsis.logger.Logger.Type;

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
