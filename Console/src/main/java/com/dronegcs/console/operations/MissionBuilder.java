package com.dronegcs.console.operations;

import javax.validation.constraints.NotNull;

import com.dronedb.persistence.scheme.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerMission;
import com.dronegcs.console.services.DialogManagerSvc;
import com.dronegcs.console.services.EventPublisherSvc;
import com.dronegcs.console.services.LoggerDisplayerSvc;
import com.geo_tools.Coordinate;
import com.dronegcs.console.services.internal.QuadGuiEvent;

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
		if (isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("Waypoints cannot be added to once there is a Land/RTL point");
			return;
		}
		Waypoint waypoint = new Waypoint();
		waypoint.setLat(c3.getLat());
		waypoint.setLon(c3.getLon());
		waypoint.setAltitude(c3.getAltitude());
		updateMissionItem(waypoint);
	}

	public void addCircle(Coordinate coord) {
		Coordinate c3 = new Coordinate(coord, 20);
		if (isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("Waypoints cannot be added to once there is a Land/RTL point");
			return;
		}
		Circle circle = new Circle();
		circle.setLon(c3.getLon());
		circle.setLat(c3.getLat());
		circle.setAltitude(c3.getAltitude());
		updateMissionItem(circle);
	}

	public void addLandPoint(Coordinate coord) {
//		Coordinate c3 = new Coordinate(coord, 20);
//		if (mission.isLastItemLandOrRTL()) {
//			dialogManagerSvc.showAlertMessageDialog("RTL/MavlinkLand point was already defined");
//			return;
//		}
//		updateMissionItem(new Land(mission, c3));
	}

	public void addRTL() {
//		if (mission.isLastItemLandOrRTL()) {
//			dialogManagerSvc.showAlertMessageDialog("RTL/MavlinkLand point was already defined");
//			return;
//		}
//		updateMissionItem(new MavlinkReturnToHome(mission));
	}

	public void addTakeOff() {
//		if (mission.isFirstItemTakeoff()) {
//			dialogManagerSvc.showAlertMessageDialog("MavlinkTakeoff point was already defined");
//			return;
//		}
//
//		String val = dialogManagerSvc.showInputDialog("Choose altitude", "",null, null, "5");
//		if (val == null) {
//			System.out.println(getClass().getName() + " MavlinkTakeoff canceled");
//			dialogManagerSvc.showAlertMessageDialog("MavlinkTakeoff must be defined with height");
//			return;
//		}
//		double altitude = Double.parseDouble((String) val);
//		updateMissionItem(new MavlinkTakeoff(mission, altitude));
	}
	
	public void addROI(Coordinate coord) {
//		Coordinate c3 = new Coordinate(coord, 20);
//		if (mission.isLastItemLandOrRTL()) {
//			dialogManagerSvc.showAlertMessageDialog("Waypoints cannot be added to once there is a MavlinkLand/RTL point");
//			return;
//		}
//		updateMissionItem(new RegionOfInterest(mission, c3));
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
		loggerDisplayerSvc.logGeneral("DroneMission editor finished");
	}

	// Utilities

	private boolean isLastItemLandOrRTL() {
		if(mission.getMissionItems().isEmpty())
			return false;

		MissionItem last = mission.getMissionItems().get(mission.getMissionItems().size() - 1);
		return (last instanceof ReturnToHome) || (last instanceof Land);
	}

	public boolean isFirstItemTakeoff() {
		return !mission.getMissionItems().isEmpty() && mission.getMissionItems().get(0) instanceof Takeoff;
	}
}
