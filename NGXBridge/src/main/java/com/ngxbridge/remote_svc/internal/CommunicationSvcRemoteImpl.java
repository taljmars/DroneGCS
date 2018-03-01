package com.ngxbridge.remote_svc.internal;


import com.ngxbridge.remote_svc.CommunicationSvcRemote;
import com.ngxbridge.svc.CommunicationSvc;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@RestController
public class CommunicationSvcRemoteImpl implements CommunicationSvcRemote
{
	private final static Logger LOGGER = Logger.getLogger(CommunicationSvcRemoteImpl.class);

	@Autowired
	private CommunicationSvc communicationSvc;
	
	@PostConstruct
	public void init() {
		Assert.notNull(communicationSvc,"Failed to initiate 'CommunicationSvc'");
		LOGGER.debug("Remote Service is up !" + this.getClass().getSimpleName());
		System.out.println("Remote Service is up !" + this.getClass().getSimpleName());
	}

	@Override
	@RequestMapping(value = "/isUp", method = RequestMethod.GET)
	@ResponseBody
	public <T extends String> ResponseEntity<T> isUp(@RequestParam String userName) {
		LOGGER.debug("Crud REMOTE CREATE called 'isUp'");
		System.out.println("Win! " + userName);
		String a = new String("Win! " + userName);
		return new ResponseEntity<T>((T) a, HttpStatus.OK);
	}

	@Override
	@RequestMapping(value = "/listPort", method = RequestMethod.GET)
	@ResponseBody
	public <T extends String> ResponseEntity<List<T>> listPort() {
		LOGGER.debug("Crud REMOTE CREATE called 'isUp'");
		Object[] objs = communicationSvc.listPorts();
		List<String> list = new ArrayList<>();
		for (Object obj : objs) {
			System.out.println(obj);
			list.add((String) obj);
		}
		return new ResponseEntity(list, HttpStatus.OK);
	}

	@Override
	@RequestMapping(value = "/sync", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Void> sync() {
		communicationSvc.sync();
		return null;
	}

}
