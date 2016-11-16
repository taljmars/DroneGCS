package mavlink.is.drone.mission.commands;

import java.io.Serializable;
import java.util.List;

import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.MissionItemType;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.protocol.msg_metadata.enums.MAV_FRAME;
import mavlink.is.utils.units.Altitude;

public class Takeoff extends MissionCMD implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1839449676706734507L;

	public static final double DEFAULT_TAKEOFF_ALTITUDE = 10.0;

	private Altitude finishedAlt = new Altitude(10);

	public Takeoff(Takeoff item) {
		super(item);
		finishedAlt = new Altitude(item.finishedAlt);
	}

	public Takeoff(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public Takeoff(Mission mission, Altitude altitude) {
		super(mission);
		finishedAlt = altitude;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.z = (float) finishedAlt.valueInMeters();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		finishedAlt = new Altitude(mavMsg.z);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.TAKEOFF;
	}

	public Altitude getFinishedAlt() {
		return finishedAlt;
	}

	public void setFinishedAlt(Altitude finishedAlt) {
		this.finishedAlt = finishedAlt;
	}

	@Override
	public Takeoff clone(Mission mission) {
		Takeoff takeoff = new Takeoff(this);
		takeoff.setMission(mission);
		return takeoff;
	}
}