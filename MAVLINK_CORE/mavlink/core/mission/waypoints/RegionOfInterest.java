package mavlink.core.mission.waypoints;

import java.util.List;

import mavlink.core.mission.Mission;
import mavlink.core.mission.MissionItem;
import mavlink.core.mission.MissionItemType;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.utils.coordinates.Coord3D;

public class RegionOfInterest extends SpatialCoordItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7150214335545129314L;

	public RegionOfInterest(MissionItem item) {
		super(item);
	}
	
	public RegionOfInterest(Mission mission,Coord3D coord) {
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

}