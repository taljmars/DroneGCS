package com.ngxbridge.svc.internal;

import com.dronegcs.console_plugin.services.internal.logevents.LogAbstractDisplayerEvent;
import com.dronegcs.console_plugin.services.internal.logevents.LogGeneralDisplayerEvent;
import com.generic_tools.logger.Logger;
import com.ngxbridge.svc.EventMessage;
import com.ngxbridge.svc.EventQueueSvc;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

@Component
public class EventQueueSvcImpl implements EventQueueSvc {

    private Queue<EventMessage> queue;

    @PostConstruct
    private void init() {
        queue = new ArrayBlockingQueue<EventMessage>(5000);
        for (int i = 0 ; i < 5000 ; i++)
            queue.add(new EventMessage(Logger.Type.GENERAL, "TAL " + (new Date()).toString()));
    }

    @Override
    public EventMessage getMessage() {
        return queue.poll();
    }

    @Override
    public List<EventMessage> getMessages(int amount) {
        List<EventMessage> lst = new ArrayList<>();
        for (int i = 0 ; i < queue.size() && i < amount ; i++) {
            lst.add(queue.poll());
        }
        return lst;
    }

    private void addGeneralMessegeToDisplay(String entry) {
        queue.add(new EventMessage(Logger.Type.GENERAL, entry));
    }

    private void addIncommingMessegeToDisplay(String entry) {
        queue.add(new EventMessage(Logger.Type.INCOMING, entry));
    }

    private void addOutgoingMessegeToDisplay(String entry) {
        queue.add(new EventMessage(Logger.Type.OUTGOING, entry));
    }

    private void addErrorMessegeToDisplay(String entry) {
        queue.add(new EventMessage(Logger.Type.ERROR, entry));
    }

    @EventListener
    public void onLogDisplayerEvent(LogAbstractDisplayerEvent event) {
        switch (event.getType()) {
            case ERROR:
                addErrorMessegeToDisplay(event.getEntry());
                break;
            case GENERAL:
                addGeneralMessegeToDisplay(event.getEntry());
                break;
            case INCOMING:
                addIncommingMessegeToDisplay(event.getEntry());
                break;
            case OUTGOING:
                addOutgoingMessegeToDisplay(event.getEntry());
                break;
        }
    }
}
