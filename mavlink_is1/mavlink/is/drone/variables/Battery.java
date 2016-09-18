package mavlink.is.drone.variables;

import mavlink.core.drone.MyDroneImpl;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.parameters.Parameter;

public class Battery extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7537253971158658371L;
	private double battVolt = -1;
	private double battRemain = -1;
	private double battCurrent = -1;

	public Battery(MyDroneImpl myDroneImpl) {
		super(myDroneImpl);
	}

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
		Parameter battCap = myDrone.getParameters().getParameter("BATT_CAPACITY");
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
			myDrone.notifyDroneEvent(DroneEventsType.BATTERY);
		}
	}

}