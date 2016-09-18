package mavlink.is.drone.mission.commands;

import java.util.List;

import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.MissionItem;
import mavlink.is.drone.mission.MissionItemType;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.utils.units.Length;

public class CameraTrigger extends MissionCMD {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3729346251461586332L;
	private Length distance = new Length(0);

	public CameraTrigger(MissionItem item) {
		super(item);
	}

	public CameraTrigger(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public CameraTrigger(Mission mission, Length triggerDistance) {
		super(mission);
		this.distance = triggerDistance;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST;
		mavMsg.param1 = (float) distance.valueInMeters() ;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		distance = new Length(mavMsg.param1);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CAMERA_TRIGGER;
	}

	public Length getTriggerDistance() {
		return distance;
	}

	public void setTriggerDistance(Length triggerDistance) {
		this.distance = triggerDistance;
	}
}