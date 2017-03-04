package is.mavlink.drone.variables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import is.gui.services.LoggerDisplayerSvc;
import is.mavlink.drone.Drone;
import is.mavlink.drone.DroneVariable;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.drone.DroneInterfaces.OnDroneListener;
import is.mavlink.drone.Preferences.Rates;
import is.mavlink.protocol.msgbuilder.MavLinkStreamRates;

@Component("streamRates")
public class StreamRates extends DroneVariable implements OnDroneListener {
	
	@Autowired
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	private boolean streamRatesWasSet = false;
	
	static int called;
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		drone.addDroneListener(this);
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
		if (streamRatesWasSet)
			return;
		
		loggerDisplayerSvc.logGeneral("Setting up stream rates");
		Rates rates = drone.getPreferences().getRates();

		MavLinkStreamRates.setupStreamRates(drone.getMavClient(), rates.extendedStatus,
				rates.extra1, rates.extra2, rates.extra3, rates.position, rates.rcChannels,
				rates.rawSensors, rates.rawController);
		
		streamRatesWasSet = true;
	}
	
	public void prepareStreamRates() {
		streamRatesWasSet = false;
	}

}
