package is.drone;

import is.connection.MavLinkConnection;
import is.protocol.msg_metadata.ardupilotmega.msg_heartbeat;
import is.protocol.msgbuilder.WaypointManager;
import com.dronegcs.mavlink.core.firmware.FirmwareType;
import is.drone.mission.Mission;
import is.drone.profiles.Parameters;
import is.drone.profiles.VehicleProfile;
import is.drone.variables.Altitude;
import is.drone.variables.Battery;
import is.drone.variables.Beacon;
import is.drone.variables.Calibration;
import is.drone.variables.CameraFootprints;
import is.drone.variables.GCS;
import is.drone.variables.GPS;
import is.drone.variables.GuidedPoint;
import is.drone.variables.Home;
import is.drone.variables.Magnetometer;
import is.drone.variables.Messeges;
import is.drone.variables.MissionStats;
import is.drone.variables.Navigation;
import is.drone.variables.Orientation;
import is.drone.variables.Perimeter;
import is.drone.variables.RC;
import is.drone.variables.Radio;
import is.drone.variables.Speed;
import is.drone.variables.State;
import is.drone.variables.StreamRates;
import is.gcs.follow.Follow;

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
