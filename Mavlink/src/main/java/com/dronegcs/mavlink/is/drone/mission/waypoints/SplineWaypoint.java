package is.drone.mission.waypoints;

import java.util.List;

import is.drone.mission.Mission;
import is.drone.mission.MissionItemType;
import is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import is.protocol.msg_metadata.enums.MAV_CMD;
import geoTools.Coordinate;

/**
 * Handle spline waypoint com.dronegcs.mavlink.is.mavlink packet generation.
 */
public class SplineWaypoint extends SpatialCoordItem {

	/**
	 * Hold time in decimal seconds. (ignored by fixed wing, time to stay at
	 * MISSION for rotary wing)
	 */
	private double delay;

	public SplineWaypoint(SplineWaypoint splineWaypoint) {
		super(splineWaypoint);
		this.delay = splineWaypoint.delay;
	}

	public SplineWaypoint(Mission mission, Coordinate coord) {
		super(mission, coord);
	}

	public SplineWaypoint(msg_mission_item msg, Mission mission) {
		super(mission, null);
		unpackMAVMessage(msg);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_SPLINE_WAYPOINT;
		mavMsg.param1 = (float) delay;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setDelay(mavMsg.param1);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.SPLINE_WAYPOINT;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}
	
	@Override
	public SplineWaypoint clone(Mission mission) {
		SplineWaypoint splineWaypoint = new SplineWaypoint(this);
		splineWaypoint.setMission(mission);
		return splineWaypoint;
	}
}
