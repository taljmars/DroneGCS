package com.ngxbridge.remote_svc;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Component
public interface CommunicationSvcRemote
{
	@RequestMapping(value = "/isUp", method = RequestMethod.GET)
	@ResponseBody
	<T extends String> ResponseEntity<T> isUp(@RequestParam String userName);

	@RequestMapping(value = "/listPort", method = RequestMethod.GET)
	@ResponseBody
	<T extends String> ResponseEntity<List<T>> listPort();

	@RequestMapping(value = "/sync", method = RequestMethod.GET)
	@ResponseBody
	ResponseEntity<Void> sync();
}
