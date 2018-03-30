package com.drone_tester.func_tests;

import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import org.springframework.stereotype.Component;

@Component
public class Test_Login extends Test {

    private int idx = 0;
    private int total = 3;

    @Override
    public Status preTestCheck() {
        restClientHelper.setToken(login("tester1","tester1"));
        publish(new TestEvent(this, Status.IN_PROGRESS, "login successfully", ++idx, total));
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        try {
            loginSvcRemoteWrapper.loginKeepAlive();
            publish(new TestEvent(this, Status.IN_PROGRESS, "keep alive succeeded", ++idx, total));
        }
        catch (Exception e) {
            return Status.FAIL;
        }
        return Status.SUCCESS;
    }

    @Override
    public Status postTestCleanup() {
        try {
            logout();
            publish(new TestEvent(this, Status.SUCCESS, "test completed", ++idx, total));
        }
        catch (Exception e) {
            return Status.FAIL;
        }
        return Status.SUCCESS;
    }

}
