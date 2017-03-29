package com.dronegcs.console_plugin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * This service responsible of sending text notifications to any registered listener.
 * It mainly being used to show messages in the toolbar
 * 
 * @author taljmars
 *
 */
@Component
public class TextNotificationPublisherSvc {
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 * publish a text message
	 * 
	 * @param message
	 */
	public void publish(String message) {
		System.out.println("Publishing event '" + message + "'");
		applicationEventPublisher.publishEvent(message);
	}
}
