package mavlink.is.drone.variables;

import mavlink.core.drone.MyDroneImpl;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_rc_channels_raw;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_servo_output_raw;

public class RC extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6270111560061361734L;
	public int in[] = new int[8];
	public int out[] = new int[8];

	public RC(MyDroneImpl myDroneImpl) {
		super(myDroneImpl);
	}

	public void setRcInputValues(msg_rc_channels_raw msg) {
		in[0] = msg.chan1_raw;
		in[1] = msg.chan2_raw;
		in[2] = msg.chan3_raw;
		in[3] = msg.chan4_raw;
		in[4] = msg.chan5_raw;
		in[5] = msg.chan6_raw;
		in[6] = msg.chan7_raw;
		in[7] = msg.chan8_raw;
		myDrone.notifyDroneEvent(DroneEventsType.RC_IN);
	}

	public void setRcOutputValues(msg_servo_output_raw msg) {
		out[0] = msg.servo1_raw;
		out[1] = msg.servo2_raw;
		out[2] = msg.servo3_raw;
		out[3] = msg.servo4_raw;
		out[4] = msg.servo5_raw;
		out[5] = msg.servo6_raw;
		out[6] = msg.servo7_raw;
		out[7] = msg.servo8_raw;
		myDrone.notifyDroneEvent(DroneEventsType.RC_OUT);
	}
	
	public int getAverageThrust() {
		int e1 = out[0];
		int e2 = out[1];
		int e3 = out[2];
		int e4 = out[3];
		
		int eAvg = (e1 + e2 + e3 + e4) / 4;
		
		return eAvg;
	}

}