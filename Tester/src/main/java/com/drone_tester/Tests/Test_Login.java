package com.drone_tester.Tests;

import com.drone_tester.Test;
import org.springframework.stereotype.Component;

@Component
public class Test_Login extends Test {

    private int idx = 0;
    private int total = 1;

    @Override
    public Status preTestCheck() {
        restClientHelper.setToken(login("talma5","tester1"));
        System.out.println(restClientHelper.getToken());
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        return Status.SUCCESS;
    }

    @Override
    public Status postTestCleanup() {
        logout();
        return Status.SUCCESS;
    }

}
