package is.mavlink.drone.variables;


import org.springframework.stereotype.Component;

import is.mavlink.drone.DroneVariable;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.drone.parameters.Parameter;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_raw_imu;

@Component("mag")
public class Magnetometer extends DroneVariable {

	private int x;
	private int y;
	private int z;

	public void newData(msg_raw_imu msg_imu) {
		x = msg_imu.xmag;
		y = msg_imu.ymag;
		z = msg_imu.zmag;
		drone.notifyDroneEvent(DroneEventsType.MAGNETOMETER);
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
		Parameter paramX = drone.getParameters().getParameter("COMPASS_OFS_X");
		Parameter paramY = drone.getParameters().getParameter("COMPASS_OFS_Y");
		Parameter paramZ = drone.getParameters().getParameter("COMPASS_OFS_Z");
		if (paramX == null || paramY == null || paramZ == null) {
			return null;
		}
		return new int[]{(int) paramX.value,(int) paramY.value,(int) paramZ.value};

	}
}
