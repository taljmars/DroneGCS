package com.dronegcs.console_plugin.event_logger;

import com.auditdb.persistence.base_scheme.EventLogObject;
import com.auditdb.persistence.scheme.*;
import com.db.persistence.scheme.BaseObject;
import com.dronegcs.tracker.objects.EventSource;
import com.dronegcs.tracker.objects.TrackerEvent;
import com.dronegcs.tracker.services.TrackerEventProducer;
import com.dronegcs.tracker.services.TrackerSvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.UUID;

@Component
public class LoggingStreamingHandler extends StompSessionHandlerAdapter implements TrackerEventProducer {

    private Logger logger = LoggerFactory.getLogger(LoggingStreamingHandler.class);

//    private EventLogBundle eventLogBundle;

    @Autowired
    private TrackerSvc trackerSvc;

    @PostConstruct
    public void init() {
//        eventLogBundle = new EventLogBundle();
        trackerSvc.addEventProducer(this);
    }

//    public EventLogBundle popEventLogBundle() {
//        EventLogBundle tmp = eventLogBundle;
//        eventLogBundle = new EventLogBundle();
//        return tmp;
//    }


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
//        BaseObject object = (BaseObject) payload;
//        eventLogBundle.append((EventLogObject) object);

        if (payload instanceof ExternalObjectLog)
            return;

        pushEventToTracker(this, trackerSvc, (EventLogObject) payload);

    }

    public static void pushEventToTracker(TrackerEventProducer producer, TrackerSvc trackerSvc, EventLogObject eventLogObject) {
//        EventLogObject eventLogObject = (EventLogObject) object;
        String topic = "", summary = "";
        Object extra = null;

        if (eventLogObject instanceof ExternalObjectLog) {
            ExternalObjectLog ex = ((ExternalObjectLog) eventLogObject);
            trackerSvc.pushEvent(producer, new TrackerEvent(
                    ex.getExternalUser(),
                    ex.getEventSource(),
                    ex.getKeyId().getObjId(),
                    TrackerEvent.Type.valueOf(ex.getType()),
                    ex.getDate(),
                    ex.getTopic(),
                    ex.getSummary(),
                    ex.getPayload()
            ));
            return;
        }
        else if (eventLogObject instanceof ObjectCreationLog) {
            topic = "Object Creation";
            summary = ((ObjectCreationLog) eventLogObject).getReferredObjType().getSimpleName() + " Created";
        }
        else if (eventLogObject instanceof ObjectDeletionLog) {
            topic = "Object Deletion";
            summary = ((ObjectDeletionLog) eventLogObject).getReferredObjType().getSimpleName() + " Deleted";
        }
        else if (eventLogObject instanceof ObjectUpdateLog) {
            topic = "Object Modified";
            summary = ((ObjectUpdateLog) eventLogObject).getReferredObjType().getSimpleName() + " Modified";
            extra = "Changed fields: " + ((ObjectUpdateLog) eventLogObject).getChangedFields().toString() + "\n";
            for (int i = 0 ; i < ((ObjectUpdateLog) eventLogObject).getChangedFields().size() ; i++) {
                String field = ((ObjectUpdateLog) eventLogObject).getChangedFields().get(i);
                String from = ((ObjectUpdateLog) eventLogObject).getChangedFromValues().get(i);
                String to = ((ObjectUpdateLog) eventLogObject).getChangedToValues().get(i);
                extra += "Field named '" + field + "' changed from '" + from + "' to '" + to + "'\n";
            }
        }
        else if (eventLogObject instanceof AccessLog) {
            topic = "login";
            summary = "User '" + ((AccessLog) eventLogObject).getUserName() + "' logged to DB server";
        }
        else if (eventLogObject instanceof RegistrationLog) {
            topic = "Registration";
            summary = "New user joined '" + ((RegistrationLog) eventLogObject).getUserName() + "'";
        }

        trackerSvc.pushEvent(producer, new TrackerEvent(
                eventLogObject.getUserName(),
                EventSource.DB_SERVER.name(),
                eventLogObject.getKeyId().getObjId(),
                TrackerEvent.Type.INFO,
                eventLogObject.getEventTime(),
                topic,
                summary,
                extra
        ));
    }

}