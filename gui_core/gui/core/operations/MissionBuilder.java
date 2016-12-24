package gui.core.operations;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import gui.core.mapTreeObjects.LayerMission;
import gui.is.Coordinate;
import gui.is.events.GuiEvent;
import gui.is.events.GuiEvent.COMMAND;
import gui.is.services.DialogManagerSvc;
import gui.is.services.EventPublisherSvc;
import gui.is.services.LoggerDisplayerSvc;
import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.commands.ReturnToHome;
import mavlink.is.drone.mission.commands.Takeoff;
import mavlink.is.drone.mission.waypoints.Circle;
import mavlink.is.drone.mission.waypoints.Land;
import mavlink.is.drone.mission.waypoints.Waypoint;
import mavlink.is.utils.coordinates.Coord3D;
import mavlink.is.utils.units.Altitude;

@ComponentScan("gui.is.services")
@Component("missionBuilder")
public class MissionBuilder {
	
    @Resource(name = "eventPublisherSvc")
	protected EventPublisherSvc eventPublisherSvc;
    
	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "dialogManagerSvc")
	@NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	private LayerMission layerMission;
	private Mission mission;

	public void startMissionLayer(LayerMission layerMission) {
		loggerDisplayerSvc.logGeneral("Setting new mission to mission editor");
		this.layerMission = layerMission;
		this.mission = layerMission.getMission();
		eventPublisherSvc.publish(new GuiEvent(COMMAND.MISSION_EDITING_STARTED, layerMission));
	}

	public void addWayPoint(Coordinate iCoord) {
		Coord3D c3 = new Coord3D(iCoord.ConvertToCoord2D(),new Altitude(20));
		if (mission.isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("Waypoints cannot be added to once there is a Land/RTL point");
			return;
		}
		Waypoint wp = new Waypoint(mission, c3);
		mission.addMissionItem(wp);
		
		layerMission.regenerateMapObjects();
		eventPublisherSvc.publish(new GuiEvent(COMMAND.MISSION_UPDATED_BY_MAP, layerMission));
	}

	public void addCircle(Coordinate coord) {
		Coord3D c3 = new Coord3D(coord.ConvertToCoord2D(),new Altitude(20));
		if (mission.isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("Waypoints cannot be added to once there is a Land/RTL point");
			return;
		}
		Circle wp = new Circle(mission, c3);
		mission.addMissionItem(wp);
		
		layerMission.regenerateMapObjects();
		eventPublisherSvc.publish(new GuiEvent(COMMAND.MISSION_UPDATED_BY_MAP, layerMission));
	}

	public void addLandPoint(Coordinate coord) {
		Coord3D c3 = new Coord3D(coord.ConvertToCoord2D(), new Altitude(20));
		if (mission.isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("RTL/Land point was already defined");
			return;
		}
		Land lnd = new Land(mission, c3);
		mission.addMissionItem(lnd);
		
		layerMission.regenerateMapObjects();
		eventPublisherSvc.publish(new GuiEvent(COMMAND.MISSION_UPDATED_BY_MAP, layerMission));
	}

	public void addRTL() {
		if (mission.isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("RTL/Land point was already defined");
			return;
		}
		ReturnToHome lnd = new ReturnToHome(mission);
		mission.addMissionItem(lnd);
		
		layerMission.regenerateMapObjects();
		eventPublisherSvc.publish(new GuiEvent(COMMAND.MISSION_UPDATED_BY_MAP, layerMission));
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

		Takeoff toff = new Takeoff(mission, new Altitude(altitude));
		mission.addMissionItem(toff);
		
		layerMission.regenerateMapObjects();
		eventPublisherSvc.publish(new GuiEvent(COMMAND.MISSION_UPDATED_BY_MAP, layerMission));
	}

	public void stopBuildMission() {
		eventPublisherSvc.publish(new GuiEvent(COMMAND.MISSION_EDITING_FINISHED, this.layerMission));
		this.layerMission = null;
		this.mission = null;
		loggerDisplayerSvc.logGeneral("Mission editor finished");
	}
}
