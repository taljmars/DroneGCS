package com.dronegcs.mavlink.is.drone.mission.waypoints;

import java.util.ArrayList;
import java.util.List;

import com.dronegcs.mavlink.is.drone.mission.ConvertMavlinkVisited;
import com.dronegcs.mavlink.is.drone.mission.ConvertMavlinkVisitor;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.MissionItemType;
import com.dronegcs.mavlink.is.drone.mission.waypoints.interfaces.MavlinkRadiusable;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_FRAME;
import com.geo_tools.Coordinate;

public class MavlinkCircle extends SpatialCoordItemDrone implements MavlinkRadiusable {

	private double radius = 10.0;
	private int turns = 1;

	public MavlinkCircle(MavlinkCircle mavlinkCircle) {
		super(mavlinkCircle);
		this.radius = mavlinkCircle.radius;
		this.turns = mavlinkCircle.turns;
	}

	public MavlinkCircle(DroneMission droneMission, Coordinate coord) {
		super(droneMission, coord);
	}

	public MavlinkCircle(msg_mission_item msg, DroneMission droneMission) {
		super(droneMission, null);
		unpackMAVMessage(msg);
	}

	public void setTurns(int turns) {
		this.turns = Math.abs(turns);
	}

	@Override
	public void setRadius(double radius) {
		this.radius = Math.abs(radius);
	}

	public int getNumberOfTurns() {
		return turns;
	}

	@Override
	public double getRadius() {
		return radius;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = new ArrayList<msg_mission_item>();
		packSingleCircle(list);
		return list;
	}

	private void packSingleCircle(List<msg_mission_item> list) {
		msg_mission_item mavMsg = new msg_mission_item();
		list.add(mavMsg);
		mavMsg.autocontinue = 1;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.x = (float) coordinate.getLat();
		mavMsg.y = (float) coordinate.getLon();
		mavMsg.z = (float) coordinate.getAltitude();
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LOITER_TURNS;
		mavMsg.param1 = Math.abs(turns);
		mavMsg.param3 = (float) radius;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setTurns((int) mavMsg.param1);
		setRadius(mavMsg.param3);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CIRCLE;
	}

	@Override
	public MavlinkCircle clone(DroneMission droneMission) {
		MavlinkCircle mavlinkCircle = new MavlinkCircle(this);
		mavlinkCircle.setDroneMission(droneMission);
		return mavlinkCircle;
	}

	@Override
	public void accept(ConvertMavlinkVisitor convertMavlinkVisitor) {
		convertMavlinkVisitor.visit(this);
	}

}