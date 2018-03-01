package com.ngxbridge.remote_svc.internal;

import com.ngxbridge.remote_svc.EventQueueSvcRemote;
import com.ngxbridge.svc.CommunicationSvc;
import com.ngxbridge.svc.EventMessage;
import com.ngxbridge.svc.EventQueueSvc;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@RestController
public class EventQueueSvcRemoteImpl implements EventQueueSvcRemote {

    private final static Logger LOGGER = Logger.getLogger(EventQueueSvcRemoteImpl.class);

    @Autowired
    private EventQueueSvc eventQueueSvc;

    @PostConstruct
    public void init() {
		Assert.notNull(eventQueueSvc,"Failed to initiate 'EventQueueSvc'");
        LOGGER.debug("Remote Service is up !" + this.getClass().getSimpleName());
        System.out.println("Remote Service is up !" + this.getClass().getSimpleName());
    }

    @Override
    @RequestMapping(value = "/getMessage", method = RequestMethod.GET)
    @ResponseBody
    public <T extends EventMessage> ResponseEntity<T> getMessage() {
        LOGGER.debug("Remote getMessage");
        EventMessage a = eventQueueSvc.getMessage();
        return new ResponseEntity<T>((T) a, HttpStatus.OK);
//        return new ResponseEntity<T>((T) new EventMessage(com.generic_tools.logger.Logger.Type.ERROR, "tal"), HttpStatus.OK);
    }

    @Override
    @RequestMapping(value = "/getMessages", method = RequestMethod.GET)
    @ResponseBody
    public <T extends EventMessage> ResponseEntity<List<T>> getMessages(@RequestParam Integer amount) {
        LOGGER.debug("Remote getMessages, amount=" + amount);
        List<EventMessage> msgs = eventQueueSvc.getMessages(amount);
        return new ResponseEntity(msgs, HttpStatus.OK);
    }

}
