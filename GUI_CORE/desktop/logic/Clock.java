package desktop.logic;

public class Clock implements mavlink.is.drone.DroneInterfaces.Clock {
	@Override
	public long elapsedRealtime() {
		return System.currentTimeMillis();
	}
};