package is.mavlink.drone;

import is.mavlink.connection.MavLinkConnection;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_heartbeat;
import is.mavlink.protocol.msgbuilder.WaypointManager;
import mavlink.core.firmware.FirmwareType;
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
import is.mavlink.gcs.follow.Follow;

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
