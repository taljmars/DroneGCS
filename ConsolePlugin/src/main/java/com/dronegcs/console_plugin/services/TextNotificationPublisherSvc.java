package com.dronegcs.console_plugin.services;

import org.slf4j.LoggerFactory;
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

	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TextNotificationPublisherSvc.class);

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 * publish a text message
	 * 
	 * @param message
	 */
	public void publish(String message) {
		LOGGER.debug("Publishing event '" + message + "'");
		applicationEventPublisher.publishEvent(message);
	}
}
