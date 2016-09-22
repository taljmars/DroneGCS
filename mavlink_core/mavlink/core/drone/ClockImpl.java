package mavlink.core.drone;

import mavlink.is.drone.DroneInterfaces.Clock;

public class ClockImpl implements Clock {
	@Override
	public long elapsedRealtime() {
		return System.currentTimeMillis();
	}
};