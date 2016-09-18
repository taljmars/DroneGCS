package mavlink.is.drone.variables;

import mavlink.core.drone.MyDroneImpl;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.drone.Preferences.Rates;
import mavlink.is.protocol.msgbuilder.MavLinkStreamRates;

public class StreamRates extends DroneVariable implements OnDroneListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4572905995884327453L;

	public StreamRates(MyDroneImpl myDroneImpl) {
		super(myDroneImpl);
		myDroneImpl.addDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case HEARTBEAT_FIRST:
		case HEARTBEAT_RESTORED:
			setupStreamRatesFromPref();
			break;
		default:
			break;
		}
	}

	public void setupStreamRatesFromPref() {
		Rates rates = myDrone.getPreferences().getRates();

		MavLinkStreamRates.setupStreamRates(myDrone.getMavClient(), rates.extendedStatus,
				rates.extra1, rates.extra2, rates.extra3, rates.position, rates.rcChannels,
				rates.rawSensors, rates.rawController);
	}

}
