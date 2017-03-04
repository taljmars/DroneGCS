package is.mavlink.protocol.msgbuilder;

import is.mavlink.drone.Drone;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_ack;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_count;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_request;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_request_list;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_set_current;
import is.mavlink.protocol.msg_metadata.enums.MAV_MISSION_RESULT;

public class MavLinkWaypoint {

	public static void sendAck(Drone drone) {
		msg_mission_ack msg = new msg_mission_ack();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;
		drone.getMavClient().sendMavPacket(msg.pack());

	}

	public static void requestWayPoint(Drone drone, int index) {
		msg_mission_request msg = new msg_mission_request();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.seq = (short) index;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void requestWaypointsList(Drone drone) {
		msg_mission_request_list msg = new msg_mission_request_list();
		msg.target_system = 1;
		msg.target_component = 1;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendWaypointCount(Drone drone, int count) {
		msg_mission_count msg = new msg_mission_count();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.count = (short) count;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendSetCurrentWaypoint(Drone drone, short i) {
		msg_mission_set_current msg = new msg_mission_set_current();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.seq = i;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

}
