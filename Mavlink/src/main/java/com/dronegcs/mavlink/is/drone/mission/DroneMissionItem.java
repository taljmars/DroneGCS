package com.dronegcs.mavlink.is.drone.mission;

import java.util.ArrayList;
import java.util.List;

import com.dronegcs.mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_FRAME;

public abstract class MissionItem implements Comparable<MissionItem>  {

	protected DroneMission droneMission;

	public MissionItem(DroneMission droneMission) {
		this.droneMission = droneMission;
	}

	public MissionItem(MissionItem item) {
		this(item.droneMission);
	}

	/**
	 * Return a new list (one or more) of MAVLinkMessage msg_mission_item that
	 * represent this MissionItem
	 * 
	 * @return
	 */
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = new ArrayList<msg_mission_item>();
		msg_mission_item mavMsg = new msg_mission_item();
		list.add(mavMsg);
		mavMsg.autocontinue = 1;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		return list;
	}

	/**
	 * Gets data from MAVLinkMessage msg_mission_item for this MissionItem
	 * 
	 * @return
	 */
	public abstract void unpackMAVMessage(msg_mission_item mavMsg);

	public abstract MissionItemType getType();

	public DroneMission getDroneMission() {
		return droneMission;
	}
	
	public void setDroneMission(DroneMission droneMission) {
		this.droneMission = droneMission;
	}

	@Override
	public int compareTo(MissionItem another) {
		return droneMission.getOrder(this) - droneMission.getOrder(another);
	}
	
	public abstract MissionItem clone(DroneMission droneMission);

}