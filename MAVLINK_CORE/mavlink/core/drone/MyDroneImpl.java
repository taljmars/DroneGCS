package mavlink.core.drone;

import mavlink.core.connection.RadioConnection;
import mavlink.core.drone.profiles.Parameters;
import mavlink.core.drone.profiles.VehicleProfile;
import mavlink.core.drone.variables.Altitude;
import mavlink.core.drone.variables.Battery;
import mavlink.core.drone.variables.Beacon;
import mavlink.core.drone.variables.Calibration;
import mavlink.core.drone.variables.CameraFootprints;
import mavlink.core.drone.variables.GCS;
import mavlink.core.drone.variables.GPS;
import mavlink.core.drone.variables.GuidedPoint;
import mavlink.core.drone.variables.HeartBeat;
import mavlink.core.drone.variables.Home;
import mavlink.core.drone.variables.Magnetometer;
import mavlink.core.drone.variables.Messeges;
import mavlink.core.drone.variables.MissionStats;
import mavlink.core.drone.variables.Navigation;
import mavlink.core.drone.variables.Orientation;
import mavlink.core.drone.variables.Perimeter;
import mavlink.core.drone.variables.RC;
import mavlink.core.drone.variables.Radio;
import mavlink.core.drone.variables.Speed;
import mavlink.core.drone.variables.State;
import mavlink.core.drone.variables.StreamRates;
import mavlink.core.drone.variables.Type;
import mavlink.core.firmware.FirmwareType;
import mavlink.core.mission.Mission;
import mavlink.is.model.Drone;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_heartbeat;
import mavlink.is.protocol.msgbuilder.WaypointManager;

public class MyDroneImpl implements Drone {

	private final DroneEvents events;
	private final Type type;
	private VehicleProfile profile;
	private final mavlink.core.drone.variables.GPS GPS;

	private final mavlink.core.drone.variables.RC RC;
	private final Perimeter Perimeter;
	private final Beacon Beacon;
	private final GCS GCS;
	private final Speed speed;
	private final Battery battery;
	private final Radio radio;
	private final Home home;
	private final Mission mission;
	private final MissionStats missionStats;
	private final StreamRates streamRates;
	private final Altitude altitude;
	private final Orientation orientation;
	private final Navigation navigation;
	private final GuidedPoint guidedPoint;
	private final Calibration calibrationSetup;
	private final WaypointManager waypointManager;
	private final Magnetometer mag;
	private final CameraFootprints footprints;
	private final State state;
	private final HeartBeat heartbeat;
	private final Parameters parameters;
	private final Messeges messeges;
	
	private final RadioConnection MavClient;
	private final Preferences preferences;

	public MyDroneImpl(RadioConnection mavClient, DroneInterfaces.Clock clock,
			DroneInterfaces.Handler handler, Preferences pref) {
		this.MavClient = mavClient;
		this.preferences = pref;

        events = new DroneEvents(this, handler);
		state = new State(this, clock, handler);
		heartbeat = new HeartBeat(this, handler);
		parameters = new Parameters(this, handler);

        RC = new RC(this);
        GPS = new GPS(this);
        GCS = new GCS(this);
        Perimeter = new Perimeter(this);
        Beacon = new Beacon(this);
        this.type = new Type(this);
        this.speed = new Speed(this);
        this.battery = new Battery(this);
        this.radio = new Radio(this);
        this.home = new Home(this);
        this.mission = new Mission(this);
        this.missionStats = new MissionStats(this);
        this.streamRates = new StreamRates(this);
        this.altitude = new Altitude(this);
        this.orientation = new Orientation(this);
        this.navigation = new Navigation(this);
        this.guidedPoint =  new GuidedPoint(this);
        this.calibrationSetup = new Calibration(this);
        this.waypointManager = new WaypointManager(this);
        this.mag = new Magnetometer(this);
        this.footprints = new CameraFootprints(this);
        this.messeges = new Messeges(this);

        loadVehicleProfile();
	}

	@Override
	public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed,
			double climb) {
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
	public RadioConnection getMavClient() {
		return MavClient;
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
}