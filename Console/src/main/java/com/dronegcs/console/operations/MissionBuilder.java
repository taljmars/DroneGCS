package operations;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import controllers.internalFrames.internal.view_tree_layers.LayerMission;
import services.DialogManagerSvc;
import services.EventPublisherSvc;
import services.LoggerDisplayerSvc;
import is.mavlink.drone.mission.Mission;
import is.mavlink.drone.mission.MissionItem;
import is.mavlink.drone.mission.commands.ReturnToHome;
import is.mavlink.drone.mission.commands.Takeoff;
import is.mavlink.drone.mission.waypoints.Circle;
import is.mavlink.drone.mission.waypoints.Land;
import is.mavlink.drone.mission.waypoints.RegionOfInterest;
import is.mavlink.drone.mission.waypoints.Waypoint;
import geoTools.Coordinate;
import services.internal.QuadGuiEvent;

@ComponentScan("gui.is.services")
@Component
public class MissionBuilder {
	
    @Autowired @NotNull( message = "Internal Error: Failed to get event publisher service" )
	protected EventPublisherSvc eventPublisherSvc;
    
    @Autowired @NotNull( message = "Internal Error: Failed to get log displayer" )
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	private LayerMission layerMission;
	private Mission mission;

	public void startMissionLayer(LayerMission layerMission) {
		loggerDisplayerSvc.logGeneral("Setting new mission to mission editor");
		this.layerMission = layerMission;
		this.mission = layerMission.getMission();
		eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_EDITING_STARTED, layerMission));
	}

	public void addWayPoint(Coordinate iCoord) {
		Coordinate c3 = new Coordinate(iCoord, 20);
		if (mission.isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("Waypoints cannot be added to once there is a Land/RTL point");
			return;
		}
		updateMissionItem(new Waypoint(mission, c3));
	}

	public void addCircle(Coordinate coord) {
		Coordinate c3 = new Coordinate(coord, 20);
		if (mission.isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("Waypoints cannot be added to once there is a Land/RTL point");
			return;
		}
		updateMissionItem(new Circle(mission, c3));
	}

	public void addLandPoint(Coordinate coord) {
		Coordinate c3 = new Coordinate(coord, 20);
		if (mission.isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("RTL/Land point was already defined");
			return;
		}
		updateMissionItem(new Land(mission, c3));
	}

	public void addRTL() {
		if (mission.isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("RTL/Land point was already defined");
			return;
		}
		updateMissionItem(new ReturnToHome(mission));
	}

	public void addTakeOff() {
		if (mission.isFirstItemTakeoff()) {
			dialogManagerSvc.showAlertMessageDialog("Takeoff point was already defined");
			return;
		}

		String val = dialogManagerSvc.showInputDialog("Choose altitude", "",null, null, "5");
		if (val == null) {
			System.out.println(getClass().getName() + " Takeoff canceled");
			dialogManagerSvc.showAlertMessageDialog("Takeoff must be defined with height");
			return;
		}
		double altitude = Double.parseDouble((String) val);
		updateMissionItem(new Takeoff(mission, altitude));
	}
	
	public void addROI(Coordinate coord) {
		Coordinate c3 = new Coordinate(coord, 20);
		if (mission.isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("Waypoints cannot be added to once there is a Land/RTL point");
			return;
		}
		updateMissionItem(new RegionOfInterest(mission, c3));
	}
	
	private void updateMissionItem(MissionItem missionItem) {
		mission.addMissionItem(missionItem);
		layerMission.regenerateMapObjects();
		eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_MAP, layerMission));
	}

	public void stopBuildMission() {
		eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_EDITING_FINISHED, this.layerMission));
		this.layerMission = null;
		this.mission = null;
		loggerDisplayerSvc.logGeneral("Mission editor finished");
	}
}
