package is.mavlink.protocol.msgbuilder;

import is.mavlink.drone.Drone;
import is.mavlink.drone.parameters.Parameter;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_param_request_list;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_param_request_read;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_param_set;

public class MavLinkParameters {
	public static void requestParametersList(Drone drone) {
		msg_param_request_list msg = new msg_param_request_list();
		msg.target_system = 1;
		msg.target_component = 1;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendParameter(Drone drone, Parameter parameter) {
		msg_param_set msg = new msg_param_set();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.setParam_Id(parameter.name);
		msg.param_type = (byte) parameter.type;
		msg.param_value = (float) parameter.value;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void readParameter(Drone drone, String name) {
		msg_param_request_read msg = new msg_param_request_read();
		msg.param_index = -1;
		msg.target_system = 1;
		msg.target_component = 1;
		msg.setParam_Id(name);
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void readParameter(Drone drone, int index) {
		msg_param_request_read msg = new msg_param_request_read();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.param_index = (short) index;
		drone.getMavClient().sendMavPacket(msg.pack());
	}
}
