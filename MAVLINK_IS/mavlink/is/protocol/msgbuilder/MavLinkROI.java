package mavlink.is.protocol.msgbuilder;

import mavlink.is.drone.Drone;
import mavlink.is.drone.mission.commands.EpmGripper;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_command_long;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_digicam_control;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.utils.coordinates.Coord3D;

public class MavLinkROI {
	public static void setROI(Drone drone, Coord3D coord) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.command = MAV_CMD.MAV_CMD_DO_SET_ROI;

		msg.param5 = (float) coord.getX();
		msg.param6 = (float) coord.getY();
		msg.param7 = (float) 0.0;

		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void resetROI(Drone drone) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.command = MAV_CMD.MAV_CMD_DO_SET_ROI;

		msg.param5 = (float) 0.0;
		msg.param6 = (float) 0.0;
		msg.param7 = (float) 0.0;

		drone.getMavClient().sendMavPacket(msg.pack());
	}
	
	public static void triggerCamera(Drone drone){
		msg_digicam_control msg = new msg_digicam_control();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.shot = 1;
		drone.getMavClient().sendMavPacket(msg.pack());
	}
	
	public static void empCommand(Drone drone, boolean release) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.command = EpmGripper.MAV_CMD_DO_GRIPPER;
		msg.param2 = release ? EpmGripper.GRIPPER_ACTION_RELEASE : EpmGripper.GRIPPER_ACTION_GRAB;

		drone.getMavClient().sendMavPacket(msg.pack());
	}
	
}
