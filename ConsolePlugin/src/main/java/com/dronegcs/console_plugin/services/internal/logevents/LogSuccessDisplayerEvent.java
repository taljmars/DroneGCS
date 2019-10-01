package com.dronegcs.console_plugin.services.internal.logevents;

import com.generic_tools.logger.Logger.Type;

/**
 * General event type
 * 
 * @author taljmars
 *
 */
public class LogSuccessDisplayerEvent extends LogAbstractDisplayerEvent {

	/**
	 * @param message
	 */
	public LogSuccessDisplayerEvent(String message) {
		super(Type.SUCCESS, message);
	}	
}
