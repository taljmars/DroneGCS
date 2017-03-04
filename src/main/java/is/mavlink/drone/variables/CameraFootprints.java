package is.mavlink.drone.variables;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import mavlink.core.survey.Footprint;
import is.mavlink.drone.DroneVariable;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.drone.mission.survey.CameraInfo;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_camera_feedback;

@Component("footprints")
public class CameraFootprints extends DroneVariable {

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
