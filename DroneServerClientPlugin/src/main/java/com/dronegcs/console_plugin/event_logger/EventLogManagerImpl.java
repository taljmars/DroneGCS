package com.dronegcs.console_plugin.event_logger;

import com.auditdb.persistence.base_scheme.EventLogObject;
import com.auditdb.persistence.scheme.AccessLog;
import com.auditdb.persistence.scheme.ObjectModificationLog;
import com.db.persistence.scheme.BaseObject;
import com.db.persistence.wsSoap.QueryRequestRemote;
import com.db.persistence.wsSoap.QueryResponseRemote;
import com.dronegcs.console_plugin.remote_services_wrappers.QuerySvcRemoteWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class EventLogManagerImpl implements EventLogManager {

    @Autowired
    private QuerySvcRemoteWrapper querySvcRemote;

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
        req.setQuery("GetAllObjectModificationLog");
        req.setClz(ObjectModificationLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        return eventLogBundle;
    }

    @Override
    public EventLogBundle getAllEventLogsBetween(Date startDate, Date endDate) {

        EventLogBundle eventLogBundle = new EventLogBundle();

        QueryRequestRemote req;
        QueryResponseRemote resp;

        req = new QueryRequestRemote();
        req.setQuery("GetAllAccessLog_BetweenDates");
        req.setClz(AccessLog.class.getCanonicalName());
        resp = querySvcRemote.query(req);
        eventLogBundle.append(convertToEventLogObject(resp.getResultList()));

        req = new QueryRequestRemote();
        req.setQuery("GetAllObjectModificationLog_BetweenDates");
        req.setClz(ObjectModificationLog.class.getCanonicalName());
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
}
