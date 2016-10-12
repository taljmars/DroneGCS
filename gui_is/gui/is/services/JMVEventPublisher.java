package gui.is.services;

import gui.is.events.JMVCommandEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("jMVEventPublisher")
public class JMVEventPublisher {

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void publish(JMVCommandEvent event) {
		applicationEventPublisher.publishEvent(event);
	}
}
