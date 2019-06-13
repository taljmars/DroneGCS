package com.drone_tester;

import com.db.persistence.scheme.LoginRequest;
import com.db.persistence.scheme.LoginResponse;
import com.db.persistence.scheme.LogoutResponse;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.dronegcs.console_plugin.remote_services_wrappers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.util.*;

import static com.db.persistence.scheme.LoginLogoutStatus.OK;

public abstract class Test implements ApplicationEventPublisherAware {

    public static String server = "localhost";
    //public static String server = "18.220.242.169";
    public static int port = 8080;
    public static String tester1 = "tester1";
    public static String tester2 = "tester2";

    @Autowired protected ObjectCrudSvcRemoteWrapper objectCrudSvcRemoteWrapper;
//    @Autowired protected MissionCrudSvcRemoteWrapper missionCrudSvcRemoteWrapper;
    @Autowired protected SessionsSvcRemoteWrapper sessionsSvcRemoteWrapper;
    @Autowired protected QuerySvcRemoteWrapper querySvcRemoteWrapper;
    @Autowired protected PerimeterCrudSvcRemoteWrapper perimeterCrudSvcRemoteWrapper;
    @Autowired protected RegistrationSvcRemoteWrapper registrationSvcRemoteWrapper;
    @Autowired protected LoginSvcRemoteWrapper loginSvcRemoteWrapper;
    @Autowired protected MissionsManager missionsManager;
    @Autowired protected PerimetersManager perimetersManager;

    @Autowired protected RestClientHelper restClientHelper;

    private ApplicationEventPublisher publisher;
    protected List<List<Object>> detailedResult;

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

    protected String login(String userName, String pass) {
        LoginRequest req = new LoginRequest();
        req.setUserName(userName);
        req.setApplicationName("GUI Tester");
        req.setTimeout(400);
        LoginResponse resp = loginSvcRemoteWrapper.login(req, pass, server, port);
        if (!resp.getReturnCode().equals(OK)) {
            //TODO: have better messaging
            System.err.println("Failed to login: " + resp.getMessage());
            throw new RuntimeException("Failed to login");
        }
        return resp.getToken();
    }

    protected void logout() {
        LogoutResponse resp = loginSvcRemoteWrapper.logout();
        if (!resp.getReturnCode().equals(OK)) {
            //TODO: have better messaging
            System.out.println("Failed to logout: " + resp.getMessage());
            throw new RuntimeException("Failed to logout");
        }
    }

    protected List<List<Object>> getDetailsTable() {
        return detailedResult;
    }

    /************************************************/
    /* **********  Duration capabilities ************/

    private Map<Object,Long> clocksMap = new HashMap();
    private Object defaultClock = new Object();

    protected void startClock() {
        startClock(defaultClock);
    }

    protected Long stopClock() {
        return stopClock(defaultClock);
    }

    protected Long pickClock() {
        return pickClock(defaultClock);
    }

    protected void startClock(Object obj) {
        Long timestamp = new Date().getTime();
        if (clocksMap.containsKey(obj))
            clocksMap.replace(obj, timestamp);
        else
            clocksMap.put(obj, timestamp);
    }

    protected Long stopClock(Object obj) {
        Long timestamp = new Date().getTime();
        return timestamp - clocksMap.replace(obj, timestamp);
    }

    protected Long pickClock(Object obj) {
        return new Date().getTime() - clocksMap.get(obj);
    }
}
