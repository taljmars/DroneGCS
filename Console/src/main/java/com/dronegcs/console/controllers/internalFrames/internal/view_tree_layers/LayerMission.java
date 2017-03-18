package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import java.util.ArrayList;
import java.util.Iterator;

import com.gui.core.mapTreeObjects.LayerSingle;
import com.gui.core.mapViewer.LayeredViewMap;
import com.gui.core.mapViewerObjects.MapLineImpl;
import com.gui.core.mapViewerObjects.MapMarkerCircle;
import com.gui.core.mapViewerObjects.MapMarkerDot;
import com.gui.is.interfaces.mapObjects.MapLine;
import javafx.scene.paint.Color;
import com.dronegcs.mavlink.is.drone.mission.Mission;
import com.dronegcs.mavlink.is.drone.mission.MissionItem;
import com.dronegcs.mavlink.is.drone.mission.waypoints.Circle;
import com.dronegcs.mavlink.is.drone.mission.waypoints.Land;
import com.dronegcs.mavlink.is.drone.mission.waypoints.RegionOfInterest;
import com.dronegcs.mavlink.is.drone.mission.waypoints.Waypoint;
import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;

public class LayerMission extends LayerSingle {
	
	private Mission mission;

	public LayerMission(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}

	public LayerMission(LayerMission layerMission, LayeredViewMap viewMap) {
		super(layerMission, viewMap);
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
					MapMarkerDot m = new MapMarkerDot(wp.getCoordinate().getLat(), wp.getCoordinate().getLon());
					addMapMarker(m);
					points.add(wp.getCoordinate());
					break;
				}
				case SPLINE_WAYPOINT:
					//return new SplineWaypoint(referenceItem);
				case TAKEOFF: {
					if (!mission.getDrone().getGps().isPositionValid())
						return;
					Coordinate curr = mission.getDrone().getGps().getPosition();
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
					MapMarkerDot m = new MapMarkerDot(Color.MAGENTA, lnd.getCoordinate());
					addMapMarker(m);
					points.add(lnd.getCoordinate());
					break;
				}
				case CIRCLE: {
					Circle wp = (Circle) item;
					//MapMarkerCircle m = new MapMarkerCircle(this, wp.getCoordinate().getLat(), wp.getCoordinate().getLng(), GeoTools.metersTolat(10));
					MapMarkerCircle m = new MapMarkerCircle(wp.getCoordinate(), GeoTools.metersTolat(10));
					//m.setBackColor(Color.MAGENTA);
					addMapMarker(m);
					points.add(wp.getCoordinate());
					break;
				}
				case ROI:
					RegionOfInterest roi = (RegionOfInterest) item;
					MapMarkerDot m = new MapMarkerDot(Color.AQUA, roi.getCoordinate());
					//m.setBackColor(Color.MAGENTA);
					addMapMarker(m);
					points.add(roi.getCoordinate());
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
