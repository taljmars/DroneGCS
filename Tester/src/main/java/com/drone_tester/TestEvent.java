package com.drone_tester;

import org.springframework.context.ApplicationEvent;

public class TestEvent extends ApplicationEvent {

    private Test.Status status;
    private String msg;
    private int msgId;
    private int msgAmount;
    private Test test;

    public TestEvent(Test test, Test.Status status, String msg, int msgId, int totalMsg) {
        super(test);
        this.status = status;
        this.msg = msg;
        this.msgId = msgId;
        this.msgAmount = totalMsg;
        this.test = test;
    }

    public Test.Status getStatus() {
        return status;
    }

    public void setStatus(Test.Status status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getMsgAmount() {
        return msgAmount;
    }

    public void setMsgAmount(int msgAmount) {
        this.msgAmount = msgAmount;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }
}
