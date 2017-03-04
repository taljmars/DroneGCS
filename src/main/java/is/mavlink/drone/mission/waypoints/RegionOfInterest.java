package is.mavlink.drone.mission.waypoints;

import java.util.List;

import is.mavlink.drone.mission.Mission;
import is.mavlink.drone.mission.MissionItemType;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import is.mavlink.protocol.msg_metadata.enums.MAV_CMD;
import tools.geoTools.Coordinate;

public class RegionOfInterest extends SpatialCoordItem {

	public RegionOfInterest(RegionOfInterest regionOfInterest) {
		super(regionOfInterest);
	}
	
	public RegionOfInterest(Mission mission, Coordinate coord) {
		super(mission,coord);
	}
	

	public RegionOfInterest(msg_mission_item msg, Mission mission) {
		super(mission, null);
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
	public RegionOfInterest clone(Mission mission) {
		RegionOfInterest regionOfInterest = new RegionOfInterest(this);
		regionOfInterest.setMission(mission);
		return regionOfInterest;
	}

}