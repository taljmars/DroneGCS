package com.dronegcs.console_plugin.services.internal.logevents;

import com.generic_tools.logger.Logger.Type;

/**
 * General event type
 * 
 * @author taljmars
 *
 */
public class LogWarningDisplayerEvent extends LogAbstractDisplayerEvent {

	/**
	 * @param message
	 */
	public LogWarningDisplayerEvent(String message) {
		super(Type.WARNING, message);
	}	
}
