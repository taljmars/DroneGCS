package gui.is.events.logevents;

import tools.logger.Logger.Type;

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
