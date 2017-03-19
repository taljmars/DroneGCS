package com.dronegcs.mavlink.is.drone;

import com.dronegcs.mavlink.is.connection.MavLinkConnection;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ardupilotmega.msg_heartbeat;
import com.dronegcs.mavlink.is.protocol.msgbuilder.WaypointManager;
import com.dronegcs.mavlink.core.firmware.FirmwareType;
import com.dronegcs.mavlink.is.drone.profiles.Parameters;
import com.dronegcs.mavlink.is.drone.profiles.VehicleProfile;
import com.dronegcs.mavlink.is.drone.variables.Altitude;
import com.dronegcs.mavlink.is.drone.variables.Battery;
import com.dronegcs.mavlink.is.drone.variables.Beacon;
import com.dronegcs.mavlink.is.drone.variables.Calibration;
import com.dronegcs.mavlink.is.drone.variables.CameraFootprints;
import com.dronegcs.mavlink.is.drone.variables.GCS;
import com.dronegcs.mavlink.is.drone.variables.GPS;
import com.dronegcs.mavlink.is.drone.variables.GuidedPoint;
import com.dronegcs.mavlink.is.drone.variables.Home;
import com.dronegcs.mavlink.is.drone.variables.Magnetometer;
import com.dronegcs.mavlink.is.drone.variables.Messeges;
import com.dronegcs.mavlink.is.drone.variables.MissionStats;
import com.dronegcs.mavlink.is.drone.variables.Navigation;
import com.dronegcs.mavlink.is.drone.variables.Orientation;
import com.dronegcs.mavlink.is.drone.variables.Perimeter;
import com.dronegcs.mavlink.is.drone.variables.RC;
import com.dronegcs.mavlink.is.drone.variables.Radio;
import com.dronegcs.mavlink.is.drone.variables.Speed;
import com.dronegcs.mavlink.is.drone.variables.State;
import com.dronegcs.mavlink.is.drone.variables.StreamRates;
import com.dronegcs.mavlink.is.gcs.follow.Follow;

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

	public DroneMission getDroneMission();

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
