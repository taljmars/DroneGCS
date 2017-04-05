package com.dronegcs.console_plugin.services.internal.logevents;

import com.generic_tools.logger.Logger.Type;

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
