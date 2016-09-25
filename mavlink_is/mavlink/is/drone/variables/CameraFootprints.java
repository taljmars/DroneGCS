package mavlink.is.drone.variables;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import mavlink.core.survey.Footprint;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.mission.survey.CameraInfo;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_camera_feedback;

@Component("footprints")
public class CameraFootprints extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -612523828472020517L;
	private CameraInfo camera = new CameraInfo();
	private List<Footprint> footprints = new ArrayList<Footprint>();

	public void newImageLocation(msg_camera_feedback msg) {
		footprints.add(new Footprint(camera,msg));
		drone.notifyDroneEvent(DroneEventsType.FOOTPRINT);
	}

	public Footprint getLastFootprint() {
		return footprints.get(footprints.size()-1);
	}

}
