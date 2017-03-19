package com.dronegcs.mavlink.is.drone.mission.waypoints;

import java.util.List;

import com.dronegcs.mavlink.is.drone.mission.ConvertMavlinkVisitor;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.MissionItemType;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import com.geo_tools.Coordinate;

public class MavlinkRegionOfInterest extends SpatialCoordItemDrone {

	public MavlinkRegionOfInterest(MavlinkRegionOfInterest mavlinkRegionOfInterest) {
		super(mavlinkRegionOfInterest);
	}
	
	public MavlinkRegionOfInterest(DroneMission droneMission, Coordinate coord) {
		super(droneMission,coord);
	}
	

	public MavlinkRegionOfInterest(msg_mission_item msg, DroneMission droneMission) {
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
	public MavlinkRegionOfInterest clone(DroneMission droneMission) {
		MavlinkRegionOfInterest mavlinkRegionOfInterest = new MavlinkRegionOfInterest(this);
		mavlinkRegionOfInterest.setDroneMission(droneMission);
		return mavlinkRegionOfInterest;
	}

	@Override
	public void accept(ConvertMavlinkVisitor convertMavlinkVisitor) {
		convertMavlinkVisitor.visit(this);
	}
}