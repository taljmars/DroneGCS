package mavlink.drone;

import mavlink.connection.MavLinkConnection;
import mavlink.core.firmware.FirmwareType;
import mavlink.drone.mission.Mission;
import mavlink.drone.profiles.Parameters;
import mavlink.drone.profiles.VehicleProfile;
import mavlink.drone.variables.Altitude;
import mavlink.drone.variables.Battery;
import mavlink.drone.variables.Beacon;
import mavlink.drone.variables.Calibration;
import mavlink.drone.variables.CameraFootprints;
import mavlink.drone.variables.GCS;
import mavlink.drone.variables.GPS;
import mavlink.drone.variables.GuidedPoint;
import mavlink.drone.variables.Home;
import mavlink.drone.variables.Magnetometer;
import mavlink.drone.variables.Messeges;
import mavlink.drone.variables.MissionStats;
import mavlink.drone.variables.Navigation;
import mavlink.drone.variables.Orientation;
import mavlink.drone.variables.Perimeter;
import mavlink.drone.variables.RC;
import mavlink.drone.variables.Radio;
import mavlink.drone.variables.Speed;
import mavlink.drone.variables.State;
import mavlink.drone.variables.StreamRates;
import mavlink.gcs.follow.Follow;
import mavlink.protocol.msg_metadata.ardupilotmega.msg_heartbeat;
import mavlink.protocol.msgbuilder.WaypointManager;

public interface Drone {

	public void addDroneListener(DroneInterfaces.OnDroneListener listener);

	public void removeDroneListener(DroneInterfaces.OnDroneListener listener);

	public void notifyDroneEvent(DroneInterfaces.DroneEventsType event);

	public GPS getGps();

	public int getMavlinkVersion();

	public boolean isConnectionAlive();

	public void onHeartbeat(msg_heartbeat msg);

	public State getState();

	public Parameters getParameters();

	public void setType(int type);

	public int getType();

	public FirmwareType getFirmwareType();

	public void loadVehicleProfile();

	public VehicleProfile getVehicleProfile();

	public MavLinkConnection getMavClient();

	public Preferences getPreferences();

	public WaypointManager getWaypointManager();

	public Speed getSpeed();

	public Battery getBattery();

	public Radio getRadio();

	public Home getHome();

	public Altitude getAltitude();

	public Orientation getOrientation();

	public Navigation getNavigation();

	public Mission getMission();

	public StreamRates getStreamRates();

	public MissionStats getMissionStats();

	public GuidedPoint getGuidedPoint();

	public Calibration getCalibrationSetup();

	public RC getRC();
	
	public Magnetometer getMagnetometer();

	public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed,
			double climb);

	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error);

	public String getFirmwareVersion();

	public void setFirmwareVersion(String message);

	public CameraFootprints getCameraFootprints();

	public Perimeter getPerimeter();

	public Messeges getMessegeQueue();

	public Beacon getBeacon();

	public GCS getGCS();

	public Follow getFollow();
	
}
