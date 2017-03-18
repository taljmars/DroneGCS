package com.dronegcs.mavlink.is.drone.mission.waypoints;

import java.util.List;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.DroneMissionItem;
import com.dronegcs.mavlink.is.drone.mission.MissionItemType;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import com.geo_tools.Coordinate;

public class Land extends SpatialCoordItemDrone {

	public Land(DroneMissionItem item) {
		super(item);
		setAltitude(0);
	}

	public Land(DroneMission droneMission) {
		this(droneMission,new Coordinate(0,0));
	}

	public Land(DroneMission mDroneMission, Coordinate coord) {
		super(mDroneMission, new Coordinate(coord, 0));
	}
	
	public Land(msg_mission_item msg, DroneMission droneMission) {
		super(droneMission, null);
		unpackMAVMessage(msg);
	}


	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LAND;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.LAND;
	}
	
	@Override
	public Land clone(DroneMission droneMission) {
		Land land = new Land(this);
		land.setDroneMission(droneMission);
		return land;
	}

}