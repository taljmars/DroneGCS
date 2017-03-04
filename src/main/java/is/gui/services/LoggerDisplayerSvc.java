package is.gui.services;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import is.gui.events.logevents.LogAbstractDisplayerEvent;
import is.gui.events.logevents.LogErrorDisplayerEvent;
import is.gui.events.logevents.LogGeneralDisplayerEvent;
import is.gui.events.logevents.LogIncomingDisplayerEvent;
import is.gui.events.logevents.LogOutgoingDisplayerEvent;

/**
 * This service responsible of publishing event to any listener based on applicationEvent I/S in Spring framework.
 * 
 * @author taljmars
 *
 */
@Component("loggerDisplayerSvc")
public class LoggerDisplayerSvc {
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	private static int called;
	/**
	 * Safety method that verify that the object is indeed a singletone
	 */
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}

	/**
	 * basic publisher of any base type of message event
	 * 
	 * @param event
	 */
	private void publish(LogAbstractDisplayerEvent event) {
		applicationEventPublisher.publishEvent(event);
	}

	/**
	 * public error message event
	 * 
	 * @param message
	 */
	public void logError(String message) {
		publish(new LogErrorDisplayerEvent(message));		
	}
	
	/**
	 * public general message event
	 * 
	 * @param message
	 */
	public void logGeneral(String message) {
		publish(new LogGeneralDisplayerEvent(message));		
	}
	
	/**
	 * publish outgoing message event
	 * 
	 * @param message
	 */
	public void logOutgoing(String message) {
		publish(new LogOutgoingDisplayerEvent(message));		
	}
	
	/**
	 * public incoming message event
	 * 
	 * @param message
	 */
	public void logIncoming(String message) {
		publish(new LogIncomingDisplayerEvent(message));		
	}
}
