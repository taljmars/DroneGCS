package com.dronegcs.console_plugin.services;

import javax.annotation.PostConstruct;

import com.dronegcs.console_plugin.ActiveUserProfile;
import com.dronegcs.console_plugin.services.internal.logevents.*;
import com.dronegcs.tracker.objects.EventSource;
import com.dronegcs.tracker.objects.TrackerEvent;
import com.dronegcs.tracker.services.TrackerEventProducer;
import com.dronegcs.tracker.services.TrackerSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import org.springframework.util.Assert;

import java.util.Date;

import static com.dronegcs.tracker.objects.TrackerEvent.Type.*;

/**
 * This service responsible of publishing event to any listener based on applicationEvent I/S in Spring framework.
 * 
 * @author taljmars
 *
 */
@Component
public class LoggerDisplayerSvc implements TrackerEventProducer {
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private TrackerSvc trackerSvc;

	@Autowired
	private ActiveUserProfile activeUserProfile;

	private static int called;
	/**
	 * Safety method that verify that the object is indeed a singleton
	 */
	@PostConstruct
	private void init() {
		Assert.isTrue(++called == 1, "Not a Singleton");
		trackerSvc.addEventProducer(this);
	}

	/**
	 * basic publisher of any base type of message event
	 * 
	 * @param event
	 */
	private void publish(LogAbstractDisplayerEvent event) {
		applicationEventPublisher.publishEvent(event);
		TrackerEvent.Type type = INFO;
		if (event instanceof LogWarningDisplayerEvent)
			type = WARNING;
		else if (event instanceof LogErrorDisplayerEvent)
			type = ERROR;
		else if (event instanceof LogSuccessDisplayerEvent)
			type = SUCCESS;

		trackerSvc.pushEvent(this, new TrackerEvent(
				activeUserProfile.getUsername(),
				EventSource.SYSTEM.name(),
				type,
				"",
				event.getEntry()
		));
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
