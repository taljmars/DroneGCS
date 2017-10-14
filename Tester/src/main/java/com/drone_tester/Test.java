package com.drone_tester;

import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.dronegcs.console_plugin.remote_services_wrappers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public abstract class Test implements ApplicationEventPublisherAware {

    @Autowired public ObjectCrudSvcRemoteWrapper objectCrudSvcRemoteWrapper;
    @Autowired public MissionCrudSvcRemoteWrapper missionCrudSvcRemoteWrapper;
    @Autowired public SessionsSvcRemoteWrapper sessionsSvcRemoteWrapper;
    @Autowired public QuerySvcRemoteWrapper querySvcRemoteWrapper;
    @Autowired public PerimeterCrudSvcRemoteWrapper perimeterCrudSvcRemoteWrapper;
    @Autowired public MissionsManager missionsManager;
    @Autowired public PerimetersManager perimetersManager;

    private ApplicationEventPublisher publisher;

    public ApplicationEventPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(Object event) {
        publisher.publishEvent(event);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    public enum Status {
        BEGIN,
        IN_PROGRESS,
        SUCCESS,
        WARNING,
        FAIL
    };

    public abstract Status preTestCheck();

    public abstract Status test();

    public abstract Status postTestCleanup();
}
