package is.drone.mission;

import is.drone.Drone;
import is.drone.DroneVariable;
import is.drone.DroneInterfaces.DroneEventsType;
import is.drone.mission.commands.CameraTrigger;
import is.drone.mission.commands.ChangeSpeed;
import is.drone.mission.commands.EpmGripper;
import is.drone.mission.commands.ReturnToHome;
import is.drone.mission.commands.Takeoff;
import is.drone.mission.waypoints.Circle;
import is.drone.mission.waypoints.Land;
import is.drone.mission.waypoints.RegionOfInterest;
import is.drone.mission.waypoints.SpatialCoordItem;
import is.drone.mission.waypoints.SplineWaypoint;
import is.drone.mission.waypoints.Waypoint;
import is.protocol.msg_metadata.ardupilotmega.msg_mission_ack;
import is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import is.protocol.msg_metadata.enums.MAV_CMD;
import is.units.Speed;
import geoTools.Coordinate;
import geoTools.GeoTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import javafx.util.Pair;

import javax.validation.constraints.NotNull;

/**
 * This implements a com.dronegcs.mavlink.is.mavlink mission. A com.dronegcs.mavlink.is.mavlink mission is a set of
 * commands/mission items to be carried out by the drone. TODO: rename the
 * 'waypoint' method to 'missionItem' (i.e: addMissionItem)
 */

@Scope("prototype")
public class Mission extends DroneVariable implements Serializable {

	private static final long serialVersionUID = 8399081979944818494L;

	@Autowired @NotNull(message = "Internal Error: Failed to get applcation context")
	private ApplicationContext applicationContext;

	/**
	 * Stores the set of mission items belonging to this mission.
	 */
	private List<MissionItem> items = new ArrayList<MissionItem>();
	private double defaultAlt = 20.0;

	public Mission(){
		super();
	}

	public Mission(Mission mission) {
		super(mission);
		defaultAlt = mission.getDefaultAlt();
		for (MissionItem mi : mission.getItems()) items.add((MissionItem) mi.clone(this));
	}

	/**
	 * @return the mission's default altitude
	 */
	public double getDefaultAlt() {
		return defaultAlt;
	}

	/**
	 * Sets the mission default altitude.
	 * 
	 * @param newAltitude value
	 */
	public void setDefaultAlt(double newAltitude) {
		defaultAlt = newAltitude;
	}

	/**
	 * Removes a waypoint from the mission's set of mission items.
	 * 
	 * @param item
	 *            waypoint to remove
	 */
	public void removeWaypoint(MissionItem item) {
		items.remove(item);
		notifyMissionUpdate();
	}

	/**
	 * Removes a list of waypoints from the mission's set of mission items.
	 * 
	 * @param toRemove
	 *            list of waypoints to remove
	 */
	public void removeWaypoints(List<MissionItem> toRemove) {
		items.removeAll(toRemove);
		notifyMissionUpdate();
	}

	/**
	 * Add a list of waypoints to the mission's set of mission items.
	 * 
	 * @param missionItems
	 *            list of waypoints to add
	 */
	public void addMissionItems(List<MissionItem> missionItems) {
		items.addAll(missionItems);
		notifyMissionUpdate();
	}

	/**
	 * Add a waypoint to the mission's set of mission item.
	 * 
	 * @param missionItem
	 *            waypoint to add
	 */
	public void addMissionItem(MissionItem missionItem) {
		items.add(missionItem);
		notifyMissionUpdate();
	}

	public void addMissionItem(int index, MissionItem missionItem) {
		items.add(index, missionItem);
		notifyMissionUpdate();
	}

