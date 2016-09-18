package mavlink.is.protocol.msgbuilder;

import gui.jmapviewer.impl.Coordinate;
import mavlink.is.drone.Drone;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_command_long;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.utils.units.Altitude;

public class MavLinkTakeoff {
	public static void sendTakeoff(Drone drone, Altitude alt) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;

		msg.param7 = (float) alt.valueInMeters();

		drone.getMavClient().sendMavPacket(msg.pack());
	}
	
	public static void sendTakeoff(Drone drone, Coordinate target_coord, Altitude alt) {
		double latitude = target_coord.getLat();
		double longitude = target_coord.getLon();
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;

		msg.param5 = (float) latitude;
		msg.param6 = (float) longitude;
		msg.param7 = (float) alt.valueInMeters();

		drone.getMavClient().sendMavPacket(msg.pack());
	}
}
