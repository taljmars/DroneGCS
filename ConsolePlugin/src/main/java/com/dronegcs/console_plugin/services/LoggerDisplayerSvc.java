package com.dronegcs.console_plugin.services;

import javax.annotation.PostConstruct;

import com.dronegcs.console_plugin.services.internal.logevents.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import org.springframework.util.Assert;

/**
 * This service responsible of publishing event to any listener based on applicationEvent I/S in Spring framework.
 * 
 * @author taljmars
 *
 */
@Component
public class LoggerDisplayerSvc {
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	private static int called;
	/**
	 * Safety method that verify that the object is indeed a singleton
	 */
	@PostConstruct
	private void init() {
		Assert.isTrue(++called == 1, "Not a Singleton");
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
	 * public warning message event
	 *
	 * @param message
	 */
	public void logWarning(String message) {
		publish(new LogWarningDisplayerEvent(message));
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
	 * public success message event
	 *
	 * @param message
	 */
	public void logSuccess(String message) {
		publish(new LogSuccessDisplayerEvent(message));
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
