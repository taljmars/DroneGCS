package com.dronegcs.console_plugin.event_logger;

import com.auditdb.persistence.base_scheme.EventLogObject;
import com.db.persistence.scheme.BaseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;

@Component
public class LoggingStreamingHandler extends StompSessionHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(LoggingStreamingHandler.class);

    private EventLogBundle eventLogBundle;

    @PostConstruct
    public void init() {
        eventLogBundle = new EventLogBundle();
    }

    public EventLogBundle popEventLogBundle() {
        EventLogBundle tmp = eventLogBundle;
        eventLogBundle = new EventLogBundle();
        return tmp;
    }


    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("New session established : " + session.getSessionId());
        session.subscribe("/topic/event-logs", this);
        logger.info("Subscribed to /topic/event-logs");
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        if (! headers.containsKey("class"))
            throw new RuntimeException("Unknown element");

        String classPath = headers.get("class").get(0);
        try {
            Class clz = getClass().getClassLoader().loadClass(classPath);
            return clz;
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find class");
        }
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        BaseObject object = (BaseObject) payload;
        eventLogBundle.append((EventLogObject) object);
    }

}