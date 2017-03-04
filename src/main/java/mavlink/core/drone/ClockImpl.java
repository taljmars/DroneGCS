package mavlink.core.drone;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import is.mavlink.drone.DroneInterfaces.Clock;

@Component("clock")
public class ClockImpl implements Clock {
	
	@Override
	public long elapsedRealtime() {
		return System.currentTimeMillis();
	}
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
};