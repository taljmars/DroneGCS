package mavlink.core.mission.commands;

import java.util.List;

import mavlink.core.mission.Mission;
import mavlink.core.mission.MissionItem;
import mavlink.core.mission.MissionItemType;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.protocol.msg_metadata.enums.MAV_FRAME;
import mavlink.is.utils.units.Altitude;

public class ReturnToHome extends MissionCMD {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8038289595958313009L;
	private Altitude returnAltitude;

	public ReturnToHome(MissionItem item) {
		super(item);
		returnAltitude = new Altitude(0);
	}

	public ReturnToHome(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public ReturnToHome(Mission mission) {
		super(mission);
		returnAltitude = new Altitude(0.0);
	}

	public Altitude getHeight() {
		return returnAltitude;
	}

	public void setHeight(Altitude altitude) {
		returnAltitude = altitude;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.z = (float) returnAltitude.valueInMeters();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMessageItem) {
		returnAltitude = new Altitude(mavMessageItem.z);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.RTL;
	}

}
