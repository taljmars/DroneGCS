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

import static com.db.persistence.scheme.LoginLogoutStatus.OK;

public abstract class Test implements ApplicationEventPublisherAware {

    @Autowired protected ObjectCrudSvcRemoteWrapper objectCrudSvcRemoteWrapper;
    @Autowired protected MissionCrudSvcRemoteWrapper missionCrudSvcRemoteWrapper;
    @Autowired protected SessionsSvcRemoteWrapper sessionsSvcRemoteWrapper;
    @Autowired protected QuerySvcRemoteWrapper querySvcRemoteWrapper;
    @Autowired protected PerimeterCrudSvcRemoteWrapper perimeterCrudSvcRemoteWrapper;
    @Autowired protected MissionsManager missionsManager;
    @Autowired protected PerimetersManager perimetersManager;
    @Autowired protected LoginSvcRemoteWrapper loginSvcRemoteWrapper;

    @Autowired protected RestClientHelper restClientHelper;

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

    protected String login(String userName, String pass) {
        LoginRequest req = new LoginRequest();
        req.setUserName(userName);
        req.setApplicationName("GUI Tester");
        req.setTimeout(400);
        LoginResponse resp = loginSvcRemoteWrapper.login(req, pass);
        if (!resp.getReturnCode().equals(OK)) {
            //TODO: have better messaging
            System.out.println("Failed to login: " + resp.getMessage());
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
}
