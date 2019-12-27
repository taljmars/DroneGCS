package com.dronegcs.tracker.services;

import com.dronegcs.tracker.objects.TrackerEvent;

import java.util.Date;
import java.util.List;

public interface TrackerSvc {

    List<TrackerEvent> getAllEventLogs();

    List<TrackerEvent> getAllEventLogsBetween(Date startDate, Date endDate);

    void addEventProducer(TrackerEventProducer trackerEventProducer);

    void pushEvent(TrackerEventProducer trackerEventProducer, TrackerEvent trackerEvent);

    void addEventConsumer(TrackerEventConsumer trackerEventConsumer);
}
