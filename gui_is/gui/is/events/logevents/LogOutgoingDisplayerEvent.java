package gui.is.events.logevents;

import tools.logger.Logger.Type;

/**
 * Outgoing event type
 * 
 * @author taljmars
 *
 */
public class LogOutgoingDisplayerEvent extends LogAbstractDisplayerEvent {
	
	/**
	 * @param message
	 */
	public LogOutgoingDisplayerEvent(String message) {
		super(Type.OUTGOING, message);
	}
}
