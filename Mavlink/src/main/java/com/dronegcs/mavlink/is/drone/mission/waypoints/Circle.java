package com.dronegcs.mavlink.is.drone.mission.waypoints;

import java.util.ArrayList;
import java.util.List;

import com.dronegcs.mavlink.is.drone.mission.Mission;
import com.dronegcs.mavlink.is.drone.mission.MissionItemType;
import com.dronegcs.mavlink.is.drone.mission.waypoints.interfaces.Radiusable;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_FRAME;
import geoTools.Coordinate;

public class Circle extends SpatialCoordItem implements Radiusable {

	private double radius = 10.0;
	private int turns = 1;

	public Circle(Circle circle) {
		super(circle);
		this.radius = circle.radius;
		this.turns = circle.turns;
	}

	public Circle(Mission mission, Coordinate coord) {
		super(mission, coord);
	}

	public Circle(msg_mission_item msg, Mission mission) {
		super(mission, null);
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
	public Circle clone(Mission mission) {
		Circle circle = new Circle(this);
		circle.setMission(mission);
		return circle;
	}

}