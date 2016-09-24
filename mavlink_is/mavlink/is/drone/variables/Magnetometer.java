package mavlink.is.drone.variables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.parameters.Parameter;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_raw_imu;

@Component("mag")
public class Magnetometer extends DroneVariable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5112533142120514573L;
	private int x;
	private int y;
	private int z;

	@Autowired
	public Magnetometer(Drone myDrone) {
		super(myDrone);
	}

	public void newData(msg_raw_imu msg_imu) {
		x = msg_imu.xmag;
		y = msg_imu.ymag;
		z = msg_imu.zmag;
		myDrone.notifyDroneEvent(DroneEventsType.MAGNETOMETER);
	}

	public int[] getVector() {
		return new int[] { x, y, z };
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public int[] getOffsets() {
		Parameter paramX = myDrone.getParameters().getParameter("COMPASS_OFS_X");
		Parameter paramY = myDrone.getParameters().getParameter("COMPASS_OFS_Y");
		Parameter paramZ = myDrone.getParameters().getParameter("COMPASS_OFS_Z");
		if (paramX == null || paramY == null || paramZ == null) {
			return null;
		}
		return new int[]{(int) paramX.value,(int) paramY.value,(int) paramZ.value};

	}
}
