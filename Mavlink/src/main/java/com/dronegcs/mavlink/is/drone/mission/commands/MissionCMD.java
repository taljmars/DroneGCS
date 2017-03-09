package is.drone.mission.commands;

import java.util.List;

import is.drone.mission.Mission;
import is.drone.mission.MissionItem;
import is.protocol.msg_metadata.ardupilotmega.msg_mission_item;

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