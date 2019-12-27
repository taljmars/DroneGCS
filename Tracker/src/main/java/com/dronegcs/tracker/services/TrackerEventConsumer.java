package com.dronegcs.tracker.services;

import com.dronegcs.tracker.objects.TrackerEvent;

public interface TrackerEventConsumer {

    void offer(TrackerEvent trackerEvent);
}
