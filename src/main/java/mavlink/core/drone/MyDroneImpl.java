package mavlink.core.drone;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import is.mavlink.connection.MavLinkConnection;
import mavlink.core.firmware.FirmwareType;
import is.mavlink.drone.Drone;
import is.mavlink.drone.DroneEvents;
import is.mavlink.drone.DroneInterfaces;
import is.mavlink.drone.Preferences;
import is.mavlink.drone.mission.Mission;
import is.mavlink.drone.profiles.Parameters;
import is.mavlink.drone.profiles.VehicleProfile;
import is.mavlink.drone.variables.Altitude;
import is.mavlink.drone.variables.Battery;
import is.mavlink.drone.variables.Beacon;
import is.mavlink.drone.variables.Calibration;
import is.mavlink.drone.variables.CameraFootprints;
import is.mavlink.drone.variables.GCS;
import is.mavlink.drone.variables.GPS;
import is.mavlink.drone.variables.GuidedPoint;
import is.mavlink.drone.variables.HeartBeat;
import is.mavlink.drone.variables.Home;
import is.mavlink.drone.variables.Magnetometer;
import is.mavlink.drone.variables.Messeges;
import is.mavlink.drone.variables.MissionStats;
import is.mavlink.drone.variables.Navigation;
import is.mavlink.drone.variables.Orientation;
import is.mavlink.drone.variables.Perimeter;
import is.mavlink.drone.variables.RC;
import is.mavlink.drone.variables.Radio;
import is.mavlink.drone.variables.Speed;
import is.mavlink.drone.variables.State;
import is.mavlink.drone.variables.StreamRates;
import is.mavlink.drone.variables.Type;
import is.mavlink.gcs.follow.Follow;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_heartbeat;
import is.mavlink.protocol.msgbuilder.WaypointManager;
import is.validations.RuntimeValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ComponentScan("tools.validations")
@ComponentScan("is.mavlink.drone.variables")
@ComponentScan("is.mavlink.drone")
@ComponentScan("is.mavlink.gcs.follow")
@ComponentScan("is.mavlink.protocol.msgbuilder")
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
	
	@NotNull(message="Missing GPS parameter")
	@Autowired
	private GPS GPS;
	
	@NotNull(message="Missing Preferences parameter")
	@Autowired
	private Preferences preferences;
	
	@NotNull(message="Missing RC parameter")
	@Autowired
	private RC RC;

	@NotNull(message="Missing Beacon parameter")
	@Autowired
	private Beacon Beacon;

	@NotNull(message="Missing GCS parameter")
	@Autowired
	private GCS GCS;
	
	@NotNull(message="Missing Speed parameter")
	@Autowired
	private Speed speed;

	@NotNull(message="Missing Battery parameter")
	@Autowired
	private Battery battery;

	@NotNull(message="Missing Radio parameter")
	@Autowired
	private Radio radio;

	@NotNull(message="Missing Home parameter")
	@Autowired
	private Home home;

	@Autowired
	private Mission mission;

	@Autowired
	private MissionStats missionStats;

	@NotNull(message="Missing StreamRates parameter")
	@Autowired
	private StreamRates streamRates;

	@NotNull(message="Missing Altitude parameter")
	@Autowired
	private Altitude altitude;

	@NotNull(message="Missing Orientation parameter")
	@Autowired
	private Orientation orientation;

	@NotNull(message="Missing Navigation parameter")
	@Autowired
	private Navigation navigation;

	@NotNull(message="Missing GuidedPoint parameter")
	@Autowired
	private GuidedPoint guidedPoint;
	
	@NotNull(message="Missing Calibration parameter")
	@Autowired
	private Calibration calibrationSetup;

	@NotNull(message="Missing WaypointManager parameter")
	@Autowired
	private WaypointManager waypointManager;

	@NotNull(message="Missing Magnetometer parameter")
	@Autowired
	private Magnetometer mag;

	@NotNull(message="Missing CameraFootprints parameter")
	@Autowired
	private CameraFootprints footprints;

	@NotNull(message="Missing Perimeter parameter")
	@Autowired
	private Perimeter Perimeter;
	
	@NotNull(message="Missing Type parameter")
	@Autowired
	private Type type;
	
	@NotNull(message="Missing Messege parameter")
	@Autowired
	private Messeges messeges;	
	
	@NotNull(message="Missing Parameters parameter")
	@Autowired
	private Parameters parameters;

	@NotNull(message="Missing DroneEvents parameter")
	@Autowired
	private DroneEvents events;
	
	@NotNull(message="Missing HeartBeat parameter")
	@Autowired
	private HeartBeat heartbeat;
	
	@NotNull(message="Missing State parameter")
	@Autowired
	private State state;

	@NotNull(message="Missing Follow parameter")
	@Autowired
	private Follow follow;
	
	@NotNull(message="Missing MavLinkConnection parameter")
	@Autowired
	private MavLinkConnection mavlinkConnection;

	@NotNull(message = "Internal Error: Failed to get validator")
	@Autowired
	private RuntimeValidator validator;

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
		
		if (!validator.validate(this))
			throw new RuntimeException("Failed to initialize drone");
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