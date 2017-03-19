package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import java.util.ArrayList;
import java.util.Iterator;

import com.dronedb.persistence.scheme.*;
import com.gui.core.mapTreeObjects.LayerSingle;
import com.gui.core.mapViewer.LayeredViewMap;
import com.gui.core.mapViewerObjects.MapLineImpl;
import com.gui.core.mapViewerObjects.MapMarkerCircle;
import com.gui.core.mapViewerObjects.MapMarkerDot;
import com.gui.is.interfaces.mapObjects.MapLine;
import javafx.scene.paint.Color;
import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;

public class LayerMission extends LayerSingle {
	
	private Mission mission;

	public LayerMission(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}

	public LayerMission(LayerMission layerMission, LayeredViewMap viewMap) {
		super(layerMission, viewMap);
		mission = new Mission();
	}

	public LayerMission(Mission mission, LayeredViewMap layeredViewMap) {
		this(mission.getName(), layeredViewMap);
		this.mission = mission;
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
		Iterator<MissionItem> it = mission.getMissionItems().iterator();
		ArrayList<Coordinate> points = new ArrayList<Coordinate>();
		int i = 0;
		while (it.hasNext()) {
			MissionItem item = it.next();
			//TODO: Remove print
			System.out.println(item.getClass());
			if (item instanceof Waypoint) {
				Waypoint wp = (Waypoint) item;
				//MapMarkerDot m = new MapMarkerDot(this,  MissionItemsType.WAYPOINT.getName() + i, wp.getCoordinate().getLat(), wp.getCoordinate().getLng());
				MapMarkerDot m = new MapMarkerDot(wp.getLat(), wp.getLon());
				addMapMarker(m);
				points.add(m.getCoordinate());

				//SPLINE_WAYPOINT:
				//return new MavlinkSplineWaypoint(referenceItem);
//				case CHANGE_SPEED:
//					//return new MavlinkChangeSpeed(referenceItem);
//				case CAMERA_TRIGGER:
//					//return new MavlinkCameraTrigger(referenceItem);
//				case EPM_GRIPPER:
//					//return new MavlinkEpmGripper(referenceItem);
			}
			else if (item instanceof Circle) {
				Circle wp = (Circle) item;
				Coordinate coordinate = new Coordinate(wp.getLat(), wp.getLon());
				MapMarkerCircle m = new MapMarkerCircle(coordinate, GeoTools.metersTolat(10));
				addMapMarker(m);
				points.add(coordinate);
//				case ROI:
//					RegionOfInterest roi = (MavlinkRegionOfInterest) item;
//					MapMarkerDot m = new MapMarkerDot(Color.AQUA, roi.getCoordinate());
//					//m.setBackColor(Color.MAGENTA);
//					addMapMarker(m);
//					points.add(roi.getCoordinate());
//					break;
//				case SURVEY:
//					//return new MavlinkSurvey(referenceItem.getMission(), Collections.<Coord2D> emptyList());
//				case CYLINDRICAL_SURVEY:
//					//return new MavlinkStructureScanner(referenceItem);
//				default:
//					break;
			}
			else if (item instanceof Land) {
				Land lnd = (Land) item;
				Coordinate coordinate = new Coordinate(lnd.getLat(), lnd.getLon());
				MapMarkerDot m = new MapMarkerDot(Color.MAGENTA, coordinate);
				addMapMarker(m);
				points.add(coordinate);
			}
			else if (item instanceof ReturnToHome) {
				Coordinate c = points.get(points.size() - 1);
				MapMarkerDot m = new MapMarkerDot(Color.MAGENTA, c);
				addMapMarker(m);
			}
			else if (item instanceof Takeoff) {
				//if (!mission.getDrone().getGps().isPositionValid())
//					return;
				//Coordinate curr = mission.getDrone().getGps().getPosition();
				//MapMarkerDot m = new MapMarkerDot(this, MissionItemsType.TAKEOFF.getName(), curr.getLat(), curr.getLon());
				//MapMarkerDot m = new MapMarkerDot(Color.GREEN, curr);
				//addMapMarker(m);
				//points.add(curr);
			}
			else {
				System.out.println("Uexpected");
			}
			i++;
			System.err.println("Generate " + i + " points");
		}
		
		route = new MapLineImpl(points);
		addMapLine(route);
		System.err.println("updateLine");
	}
}
