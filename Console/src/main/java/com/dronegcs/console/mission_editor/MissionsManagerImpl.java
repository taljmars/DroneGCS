package com.dronegcs.console.operations;

import javax.validation.constraints.NotNull;

import com.dronedb.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.apis.*;
import com.dronedb.persistence.scheme.mission.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.dronegcs.console.services.DialogManagerSvc;
import com.dronegcs.console.services.LoggerDisplayerSvc;
import com.geo_tools.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MissionsManager {

	@Autowired
	@NotNull(message = "Internal Error: Failed to get log displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;

	@Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
	private DroneDbCrudSvcRemote droneDbCrudSvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get query")
	private QuerySvcRemote querySvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get mission object crud")
	private MissionCrudSvcRemote missionCrudSvcRemote;

	private Mission mission;
	private Mission originalMission;

	public Mission startMissionEditing(String initialName) {
		loggerDisplayerSvc.logGeneral("Setting new mission to mission editor");
		this.mission = new Mission();
		this.mission.setName(initialName);
		droneDbCrudSvcRemote.update(mission);
		return this.mission;
	}

	public Mission startMissionEditing(Mission mission) {
		loggerDisplayerSvc.logGeneral("Setting new mission to mission editor");
		this.mission = mission;
		this.originalMission = missionCrudSvcRemote.cloneMission(this.mission);
		this.mission.setName(this.mission.getName() + "*");
		droneDbCrudSvcRemote.update(this.mission);
		return this.mission;
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
		if (isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("RTL/MavlinkLand point was already defined");
			return;
		}

		Land land = new Land();
		land.setAltitude(20.0);
		land.setLat(coord.getLat());
		land.setLon(coord.getLon());
		updateMissionItem(land);
	}

	public void addRTL() {
		if (isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("RTL/MavlinkLand point was already defined");
			return;
		}
		ReturnToHome returnToHome = new ReturnToHome();
		returnToHome.setAltitude(0.0);
		updateMissionItem(returnToHome);
	}

	public void addTakeOff() {
		if (isFirstItemTakeoff()) {
			dialogManagerSvc.showAlertMessageDialog("MavlinkTakeoff point was already defined");
			return;
		}

		String val = dialogManagerSvc.showInputDialog("Choose altitude", "",null, null, "5");
		if (val == null) {
			System.out.println(getClass().getName() + " MavlinkTakeoff canceled");
			dialogManagerSvc.showAlertMessageDialog("MavlinkTakeoff must be defined with height");
			return;
		}
		double altitude = Double.parseDouble((String) val);
		Takeoff takeoff = new Takeoff();
		takeoff.setFinishedAlt(altitude);
		updateMissionItem(takeoff);
	}

	public void addROI(Coordinate coord) {
		if (isLastItemLandOrRTL()) {
			dialogManagerSvc.showAlertMessageDialog("Waypoints cannot be added to once there is a MavlinkLand/RTL point");
			return;
		}

		RegionOfInterest regionOfInterest = new RegionOfInterest();
		regionOfInterest.setAltitude(20.0);
		regionOfInterest.setLat(coord.getLat());
		regionOfInterest.setLon(coord.getLon());
		updateMissionItem(regionOfInterest);
	}

	private void updateMissionItem(MissionItem missionItem) {
		// Update Item
		droneDbCrudSvcRemote.update(missionItem);
		mission.addMissionItemUid(missionItem.getObjId());

		// Update Mission
		droneDbCrudSvcRemote.update(mission);
	}

	public Mission stopMissionEditing(boolean shouldSave) {
		Mission res = this.mission;
		if (!shouldSave) {
			droneDbCrudSvcRemote.delete(mission);
			res = this.originalMission;
		}
		else {
			if (originalMission != null) droneDbCrudSvcRemote.delete(originalMission);
		}
		System.out.println("Before resting " + res);
		this.originalMission = null;
		this.mission = null;
		loggerDisplayerSvc.logGeneral("DroneMission editor finished");
		System.out.println("After resting " + res);
		return res;
	}

	// Utilities

	private boolean isLastItemLandOrRTL() {
		if (mission.getMissionItemsUids().isEmpty())
			return false;

		UUID lastUid = mission.getMissionItemsUids().get(mission.getMissionItemsUids().size() - 1);
		MissionItem last = droneDbCrudSvcRemote.readByClass(lastUid, MissionItem.class);
		return (last instanceof ReturnToHome) || (last instanceof Land);
	}

	public boolean isFirstItemTakeoff() {
		if (mission.getMissionItemsUids().isEmpty())
			return false;

		UUID uid = mission.getMissionItemsUids().get(0);
		MissionItem missionItem = droneDbCrudSvcRemote.readByClass(uid, MissionItem.class);
		return missionItem instanceof Takeoff;
	}

	public void setMissionName(String missionName) {
		this.mission.setName(missionName);
	}

	public void delete(Mission mission) {
		if (mission.equals(this.mission) && this.originalMission != null) {
			droneDbCrudSvcRemote.delete(originalMission);
			this.originalMission = null;
			this.mission = null;
		}
		droneDbCrudSvcRemote.delete(mission);
	}

	public Mission update(Mission mission) {
		return droneDbCrudSvcRemote.update(mission);
	}

	public List<BaseObject> getAllMissions() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(Mission.class);
		queryRequestRemote.setQuery("GetAllMissions");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> missionList = queryResponseRemote.getResultList();
		return missionList;
	}

	public MissionItem getMissionItem(UUID missionItemUid) {
		return droneDbCrudSvcRemote.readByClass(missionItemUid, MissionItem.class);
	}

	public List<MissionItem> getMissionItems(List<UUID> missionItemsUids) {
		List<MissionItem> res = new ArrayList<>();
		missionItemsUids.forEach( (UUID uuid) -> res.add(droneDbCrudSvcRemote.readByClass(uuid, MissionItem.class)));
		return res;
	}
}
