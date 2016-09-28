package gui.is.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

public class JMVEventPublisher {

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void publish(JMVCommandEvent event) {
		System.out.println("Publishing event " + event);
		applicationEventPublisher.publishEvent(event);
	}
}
