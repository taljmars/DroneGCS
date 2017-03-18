package com.dronegcs.mavlink.is.drone.mission.waypoints;

import java.util.List;

import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.MissionItemType;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import com.geo_tools.Coordinate;

public class RegionOfInterest extends SpatialCoordItemDrone {

	public RegionOfInterest(RegionOfInterest regionOfInterest) {
		super(regionOfInterest);
	}
	
	public RegionOfInterest(DroneMission droneMission, Coordinate coord) {
		super(droneMission,coord);
	}
	

	public RegionOfInterest(msg_mission_item msg, DroneMission droneMission) {
		super(droneMission, null);
		unpackMAVMessage(msg);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_SET_ROI;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.ROI;
	}

	@Override
	public RegionOfInterest clone(DroneMission droneMission) {
		RegionOfInterest regionOfInterest = new RegionOfInterest(this);
		regionOfInterest.setDroneMission(droneMission);
		return regionOfInterest;
	}

}