package com.dronegcs.console_plugin.services.internal.logevents;

import com.dronegcs.gcsis.logger.Logger.Type;

/**
 * General event type
 * 
 * @author taljmars
 *
 */
public class LogGeneralDisplayerEvent extends LogAbstractDisplayerEvent {
	
	/**
	 * @param message
	 */
	public LogGeneralDisplayerEvent(String message) {
		super(Type.GENERAL, message);
	}	
}
