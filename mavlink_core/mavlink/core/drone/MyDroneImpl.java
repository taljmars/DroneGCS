package mavlink.core.drone;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import mavlink.core.firmware.FirmwareType;
import mavlink.is.connection.MavLinkConnection;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneEvents;
import mavlink.is.drone.DroneInterfaces;
import mavlink.is.drone.Preferences;
import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.profiles.Parameters;
import mavlink.is.drone.profiles.VehicleProfile;
import mavlink.is.drone.variables.Altitude;
import mavlink.is.drone.variables.Battery;
import mavlink.is.drone.variables.Beacon;
import mavlink.is.drone.variables.Calibration;
import mavlink.is.drone.variables.CameraFootprints;
import mavlink.is.drone.variables.GCS;
import mavlink.is.drone.variables.GPS;
import mavlink.is.drone.variables.GuidedPoint;
import mavlink.is.drone.variables.HeartBeat;
import mavlink.is.drone.variables.Home;
import mavlink.is.drone.variables.Magnetometer;
import mavlink.is.drone.variables.Messeges;
import mavlink.is.drone.variables.MissionStats;
import mavlink.is.drone.variables.Navigation;
import mavlink.is.drone.variables.Orientation;
import mavlink.is.drone.variables.Perimeter;
import mavlink.is.drone.variables.RC;
import mavlink.is.drone.variables.Radio;
import mavlink.is.drone.variables.Speed;
import mavlink.is.drone.variables.State;
import mavlink.is.drone.variables.StreamRates;
import mavlink.is.drone.variables.Type;
import mavlink.is.gcs.follow.Follow;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_heartbeat;
import mavlink.is.protocol.msgbuilder.WaypointManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ComponentScan("mavlink.is.drone.variables")
@ComponentScan("mavlink.is.drone")
@ComponentScan("mavlink.is.gcs.follow")
@ComponentScan("mavlink.is.protocol.msgbuilder")
@ComponentScan("mavlink.core.connection")
@ComponentScan("mavlink.core.location")
@Component("drone")
@Configuration
public class MyDroneImpl implements Drone {

	@Bean
	public VehicleProfile profile() {
		loadVehicleProfile();
		return profile;
	}
	
	@Resource(name = "gps")
	private GPS GPS;
	
	@Resource(name = "preferencesImpl")
	private Preferences preferences;
	
	@Resource(name = "rc")
	private RC RC;

	@Resource(name = "beacon")
	private Beacon Beacon;

	@Resource(name = "gcs")
	private GCS GCS;
	
	@Resource(name = "speed")
	private Speed speed;

	@Resource(name = "battery")
	private Battery battery;

	@Resource(name = "radio")
	private Radio radio;

	@Resource(name = "home")
	private Home home;

	@Resource(name = "mission")
	private Mission mission;
	
	@Resource(name = "missionStats")
	private MissionStats missionStats;

	@Resource(name = "streamRates")
	private StreamRates streamRates;

	@Resource(name = "altitude")
	private Altitude altitude;

	@Resource(name = "orientation")
	private Orientation orientation;

	@Resource(name = "navigation")
	private Navigation navigation;

	@Resource(name = "guidedPoint")
	private GuidedPoint guidedPoint;
	
	@Resource(name = "calibrationSetup")
	private Calibration calibrationSetup;

	@Resource(name = "waypointManager")
	private WaypointManager waypointManager;

	@Resource(name = "mag")
	private Magnetometer mag;

	@Resource(name = "footprints")
	private CameraFootprints footprints;

	@Resource(name = "perimeter")
	private Perimeter Perimeter;
	
	@Resource(name = "type")
	private Type type;
	
	@Resource(name = "messeges")
	private Messeges messeges;	
	
	@Resource(name = "parameters")
	private Parameters parameters;

	@Resource(name = "events")
	private DroneEvents events;
	
	@Resource(name = "heartbeat")
	private HeartBeat heartbeat;
	
	@Resource(name = "state")
	private State state;

	@Resource(name = "follow")
	private Follow follow;
	
	@Resource(name = "radioConnection")
	private MavLinkConnection mavlinkConnection;