	/**
	 * Signals that this mission object was updated. //TODO: maybe move outside
	 * of this class
	 */
	public void notifyMissionUpdate() {
		drone.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	/**
	 * @return the altitude of the last added mission item.
	 */
	public double getLastAltitude() {
		double alt;
		try {
			SpatialCoordItem lastItem = (SpatialCoordItem) items.get(items.size() - 1);
			alt = lastItem.getCoordinate().getAltitude();
		} catch (Exception e) {
			alt = defaultAlt;
		}
		return alt;
	}

	/**
	 * Updates a mission item
	 * 
	 * @param oldItem
	 *            mission item to update
	 * @param newItem
	 *            new mission item
	 */
	public void replace(MissionItem oldItem, MissionItem newItem) {
		final int index = items.indexOf(oldItem);
		if (index == -1) {
			return;
		}

		items.remove(index);
		items.add(index, newItem);
		notifyMissionUpdate();
	}

	public void replaceAll(List<Pair<MissionItem, MissionItem>> updatesList) {
		if (updatesList == null || updatesList.isEmpty()) {
			return;
		}

		boolean wasUpdated = false;
		for (Pair<MissionItem, MissionItem> updatePair : updatesList) {
			final MissionItem oldItem = updatePair.getKey();
			final int index = items.indexOf(oldItem);
			if (index == -1) {
				continue;
			}

			final MissionItem newItem = updatePair.getValue();
			items.remove(index);
			items.add(index, newItem);

			wasUpdated = true;
		}

		if (wasUpdated) {
			notifyMissionUpdate();
		}
	}

	/**
	 * Reverse the order of the mission items.
	 */
	public void reverse() {
		Collections.reverse(items);
		notifyMissionUpdate();
	}

	public void onWriteWaypoints(msg_mission_ack msg) {
		drone.notifyDroneEvent(DroneEventsType.MISSION_SENT);
	}

	public List<MissionItem> getItems() {
		return items;
	}

	public int getOrder(MissionItem waypoint) {
		return items.indexOf(waypoint) + 1; // plus one to account for the fact
											// that this is an index
	}

	public double getAltitudeDiffFromPreviousItem(SpatialCoordItem waypoint)
			throws IllegalArgumentException {
		int i = items.indexOf(waypoint);
		if (i > 0) {
			MissionItem previous = items.get(i - 1);
			if (previous instanceof SpatialCoordItem) {
				double aa = ((SpatialCoordItem) previous).getCoordinate().getAltitude();
				return waypoint.getCoordinate().getAltitude() - aa;
			}
		}
		throw new IllegalArgumentException("Last waypoint doesn't have an altitude");
	}

	public double getDistanceFromLastWaypoint(SpatialCoordItem waypoint)
			throws IllegalArgumentException {
		int i = items.indexOf(waypoint);
		if (i > 0) {
			MissionItem previous = items.get(i - 1);
			if (previous instanceof SpatialCoordItem) {
				return GeoTools.getDistance(waypoint.getCoordinate(),
						((SpatialCoordItem) previous).getCoordinate());
			}
		}
		throw new IllegalArgumentException("Last waypoint doesn't have a coordinate");
	}

	public boolean hasItem(MissionItem item) {
		return items.contains(item);
	}

	public void onMissionReceived(List<msg_mission_item> msgs) {
		if (msgs != null) {
			drone.getHome().setHome(msgs.get(0));
			msgs.remove(0); // Remove Home waypoint
			items.clear();
			items.addAll(processMavLinkMessages(msgs));
			drone.notifyDroneEvent(DroneEventsType.MISSION_RECEIVED);
			notifyMissionUpdate();
		}
	}

	public void onMissionLoaded(List<msg_mission_item> msgs) {
		if (msgs != null) {
			drone.getHome().setHome(msgs.get(0));
			msgs.remove(0); // Remove Home waypoint
			items.clear();
			items.addAll(processMavLinkMessages(msgs));
			drone.notifyDroneEvent(DroneEventsType.MISSION_RECEIVED);
			notifyMissionUpdate();
		}
	}

	private List<MissionItem> processMavLinkMessages(List<msg_mission_item> msgs) {
		List<MissionItem> received = new ArrayList<MissionItem>();

		for (msg_mission_item msg : msgs) {
			switch (msg.command) {
			case MAV_CMD.MAV_CMD_NAV_WAYPOINT:
				received.add(new Waypoint(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_SPLINE_WAYPOINT:
				received.add(new SplineWaypoint(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_LAND:
				received.add(new Land(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
				received.add(new Takeoff(msg, this));
				break;
			case MAV_CMD.MAV_CMD_DO_CHANGE_SPEED:
				received.add(new ChangeSpeed(msg, this));
				break;
			case MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST:
				received.add(new CameraTrigger(msg,this));
				break;
			case EpmGripper.MAV_CMD_DO_GRIPPER:
				received.add(new EpmGripper(msg, this));
				break;
			case MAV_CMD.MAV_CMD_DO_SET_ROI:
				received.add(new RegionOfInterest(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_LOITER_TURNS:
				received.add(new Circle(msg, this));
				break;
			case MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH:
				received.add(new ReturnToHome(msg, this));
			default:
				break;
			}
		}
		return received;
	}

	/**
	 * Sends the mission to the drone using the com.dronegcs.mavlink.is.mavlink protocol.
	 */
	public void sendMissionToAPM() {
		drone.getWaypointManager().writeWaypoints(getMsgMissionItems());
	}

	public List<msg_mission_item> getMsgMissionItems() {
		final List<msg_mission_item> data = new ArrayList<msg_mission_item>();
		data.add(drone.getHome().packMavlink());
		for (MissionItem item : items) {
			data.addAll(item.packMissionItem());
		}
		return data;
	}

	/**
	 * Create and upload a dronie mission to the drone
	 * 
	 * @return the bearing in degrees the drone trajectory will take.
	 */
	public double makeAndUploadDronie() {
		Coordinate currentPosition = drone.getGps().getPosition();
		if (currentPosition == null || drone.getGps().getSatCount() <= 5) {
			drone.notifyDroneEvent(DroneEventsType.WARNING_NO_GPS);
			return -1;
		}

		final double bearing = 180 + drone.getOrientation().getYaw();
		items.clear();
		items.addAll(createDronie(currentPosition,
				GeoTools.newCoordFromBearingAndDistance(currentPosition, bearing, 50.0)));
		sendMissionToAPM();
		notifyMissionUpdate();

		return bearing;
	}

	public List<MissionItem> createDronie(Coordinate start, Coordinate end) {
		final int startAltitude = 4;
		final int roiDistance = -8;
		Coordinate slowDownPoint = GeoTools.pointAlongTheLine(start, end, 5);

		Speed defaultSpeed = drone.getSpeed().getSpeedParameter();
		if (defaultSpeed == null) {
			defaultSpeed = new Speed(5);
		}

		List<MissionItem> dronieItems = new ArrayList<MissionItem>();
		
		dronieItems.add(new Takeoff(this, 1.0 * startAltitude));
		
		dronieItems.add(new RegionOfInterest(this, new Coordinate(GeoTools.pointAlongTheLine(start, end, roiDistance), 1)));
		dronieItems.add(new Waypoint(this, new Coordinate(end, startAltitude + GeoTools.getDistance(start, end)/ 2.0)));
		dronieItems.add(new Waypoint(this, new Coordinate(slowDownPoint, startAltitude + GeoTools.getDistance(start, slowDownPoint) / 2.0)));
		dronieItems.add(new ChangeSpeed(this, new Speed(1.0)));
		dronieItems.add(new Waypoint(this, new Coordinate(start, startAltitude)));
		dronieItems.add(new ChangeSpeed(this, defaultSpeed));
		dronieItems.add(new Land(this, start));
		return dronieItems;
	}

	public boolean hasTakeoffAndLandOrRTL() {
		if (items.size() >= 2) {
			if (isFirstItemTakeoff() && isLastItemLandOrRTL()) {
				return true;
			}
		}
		return false;
	}

	public boolean isFirstItemTakeoff() {
		return !items.isEmpty() && items.get(0) instanceof Takeoff;
	}

	public boolean isLastItemLandOrRTL() {
        if(items.isEmpty())
            return false;

		MissionItem last = items.get(items.size() - 1);
		return (last instanceof ReturnToHome) || (last instanceof Land);
	}

	public Mission duplicate() {
		//Mission ans = new Mission();
		Mission ans = applicationContext.getBean(Mission.class);
		ans.setDrone(drone);
		Iterator<MissionItem> it = this.items.iterator();
		while (it.hasNext()) {
			ans.addMissionItem(it.next());
		}
		return ans;
	}

	public Drone getDrone() {
		return drone;
	}
	
	public boolean equals(Mission mission) {
		if (mission == null)
			return false;
		
		return mission.getItems().equals(this.getItems());
	}
}
