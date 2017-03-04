package is.mavlink.drone.mission.commands;

import java.util.List;

import is.mavlink.drone.mission.Mission;
import is.mavlink.drone.mission.MissionItem;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_item;

public abstract class MissionCMD extends MissionItem {

	public MissionCMD(Mission mission) {
		super(mission);
	}

	public MissionCMD(MissionCMD item) {
		super(item);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		return super.packMissionItem();
	}

}