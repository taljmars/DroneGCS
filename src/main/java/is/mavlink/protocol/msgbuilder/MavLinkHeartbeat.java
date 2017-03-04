package is.mavlink.protocol.msgbuilder;

import is.mavlink.drone.Drone;
import is.mavlink.protocol.msg_metadata.MAVLinkPacket;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_heartbeat;
import is.mavlink.protocol.msg_metadata.enums.MAV_AUTOPILOT;
import is.mavlink.protocol.msg_metadata.enums.MAV_TYPE;

/**
 * This class contains logic used to send an heartbeat to a
 * {@link is.mavlink.drone.Drone}.
 */
public class MavLinkHeartbeat {

	/**
	 * This is the msg heartbeat used to check the drone is present, and
	 * responding.
	 */
	private static final msg_heartbeat sMsg = new msg_heartbeat();
	static {
		sMsg.type = MAV_TYPE.MAV_TYPE_GCS;
		sMsg.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC;
	}

	/**
	 * This is the mavlink packet obtained from the msg heartbeat, and used for
	 * actual communication.
	 */
	private static final MAVLinkPacket sMsgPacket = sMsg.pack();

	/**
	 * Sends the heartbeat to the {@link mavlink.drone.Drone}
	 * object.
	 * 
	 * @param drone
	 *            drone to send the heartbeat to
	 */
	public static void sendMavHeartbeat(Drone drone) {
		if (drone != null)
			drone.getMavClient().sendMavPacket(sMsgPacket);
	}

}
