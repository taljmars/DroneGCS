package mavlink.drone.mission.commands;

import java.util.List;

import mavlink.drone.mission.Mission;
import mavlink.drone.mission.MissionItemType;
import mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.protocol.msg_metadata.enums.MAV_FRAME;
import mavlink.utils.units.Speed;

public class ChangeSpeed extends MissionCMD {

	private Speed speed = new Speed(5);

	public ChangeSpeed(ChangeSpeed item) {
		super(item);
		this.speed = new Speed(item.speed);
	}

	public ChangeSpeed(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public ChangeSpeed(Mission mission, Speed speed) {
		super(mission);
		this.speed = speed;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_CHANGE_SPEED;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.param2 = (float) speed.valueInMetersPerSecond();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		speed = new Speed(mavMsg.param2);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CHANGE_SPEED;
	}

	public Speed getSpeed() {
		return speed;
	}

	public void setSpeed(Speed speed) {
		this.speed = speed;
	}

	@Override
	public ChangeSpeed clone(Mission mission) {
		ChangeSpeed changeSpeed = new ChangeSpeed(this);
		changeSpeed.setMission(mission);
		return changeSpeed;
	}
}