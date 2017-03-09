package com.dronegcs.console.services.internal.logevents;

import com.dronegcs.gcsis.logger.Logger.Type;

/**
 * Incoming event type
 * 
 * @author taljmars
 *
 */
public class LogIncomingDisplayerEvent extends LogAbstractDisplayerEvent {
	
	/**
	 * @param message
	 */
	public LogIncomingDisplayerEvent(String message) {
		super(Type.INCOMING, message);
	}
}
