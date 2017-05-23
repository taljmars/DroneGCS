package com.dronegcs.mavlink.is.drone.mission.commands;

import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.DroneMissionItem;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;

import java.util.List;

public abstract class DroneMissionCMD extends DroneMissionItem {

	public DroneMissionCMD(DroneMission droneMission) {
		super(droneMission);
	}

	public DroneMissionCMD(DroneMissionCMD item) {
		super(item);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		return super.packMissionItem();
	}

}