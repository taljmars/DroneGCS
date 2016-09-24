package mavlink.core.drone;

import org.springframework.stereotype.Component;

import mavlink.is.drone.DroneInterfaces.Clock;

@Component("clockImpl")
public class ClockImpl implements Clock {
	
	@Override
	public long elapsedRealtime() {
		return System.currentTimeMillis();
	}
};