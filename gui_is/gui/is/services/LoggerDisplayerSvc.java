package gui.is.services;

import gui.is.events.LogAbstractDisplayerEvent;
import gui.is.events.LogErrorDisplayerEvent;
import gui.is.events.LogGeneralDisplayerEvent;
import gui.is.events.LogIncomingDisplayerEvent;
import gui.is.events.LogOutgoingDisplayerEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

public class LoggerDisplayerSvc {
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	private void publish(LogAbstractDisplayerEvent event) {
		applicationEventPublisher.publishEvent(event);
	}

	public void logError(String txt) {
		publish(new LogErrorDisplayerEvent(txt));		
	}
	
	public void logGeneral(String txt) {
		publish(new LogGeneralDisplayerEvent(txt));		
	}
	
	public void logOutgoing(String txt) {
		publish(new LogOutgoingDisplayerEvent(txt));		
	}
	
	public void logIncoming(String txt) {
		publish(new LogIncomingDisplayerEvent(txt));		
	}
}
