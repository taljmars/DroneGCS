package com.dronegcs.mavlink.is.drone.mission.commands;

import java.util.List;

import com.dronegcs.mavlink.is.drone.mission.Mission;
import com.dronegcs.mavlink.is.drone.mission.MissionItemType;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_CMD;

public class CameraTrigger extends MissionCMD {

	private double distance = 0;

	public CameraTrigger(CameraTrigger item) {
		super(item);
		this.distance = item.distance;
	}

	public CameraTrigger(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public CameraTrigger(Mission mission, double triggerDistance) {
		super(mission);
		this.distance = triggerDistance;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST;
		mavMsg.param1 = (float) distance;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		distance = mavMsg.param1;
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CAMERA_TRIGGER;
	}

	public double getTriggerDistance() {
		return distance;
	}

	public void setTriggerDistance(double triggerDistance) {
		this.distance = triggerDistance;
	}

	@Override
	public CameraTrigger clone(Mission mission) {
		CameraTrigger cameraTrigger = new CameraTrigger(this);
		cameraTrigger.setMission(mission);
		return cameraTrigger;
	}
}