package com.drone_tester.Tests;

import com.db.persistence.scheme.RegistrationRequest;
import com.db.persistence.scheme.RegistrationResponse;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class Test_UserRegistration extends Test {

    private int idx = 0;
    private int total = 4;

    @Override
    public Status preTestCheck() {
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        try {
            RegistrationRequest registrationRequest = new RegistrationRequest();
            registrationRequest.setUserName("tester50");
            registrationRequest.setPassword("1234");
            RegistrationResponse registrationResponse = registrationSvcRemoteWrapper.registerNewUser(registrationRequest);
            Assert.isTrue(registrationResponse.getReturnCode().equals(0));
            publish(new TestEvent(this, Status.IN_PROGRESS, "User registered succeeded", ++idx, total));

            registrationResponse = registrationSvcRemoteWrapper.registerNewUser(registrationRequest);
            Assert.isTrue(!registrationResponse.getReturnCode().equals(0));
            publish(new TestEvent(this, Status.IN_PROGRESS, "User try to register over existing user failed as expected", ++idx, total));

            registrationRequest.setUserName("tester51");
            registrationRequest.setPassword("1234");
            registrationResponse = registrationSvcRemoteWrapper.registerNewUser(registrationRequest);
            Assert.isTrue(registrationResponse.getReturnCode().equals(0));
            publish(new TestEvent(this, Status.IN_PROGRESS, "User registered succeeded", ++idx, total));
        }
        catch (Exception e) {
            publish(new TestEvent(this, Status.FAIL, "Test failed: " + e.getMessage(), ++idx, total));
            return Status.FAIL;
        }
        return Status.SUCCESS;
    }

    @Override
    public Status postTestCleanup() {
        publish(new TestEvent(this, Status.SUCCESS, "test completed", ++idx, total));
        return Status.SUCCESS;
    }

}