	private VehicleProfile profile;
	
	static int called = 0;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		heartbeat.init();
		state.init();
		follow.init();
		guidedPoint.init();
		type.init();
		streamRates.init();
		Perimeter.init();
		parameters.init();
		messeges.init();
	}

	@Override
	public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed, double climb) {
		this.altitude.setAltitude(altitude);
		speed.setGroundAndAirSpeeds(groundSpeed, airSpeed, climb);
	    notifyDroneEvent(DroneInterfaces.DroneEventsType.SPEED);
	}

	@Override
	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error) {
		missionStats.setDistanceToWp(disttowp);
		altitude.setAltitudeError(alt_error);
		speed.setSpeedError(aspd_error);
		notifyDroneEvent(DroneInterfaces.DroneEventsType.ORIENTATION);
	}

	@Override
	public boolean isConnectionAlive() {
		return heartbeat.isConnectionAlive();
	}

	@Override
	public void addDroneListener(DroneInterfaces.OnDroneListener listener) {
		events.addDroneListener(listener);
	}

	@Override
	public void removeDroneListener(DroneInterfaces.OnDroneListener listener) {
		events.removeDroneListener(listener);
	}

	@Override
	public void notifyDroneEvent(final DroneInterfaces.DroneEventsType event) {
		events.notifyDroneEvent(event);
	}

	@Override
	public GPS getGps() {
		return GPS;
	}

	@Override
	public int getMavlinkVersion() {
		return heartbeat.getMavlinkVersion();
	}

	@Override
	public void onHeartbeat(msg_heartbeat msg) {
		heartbeat.onHeartbeat(msg);
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public Parameters getParameters() {
		return parameters;
	}

	@Override
	public void setType(int type) {
		this.type.setType(type);
	}

	@Override
	public int getType() {
		return type.getType();
	}

	@Override
	public FirmwareType getFirmwareType() {
		return type.getFirmwareType();
	}

	@Override
	public void loadVehicleProfile() {
		profile = preferences.loadVehicleProfile(getFirmwareType());
	}

	@Override
	public VehicleProfile getVehicleProfile() {
		return profile;
	}

	@Override
	public Preferences getPreferences() {
		return preferences;
	}

	@Override
	public WaypointManager getWaypointManager() {
		return waypointManager;
	}

	@Override
	public RC getRC() {
		return RC;
	}

	@Override
	public Speed getSpeed() {
		return speed;
	}

	@Override
	public Battery getBattery() {
		return battery;
	}

	@Override
	public Radio getRadio() {
		return radio;
	}

	@Override
	public Home getHome() {
		return home;
	}

	@Override
	public Mission getMission() {
		return mission;
	}

	@Override
	public MissionStats getMissionStats() {
		return missionStats;
	}

	@Override
	public StreamRates getStreamRates() {
		return streamRates;
	}

	@Override
	public Altitude getAltitude() {
		return altitude;
	}

	@Override
	public Orientation getOrientation() {
		return orientation;
	}

	@Override
	public Navigation getNavigation() {
		return navigation;
	}

	@Override
	public GuidedPoint getGuidedPoint() {
		return guidedPoint;
	}

	@Override
	public Calibration getCalibrationSetup() {
		return calibrationSetup;
	}

	@Override
	public String getFirmwareVersion() {
		return type.getFirmwareVersion();
	}

	@Override
	public void setFirmwareVersion(String message) {
		type.setFirmwareVersion(message);
	}

	@Override
	public Magnetometer getMagnetometer() {
		return mag;
	}
	
	public CameraFootprints getCameraFootprints() {
		return footprints;
	}

	@Override
	public Perimeter getPerimeter() {
		return Perimeter;
	}

	@Override
	public Messeges getMessegeQueue() {
		return messeges;
	}

	@Override
	public Beacon getBeacon() {
		return Beacon;
	}
	
	@Override
	public GCS getGCS() {
		return GCS;
	}

	@Override
	public Follow getFollow() {
		return follow;
	}
	
	@Override
	public MavLinkConnection getMavClient() {
		return mavlinkConnection;
	}
}