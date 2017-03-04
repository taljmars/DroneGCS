package is.gui.events.logevents;

import is.logger.Logger.Type;

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
