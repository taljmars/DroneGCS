package mavlink.is.model;

import mavlink.core.connection.RadioConnection;
import mavlink.core.drone.DroneInterfaces;
import mavlink.core.drone.Preferences;
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
import mavlink.core.firmware.FirmwareType;
import mavlink.core.mission.Mission;
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
