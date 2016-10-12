package gui.is.services;

import javax.annotation.PostConstruct;

import gui.is.events.LogAbstractDisplayerEvent;
import gui.is.events.LogErrorDisplayerEvent;
import gui.is.events.LogGeneralDisplayerEvent;
import gui.is.events.LogIncomingDisplayerEvent;
import gui.is.events.LogOutgoingDisplayerEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("loggerDisplayerSvc")
public class LoggerDisplayerSvc {
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}

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
