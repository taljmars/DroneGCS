package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.drone.MyDroneImpl;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;

public class GPS extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6356840209453851257L;
	public final static int LOCK_2D = 2;
	public final static int LOCK_3D = 3;

	private double gps_eph = -1;
	private int satCount = -1;
	private int fixType = -1;
	private Coord2D position;
	private double distanceTraveled;

	public GPS(MyDroneImpl myDroneImpl) {
		super(myDroneImpl);
		distanceTraveled = 0;
	}

	public boolean isPositionValid() {
		return (position != null);
	}

	public Coord2D getPosition() {
		return position;
	}

	public double getGpsEPH() {
		return gps_eph;
	}

	public int getSatCount() {
		return satCount;
	}

	public String getFixType() {
		String gpsFix = "";
		switch (fixType) {
		case LOCK_2D:
			gpsFix = ("2D");
			break;
		case LOCK_3D:
			gpsFix = ("3D");
			break;
		default:
			gpsFix = ("NoFix");
			break;
		}
		return gpsFix;
	}

	public int getFixTypeNumeric() {
		return fixType;
	}

	public void setGpsState(int fix, int satellites_visible, int eph) {
		if (satCount != satellites_visible) {
			satCount = satellites_visible;
			gps_eph = (double) eph / 100; // convert from eph(cm) to gps_eph(m)
			myDrone.notifyDroneEvent(DroneEventsType.GPS_COUNT);
		}
		if (fixType != fix) {
			fixType = fix;
			myDrone.notifyDroneEvent(DroneEventsType.GPS_FIX);
		}
	}

	public void setPosition(Coord2D position) {
		if (this.position != position) {
			if (this.position != null && myDrone.getState().isFlying())
				distanceTraveled += GeoTools.getDistance(this.position, position).valueInMeters();
			this.position = position;
			myDrone.notifyDroneEvent(DroneEventsType.GPS);
		}
	}
	
	public double getDistanceTraveled() {
		return distanceTraveled;
	}
}