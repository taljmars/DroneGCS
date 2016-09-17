package mavlink.core.drone.variables;

import java.util.ArrayList;
import java.util.List;

import mavlink.core.drone.DroneVariable;
import mavlink.core.drone.DroneInterfaces.DroneEventsType;
import mavlink.core.mission.survey.CameraInfo;
import mavlink.core.survey.Footprint;
import mavlink.is.model.Drone;
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
