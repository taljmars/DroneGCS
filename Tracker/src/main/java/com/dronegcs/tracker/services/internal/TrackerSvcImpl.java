package com.dronegcs.tracker.services.internal;

import com.dronegcs.tracker.objects.TrackerEvent;
import com.dronegcs.tracker.services.TrackerEventConsumer;
import com.dronegcs.tracker.services.TrackerEventProducer;
import com.dronegcs.tracker.services.TrackerSvc;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class TrackerSvcImpl implements TrackerSvc {

    private Set<TrackerEventProducer> producers;
    private List<TrackerEvent> list;
    private Set<TrackerEventConsumer> consumers;

    @PostConstruct
    private void init() {
        producers = new HashSet<>();
        consumers = new HashSet<>();
        list = new ArrayList<>();
    }

    @Override
    public List<TrackerEvent> getAllEventLogs() {
        return null;
    }

    @Override
    public List<TrackerEvent> getAllEventLogsBetween(Date startDate, Date endDate) {
        return null;
    }

    @Override
    public void addEventProducer(TrackerEventProducer trackerEventProducer) {
        producers.add(trackerEventProducer);
    }

    @Override
    public void pushEvent(TrackerEventProducer trackerEventProducer, TrackerEvent trackerEvent) {
        if (!producers.contains(trackerEventProducer))
            throw new RuntimeException("Producer is not signed");

        list.add(trackerEvent);

        for (TrackerEventConsumer consumer : consumers)
            consumer.offer(trackerEvent);
    }

    @Override
    public void addEventConsumer(TrackerEventConsumer trackerEventConsumer) {
        consumers.add(trackerEventConsumer);
    }
}
