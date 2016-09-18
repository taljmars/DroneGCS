package mavlink.is.drone;

import java.io.Serializable;

public class DroneVariable  implements Serializable /*TALMA Serializele*/  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 191659366278354844L;
	protected transient Drone myDrone;

	public DroneVariable(Drone myDrone2) {
		this.myDrone = myDrone2;
	}

	public void setDrone(Drone drone) {
		myDrone = drone;
	}
}