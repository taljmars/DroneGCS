package com.ngxbridge.remote_svc.internal;


import com.ngxbridge.remote_svc.CommunicationSvcRemote;
import com.ngxbridge.svc.CommunicationSvc;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
public class CommunicationSvcRemoteImpl implements CommunicationSvcRemote
{
	private final static Logger LOGGER = Logger.getLogger(CommunicationSvcRemoteImpl.class);

//	@Autowired
	private CommunicationSvc communicationSvc;
	
	@PostConstruct
	public void init() {
//		Assert.notNull(communicationSvc,"Failed to initiate 'objectCrudSvc'");
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

}
