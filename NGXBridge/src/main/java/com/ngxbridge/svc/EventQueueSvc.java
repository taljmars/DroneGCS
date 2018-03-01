package com.ngxbridge.svc;

import java.util.List;

public interface EventQueueSvc {

    EventMessage getMessage();

    List<EventMessage> getMessages(int amount);
}
