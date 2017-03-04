package is.mavlink.drone.mission.waypoints;

import java.util.List;

import is.mavlink.drone.mission.Mission;
import is.mavlink.drone.mission.MissionItem;
import is.mavlink.drone.mission.waypoints.interfaces.Altitudable;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import tools.geoTools.Coordinate;

public abstract class SpatialCoordItem extends MissionItem implements Altitudable {

	protected Coordinate coordinate;

	public SpatialCoordItem(Mission mission, Coordinate coord) {
		super(mission);
		this.coordinate = coord;
	}

	public SpatialCoordItem(MissionItem item) {
		super(item);
		if (item instanceof SpatialCoordItem) {
			coordinate = ((SpatialCoordItem) item).getCoordinate();
		} else {
			coordinate = new Coordinate(0, 0, 0);
		}
	}

	public void setCoordinate(Coordinate coordNew) {
		coordinate = coordNew;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.x = (float) coordinate.getLat();
		mavMsg.y = (float) coordinate.getLon();
		mavMsg.z = (float) coordinate.getAltitude();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		double alt = mavMsg.z;
		setCoordinate(new Coordinate(mavMsg.x, mavMsg.y, alt));
	}

	@Override
	public void setAltitude(double altitude) {
		coordinate.set(coordinate.getLat(), coordinate.getLon(), altitude);
	}
	
	@Override
	public double getAltitude() {
		return coordinate.getAltitude();
	}

	public void setPosition(Coordinate position) {
		coordinate.set(position.getLat(), position.getLon(), position.getAltitude());
	}
}