package mavlink.drone.mission.waypoints;

import java.util.List;
import mavlink.drone.mission.Mission;
import mavlink.drone.mission.MissionItem;
import mavlink.drone.mission.MissionItemType;
import mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.protocol.msg_metadata.enums.MAV_CMD;
import tools.geoTools.Coordinate;

public class Land extends SpatialCoordItem {

	public Land(MissionItem item) {
		super(item);
		setAltitude(0);
	}

	public Land(Mission mission) {
		this(mission,new Coordinate(0,0));
	}

	public Land(Mission mMission, Coordinate coord) {
		super(mMission, new Coordinate(coord, 0));
	}
	
	public Land(msg_mission_item msg, Mission mission) {
		super(mission, null);
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
	public Land clone(Mission mission) {
		Land land = new Land(this);
		land.setMission(mission);
		return land;
	}

}