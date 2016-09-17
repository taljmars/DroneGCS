package desktop.logic;

public class Clock implements mavlink.core.drone.DroneInterfaces.Clock {
	@Override
	public long elapsedRealtime() {
		return System.currentTimeMillis();
	}
};