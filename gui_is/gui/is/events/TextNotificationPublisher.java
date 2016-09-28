package gui.is.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

public class TextNotificationPublisher {
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void publish(String msg) {
		System.out.println("Publishing event " + msg);
		applicationEventPublisher.publishEvent(msg);
	}
}
