package com.ngxbridge.remote_svc;

import com.ngxbridge.svc.EventMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

public interface EventQueueSvcRemote {

    @RequestMapping(value = "/getMessage", method = RequestMethod.GET)
    @ResponseBody
    <T extends EventMessage> ResponseEntity<T> getMessage();

    @RequestMapping(value = "/getMessages", method = RequestMethod.GET)
    @ResponseBody
    <T extends EventMessage> ResponseEntity<List<T>> getMessages(@RequestParam Integer amount);
}
