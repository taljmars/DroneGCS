package services.internal.logevents;

import logger.Logger.Type;

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
