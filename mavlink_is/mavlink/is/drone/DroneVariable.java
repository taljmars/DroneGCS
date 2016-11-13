package mavlink.is.drone;

import java.io.Serializable;

import javax.annotation.Resource;

public class DroneVariable  implements Serializable /*TALMA Serializele*/  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 191659366278354844L;
	
	@Resource(name = "drone")
	protected transient Drone drone;

	public DroneVariable() {

	}
	
	public DroneVariable(DroneVariable dv) {
		drone = dv.drone;
	}

	public void setDrone(Drone myDrone) {
		drone = myDrone;
	}
}