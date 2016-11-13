package mavlink.is.drone.mission.waypoints.interfaces;

import mavlink.is.utils.units.Altitude;

public interface Altitudable {
	
	public void setAltitude(Altitude altitude);
	
	public Altitude getAltitude();

}
