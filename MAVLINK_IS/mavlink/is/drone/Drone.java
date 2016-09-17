package mavlink.is.drone;

import mavlink.core.connection.RadioConnection;
import mavlink.core.firmware.FirmwareType;
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
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_heartbeat;
import mavlink.is.protocol.msgbuilder.WaypointManager;

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

	public RadioConnection getMavClient();

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
	
}
