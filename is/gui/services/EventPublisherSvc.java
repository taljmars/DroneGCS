package gui.services;

import gui.is.events.GuiEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Service that responsible of publishing GUI events.
 * It is mainly being used by GUI components such as MapView or TreeView.
 * 
 * @author taljmars
 *
 */
@Component("eventPublisherSvc")
public class EventPublisherSvc {

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 * publishing GUI event
	 * 
	 * @param event
	 */
	public void publish(GuiEvent event) {
		applicationEventPublisher.publishEvent(event);
	}
}
