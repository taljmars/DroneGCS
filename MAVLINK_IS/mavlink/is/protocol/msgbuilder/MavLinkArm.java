package mavlink.is.protocol.msgbuilder;

import mavlink.is.drone.Drone;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_command_long;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.protocol.msg_metadata.enums.MAV_COMPONENT;

public class MavLinkArm {

	public static void sendArmMessage(Drone drone, boolean arm) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = (byte) MAV_COMPONENT.MAV_COMP_ID_SYSTEM_CONTROL;

		msg.command = MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM;
		msg.param1 = arm ? 1 : 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

}