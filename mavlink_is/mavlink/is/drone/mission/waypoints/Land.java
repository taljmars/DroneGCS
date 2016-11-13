package mavlink.is.drone.mission.waypoints;

import java.io.Serializable;
import java.util.List;

import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.MissionItem;
import mavlink.is.drone.mission.MissionItemType;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.coordinates.Coord3D;
import mavlink.is.utils.units.Altitude;

public class Land extends SpatialCoordItem implements Serializable /* TALMA serializble*/  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7182225492182834661L;

	public Land(MissionItem item) {
		super(item);
		setAltitude(new Altitude(0.0));
	}

	public Land(Mission mission) {
		this(mission,new Coord2D(0,0));
	}

	public Land(Mission mMission, Coord2D coord) {
		super(mMission, new Coord3D(coord,new Altitude(0)));
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