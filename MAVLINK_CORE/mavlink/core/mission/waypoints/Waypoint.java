package mavlink.core.mission.waypoints;

import java.io.Serializable;
import java.util.List;

import mavlink.core.mission.Mission;
import mavlink.core.mission.MissionItem;
import mavlink.core.mission.MissionItemType;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.utils.coordinates.Coord3D;

public class Waypoint extends SpatialCoordItem implements Serializable /* TALMA serializble*/ {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3576058694665759132L;
	private double delay;
	private double acceptanceRadius;
	private double yawAngle;
	private double orbitalRadius;
	private boolean orbitCCW;

	public Waypoint(MissionItem item) {
		super(item);
	}

	public Waypoint(Mission mission, Coord3D coord) {
		super(mission, coord);
	}

	public Waypoint(msg_mission_item msg, Mission mission) {
		super(mission, null);
		unpackMAVMessage(msg);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		mavMsg.param1 = (float) getDelay();
		mavMsg.param2 = (float) getAcceptanceRadius();
		mavMsg.param3 = (float) (isOrbitCCW() ? getOrbitalRadius() * -1.0 : getOrbitalRadius());
		mavMsg.param4 = (float) getYawAngle();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setDelay(mavMsg.param1);
		setAcceptanceRadius(mavMsg.param2);
		setOrbitCCW(mavMsg.param3 < 0);
		setOrbitalRadius(Math.abs(mavMsg.param3));
		setYawAngle(mavMsg.param4);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.WAYPOINT;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public double getAcceptanceRadius() {
		return acceptanceRadius;
	}

	public void setAcceptanceRadius(double acceptanceRadius) {
		this.acceptanceRadius = acceptanceRadius;
	}

	public double getYawAngle() {
		return yawAngle;
	}

	public void setYawAngle(double yawAngle) {
		this.yawAngle = yawAngle;
	}

	public double getOrbitalRadius() {
		return orbitalRadius;
	}

	public void setOrbitalRadius(double orbitalRadius) {
		this.orbitalRadius = orbitalRadius;
	}

	public boolean isOrbitCCW() {
		return orbitCCW;
	}

	public void setOrbitCCW(boolean orbitCCW) {
		this.orbitCCW = orbitCCW;
	}

}