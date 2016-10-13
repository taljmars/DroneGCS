package gui.is.services;

import gui.is.events.JMVCommandEvent;

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
@Component("jMVEventPublisher")
public class JMVEventPublisher {

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 * publishing GUI event
	 * 
	 * @param event
	 */
	public void publish(JMVCommandEvent event) {
		applicationEventPublisher.publishEvent(event);
	}
}
