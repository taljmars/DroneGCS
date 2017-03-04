package is.mavlink.drone.variables;


import org.springframework.stereotype.Component;

import is.mavlink.drone.DroneVariable;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.drone.parameters.Parameter;

@Component("battery")
public class Battery extends DroneVariable {

	private double battVolt = -1;
	private double battRemain = -1;
	private double battCurrent = -1;


	public double getBattVolt() {
		return battVolt;
	}

	public double getBattRemain() {
		return battRemain;
	}

	public double getBattCurrent() {
		return battCurrent;
	}

	public Double getBattDischarge() {
		Parameter battCap = drone.getParameters().getParameter("BATT_CAPACITY");
		if (battCap == null || battRemain == -1) {
			return null;			
		}
		return (1-battRemain/100.0)*battCap.value; 
	}
	
	public void setBatteryState(double battVolt, double battRemain, double battCurrent) {
		if (this.battVolt != battVolt | this.battRemain != battRemain
				| this.battCurrent != battCurrent) {
			this.battVolt = battVolt;
			this.battRemain = battRemain;
			this.battCurrent = battCurrent;
			drone.notifyDroneEvent(DroneEventsType.BATTERY);
		}
	}

}