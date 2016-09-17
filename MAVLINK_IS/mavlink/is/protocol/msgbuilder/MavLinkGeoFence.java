package mavlink.is.protocol.msgbuilder;

import mavlink.is.drone.Drone;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_command_long;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;

public class MavLinkGeoFence {
	public static void setGeoFence(Drone drone, boolean isOn) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.command = MAV_CMD.MAV_CMD_DO_FENCE_ENABLE;
		msg.param1 = (isOn) ? 1 : 0;
		drone.getMavClient().sendMavPacket(msg.pack());
	}
}
