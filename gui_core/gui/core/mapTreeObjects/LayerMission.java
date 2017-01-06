package gui.core.mapTreeObjects;

import java.util.ArrayList;
import java.util.Iterator;

import gui.core.mapViewerObjects.MapLineImpl;
import gui.core.mapViewerObjects.MapMarkerCircle;
import gui.core.mapViewerObjects.MapMarkerDot;
import gui.is.Coordinate;
import gui.is.interfaces.mapObjects.MapLine;
import javafx.scene.paint.Color;
import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.MissionItem;
import mavlink.is.drone.mission.waypoints.Circle;
import mavlink.is.drone.mission.waypoints.Land;
import mavlink.is.drone.mission.waypoints.RegionOfInterest;
import mavlink.is.drone.mission.waypoints.Waypoint;
import mavlink.is.utils.geoTools.GeoTools;

public class LayerMission extends LayerSingle {
	
	private Mission mission;

	public LayerMission(String name) {
		super(name);
	}

	public LayerMission(LayerMission layerMission) {
		super(layerMission);
		mission = new Mission(layerMission.getMission());
	}

	public void setMission(Mission msn) {
		mission = msn;
	}

	public Mission getMission() {
		return mission;
	}
	
	@Override
	public void regenerateMapObjects() {
		removeAllMapObjects();
		
		if (mission == null)
			return;

		MapLine route = null;
		Iterator<MissionItem> it = mission.getItems().iterator();
		ArrayList<Coordinate> points = new ArrayList<Coordinate>();
		int i = 0;
		while (it.hasNext()) {
			MissionItem item = it.next();
			
			switch (item.getType()) {
				case WAYPOINT: {
					Waypoint wp = (Waypoint) item;
					//MapMarkerDot m = new MapMarkerDot(this,  MissionItemsType.WAYPOINT.getName() + i, wp.getCoordinate().getLat(), wp.getCoordinate().getLng());
					MapMarkerDot m = new MapMarkerDot(wp.getCoordinate().getLat(), wp.getCoordinate().getLng());
					addMapMarker(m);
					points.add(wp.getCoordinate().convertToCoordinate());
					break;
				}
				case SPLINE_WAYPOINT:
					//return new SplineWaypoint(referenceItem);
				case TAKEOFF: {
					if (!mission.getDrone().getGps().isPositionValid())
						return;
					Coordinate curr = mission.getDrone().getGps().getPosition().convertToCoordinate();
					//MapMarkerDot m = new MapMarkerDot(this, MissionItemsType.TAKEOFF.getName(), curr.getLat(), curr.getLon());
					MapMarkerDot m = new MapMarkerDot(Color.GREEN, curr);
					addMapMarker(m);
					points.add(curr);
					break;
				}
				case CHANGE_SPEED:
					//return new ChangeSpeed(referenceItem);
				case CAMERA_TRIGGER:
					//return new CameraTrigger(referenceItem);
				case EPM_GRIPPER:
					//return new EpmGripper(referenceItem);
				case RTL: {
					Coordinate c = points.get(points.size() - 1);
					//MapMarkerDot m = new MapMarkerDot(this, MissionItemsType.RTL.getName(), c.getLat(), c.getLon());
					MapMarkerDot m = new MapMarkerDot(Color.MAGENTA, c);
					addMapMarker(m);
					break;
				}
				case LAND: {
					Land lnd = (Land) item;
					//MapMarkerDot m = new MapMarkerDot(this, MissionItemsType.LAND.getName(), lnd.getCoordinate().getLat(), lnd.getCoordinate().getLng());
					MapMarkerDot m = new MapMarkerDot(Color.MAGENTA, lnd.getCoordinate().convertToCoordinate());
					addMapMarker(m);
					points.add(lnd.getCoordinate().convertToCoordinate());
					break;
				}
				case CIRCLE: {
					Circle wp = (Circle) item;
					//MapMarkerCircle m = new MapMarkerCircle(this, wp.getCoordinate().getLat(), wp.getCoordinate().getLng(), GeoTools.metersTolat(10));
					MapMarkerCircle m = new MapMarkerCircle(wp.getCoordinate().convertToCoordinate(), GeoTools.metersTolat(10));
					//m.setBackColor(Color.MAGENTA);
					addMapMarker(m);
					points.add(wp.getCoordinate().convertToCoordinate());
					break;
				}
				case ROI:
					RegionOfInterest roi = (RegionOfInterest) item;
					MapMarkerDot m = new MapMarkerDot(Color.AQUA, roi.getCoordinate().convertToCoordinate());
					//m.setBackColor(Color.MAGENTA);
					addMapMarker(m);
					points.add(roi.getCoordinate().convertToCoordinate());
					break;
				case SURVEY:
					//return new Survey(referenceItem.getMission(), Collections.<Coord2D> emptyList());
				case CYLINDRICAL_SURVEY:
					//return new StructureScanner(referenceItem);
				default:
					break;
			}
			i++;
			System.err.println("Generate " + i + " points");
		}
		
		route = new MapLineImpl(points);
		addMapLine(route);
		System.err.println("updateLine");
	}
}
