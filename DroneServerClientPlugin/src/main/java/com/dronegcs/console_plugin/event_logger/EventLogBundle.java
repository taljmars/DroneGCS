package com.dronegcs.console_plugin.event_logger;

import com.auditdb.persistence.base_scheme.EventLogObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventLogBundle {

    List<EventLogObject> logs;

    public EventLogBundle() {
        this.logs = new ArrayList<>();
    }

    public EventLogBundle append(List<EventLogObject> lst) {
        this.logs.addAll(lst);
        return this;
    }

    public EventLogBundle sortByEventDate() {
        Comparator<EventLogObject> eventDateComparator = Comparator.comparing(EventLogObject::getEventTime);//(o1, o2) -> {return o1.getEventTime().compareTo(o2.getEventTime());};
        logs.sort(eventDateComparator);
        return this;
    }

    public List<? extends EventLogObject> getLogs() {
        return logs;
    }
}
