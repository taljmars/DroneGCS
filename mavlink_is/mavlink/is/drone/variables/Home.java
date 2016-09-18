package mavlink.is.drone.variables;

import mavlink.core.drone.MyDroneImpl;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.protocol.msg_metadata.enums.MAV_FRAME;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.geoTools.GeoTools;
import mavlink.is.utils.units.Altitude;
import mavlink.is.utils.units.Length;

public class Home extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6657078500448176430L;
	private Coord2D coordinate;
	private Altitude altitude = new Altitude(0);

	public Home(MyDroneImpl myDroneImpl) {
		super(myDroneImpl);
	}

	public boolean isValid() {
		return (coordinate != null);
	}

	public Home getHome() {
		return this;
	}

	public Length getDroneDistanceToHome() {
		if (isValid() && myDrone.getGps().isPositionValid()) {
			return GeoTools.getDistance(coordinate, myDrone.getGps().getPosition());
		} else {
			return new Length(0); // TODO fix this
		}
	}

	public Coord2D getCoord() {
		return coordinate;
	}

	public Length getAltitude() {
		return altitude;
	}

	public void setHome(msg_mission_item msg) {
		this.coordinate = new Coord2D(msg.x, msg.y);
		this.altitude = new Altitude(msg.z);
		myDrone.notifyDroneEvent(DroneEventsType.HOME);
	}

	public msg_mission_item packMavlink() {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		mavMsg.current = 0;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		if (isValid()) {
			mavMsg.x = (float) getCoord().getLat();
			mavMsg.y = (float) getCoord().getLng();
			mavMsg.z = (float) getAltitude().valueInMeters();
		}

		return mavMsg;
	}

}
