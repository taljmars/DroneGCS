package mavlink.is.drone.mission.commands;

import java.util.List;

import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.MissionItem;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;

public abstract class MissionCMD extends MissionItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5992408850648290132L;

	public MissionCMD(Mission mission) {
		super(mission);
	}

	public MissionCMD(MissionItem item) {
		super(item);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		return super.packMissionItem();
	}

}