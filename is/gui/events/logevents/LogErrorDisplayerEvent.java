package gui.events.logevents;

import logger.Logger.Type;

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
