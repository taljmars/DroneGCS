package mavlink.is.drone.mission.commands;

import java.util.List;

import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.MissionItemType;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.protocol.msg_metadata.enums.MAV_FRAME;

public class ReturnToHome extends MissionCMD {

	private static final long serialVersionUID = -8038289595958313009L;
	private double returnAltitude;

	public ReturnToHome(ReturnToHome item) {
		super(item);
		returnAltitude = item.returnAltitude;
	}

	public ReturnToHome(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public ReturnToHome(Mission mission) {
		super(mission);
		returnAltitude = 0.0;
	}

	public double getHeight() {
		return returnAltitude;
	}

	public void setHeight(double altitude) {
		returnAltitude = altitude;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.z = (float) returnAltitude;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMessageItem) {
		returnAltitude = mavMessageItem.z;
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.RTL;
	}
	
	@Override
	public ReturnToHome clone(Mission mission) {
		ReturnToHome returnToHome = new ReturnToHome(this);
		returnToHome.setMission(mission);
		return returnToHome;
	}

}
