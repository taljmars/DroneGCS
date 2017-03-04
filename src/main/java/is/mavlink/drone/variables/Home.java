package mavlink.drone.variables;

import org.springframework.stereotype.Component;

import mavlink.drone.DroneVariable;
import mavlink.drone.DroneInterfaces.DroneEventsType;
import mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.protocol.msg_metadata.enums.MAV_FRAME;
import tools.geoTools.Coordinate;
import tools.geoTools.GeoTools;

@Component("home")
public class Home extends DroneVariable {
	
	private Coordinate coordinate;

	public boolean isValid() {
		return (coordinate != null);
	}

	public Home getHome() {
		return this;
	}

	public double getDroneDistanceToHome() {
		if (isValid() && drone.getGps().isPositionValid()) {
			return GeoTools.getDistance(coordinate, drone.getGps().getPosition());
		} else {
			return 0; // TODO fix this
		}
	}

	public Coordinate getCoord() {
		return coordinate;
	}

	public double getAltitude() {
		return coordinate.getAltitude();
	}

	public void setHome(msg_mission_item msg) {
		this.coordinate = new Coordinate(msg.x, msg.y);
		drone.notifyDroneEvent(DroneEventsType.HOME);
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
			mavMsg.y = (float) getCoord().getLon();
			mavMsg.z = (float) getCoord().getAltitude();
		}

		return mavMsg;
	}

}
