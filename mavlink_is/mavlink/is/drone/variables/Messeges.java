package mavlink.is.drone.variables;

import java.util.Vector;

import mavlink.core.drone.MyDroneImpl;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;

public class Messeges extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8178265797887679595L;
	private Vector<String> messeges;
	
	public Messeges(MyDroneImpl myDroneImpl) {
		super(myDroneImpl);
		messeges = new Vector<String>();
	}
	
	public String pop() {
		if (messeges == null)
			return null;
		
		if (messeges.isEmpty())
			return null;
		
		String res = messeges.get(0);
		messeges.remove(0);
		
		return res;
	}
	
	public void push(String text) {
		messeges.addElement(text);
		myDrone.notifyDroneEvent(DroneEventsType.TEXT_MESSEGE);
	}
}