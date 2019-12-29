package com.dronegcs.console_plugin.event_logger;

import com.auditdb.persistence.base_scheme.EventLogObject;
import com.auditdb.persistence.scheme.*;
import com.db.persistence.scheme.BaseObject;
import com.db.persistence.scheme.QueryRequestRemote;
import com.db.persistence.scheme.QueryResponseRemote;
import com.dronegcs.console_plugin.remote_services_wrappers.QuerySvcRemoteWrapper;
import com.dronegcs.tracker.services.TrackerEventProducer;
import com.dronegcs.tracker.services.TrackerSvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.*;

@Component
public class EventLogManagerImpl implements EventLogManager, TrackerEventProducer {

    private final static Logger LOGGER = LoggerFactory.getLogger(EventLogManagerImpl.class);

    @Autowired
    private QuerySvcRemoteWrapper querySvcRemote;

    @Autowired
    private LoggingStreamingHandler loggingStreamHandler;

    @Autowired
    private TrackerSvc trackerSvc;

    @PostConstruct
    public void init(){
        WebSocketClient client = new StandardWebSocketClient();

        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        stompClient.connect("ws://localhost:9024/events", loggingStreamHandler);

        trackerSvc.addEventProducer(this);
    }

    @Override
    public EventLogBundle getAllEventLogs() {

        EventLogBundle eventLogBundle = new EventLogBundle();

        QueryRequestRemote req;
        QueryResponseRemote resp;

        req = new QueryRequestRemote();
        req.setQuery("GetAllAccessLog");
        req.setClz(AccessLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req = new QueryRequestRemote();
        req.setQuery("GetAllObjectCreationLog");
        req.setClz(ObjectCreationLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req.setQuery("GetAllObjectDeletionLog");
        req.setClz(ObjectDeletionLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req.setQuery("GetAllObjectUpdateLog");
        req.setClz(ObjectUpdateLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req.setQuery("GetAllRegistrationLog");
        req.setClz(RegistrationLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req.setQuery("ExternalObjectLog");
        req.setClz(ExternalObjectLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        return eventLogBundle;
    }


    @Override
    public EventLogBundle getAllEventLogsBetween(Date startDate, Date endDate) {

        EventLogBundle eventLogBundle = new EventLogBundle();

        QueryRequestRemote req;
        QueryResponseRemote resp;

        Map<String, String> boundaries = new HashMap<>();
        boundaries.put("START_DATE", new Timestamp(startDate.getTime()).toString());
        boundaries.put("END_DATE", new Timestamp(endDate.getTime()).toString());

        req = new QueryRequestRemote();
        req.getParameters().putAll(boundaries);
        req.setQuery("GetAllAccessLog_BetweenDates");
        req.setClz(AccessLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req = new QueryRequestRemote();
        req.getParameters().putAll(boundaries);
        req.setQuery("GetAllObjectCreationLog_BetweenDates");
        req.setClz(ObjectCreationLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req = new QueryRequestRemote();
        req.getParameters().putAll(boundaries);
        req.setQuery("GetAllObjectDeletionLog_BetweenDates");
        req.setClz(ObjectDeletionLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req = new QueryRequestRemote();
        req.getParameters().putAll(boundaries);
        req.setQuery("GetAllObjectUpdateLog_BetweenDates");
        req.setClz(ObjectUpdateLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req.setQuery("GetAllRegistrationLog_BetweenDates");
        req.getParameters().putAll(boundaries);
        req.setClz(RegistrationLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req.setQuery("ExternalObjectLog_BetweenDates");
        req.getParameters().putAll(boundaries);
        req.setClz(ExternalObjectLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        return eventLogBundle;
    }

    private List<EventLogObject> convertToEventLogObject(List<BaseObject> lst) {
        List<EventLogObject> logs = new ArrayList<>();
        for (BaseObject baseObject : lst) {
            if (baseObject instanceof EventLogObject)
                logs.add((EventLogObject) baseObject);
        }
        return logs;
    }

    @Override
    public void SyncTracker() {
        LOGGER.debug("Generating logs from the last month");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1); // to get previous year add -1
        Date lastDate = cal.getTime();
        LOGGER.debug("Setting first timestamp to " + lastDate);
        Date tmpDate = new Date();
        EventLogBundle eventLogBundle = getAllEventLogsBetween(lastDate, tmpDate);
        for (EventLogObject a: eventLogBundle.logs) {
            LoggingStreamingHandler.pushEventToTracker(this, trackerSvc, a);
        }
    }
}
