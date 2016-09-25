package mavlink.is.drone.variables;

import java.util.Vector;

import org.springframework.stereotype.Component;

import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;

@Component("messeges")
public class Messeges extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8178265797887679595L;
	private Vector<String> messeges;
	
	public void init() {
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
		drone.notifyDroneEvent(DroneEventsType.TEXT_MESSEGE);
	}
}
