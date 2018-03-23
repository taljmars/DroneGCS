package com.drone_tester.Tests;

import com.db.persistence.scheme.RegistrationRequest;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import org.springframework.stereotype.Component;

@Component
public class Test_UserRegistration extends Test {

    private int idx = 0;
    private int total = 1;

    @Override
    public Status preTestCheck() {
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        try {
            RegistrationRequest registrationRequest = new RegistrationRequest();
            registrationRequest.setUserName("tal1");
            registrationRequest.setPassword("1234");
            registrationSvcRemoteWrapper.registerNewUser(registrationRequest, "1234");
            publish(new TestEvent(this, Status.IN_PROGRESS, "User registered succeeded", ++idx, total));
        }
        catch (Exception e) {
            return Status.FAIL;
        }
        return Status.SUCCESS;
    }

    @Override
    public Status postTestCleanup() {
        return Status.SUCCESS;
    }

}
