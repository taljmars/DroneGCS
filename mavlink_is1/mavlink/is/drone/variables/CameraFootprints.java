package mavlink.is.drone.variables;

import java.util.ArrayList;
import java.util.List;

import mavlink.core.survey.Footprint;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.mission.survey.CameraInfo;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_camera_feedback;

public class CameraFootprints extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -612523828472020517L;
	private CameraInfo camera = new CameraInfo();
	private List<Footprint> footprints = new ArrayList<Footprint>();

	public CameraFootprints(Drone myDrone) {
		super(myDrone);
	}

	public void newImageLocation(msg_camera_feedback msg) {
		footprints.add(new Footprint(camera,msg));
		myDrone.notifyDroneEvent(DroneEventsType.FOOTPRINT);
	}

	public Footprint getLastFootprint() {
		return footprints.get(footprints.size()-1);
	}

}
