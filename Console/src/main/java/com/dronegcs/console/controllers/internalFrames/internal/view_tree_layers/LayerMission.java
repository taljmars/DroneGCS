package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;
import com.gui.core.mapViewer.LayeredViewMap;
import com.gui.core.mapViewerObjects.MapLineImpl;
import com.gui.core.mapViewerObjects.MapMarkerCircle;
import com.gui.core.mapViewerObjects.MapMarkerDot;
import com.gui.is.interfaces.mapObjects.MapLine;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LayerMission extends EditedLayer {

	private final static Logger LOGGER = LoggerFactory.getLogger(LayerMission.class);
	
	private Mission mission;
	private ApplicationContext applicationContext;

	public LayerMission(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
		startEditing();
	}

	public LayerMission(LayerMission layerMission, LayeredViewMap viewMap) throws MissionUpdateException {
		super(layerMission, viewMap);
		LOGGER.debug("Before copy " + layerMission.getMission());
		MissionsManager missionsManager = applicationContext.getBean(MissionsManager.class);
		mission = missionsManager.cloneMission(layerMission.getMission());
		startEditing();
		LOGGER.debug("After copy " + mission);
	}

	public LayerMission(Mission mission, LayeredViewMap layeredViewMap) {
		this(mission.getName(), layeredViewMap);
		this.mission = mission;
	}

	public LayerMission(Mission mission, LayeredViewMap layeredViewMap, boolean isEditing) {
		super(mission.getName(), layeredViewMap);
		this.mission = mission;
		if (isEditing)
			startEditing();
		else
			stopEditing();
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

		MissionsManager missionsManager = applicationContext.getBean(MissionsManager.class);
		List<MissionItem> missionItemList = missionsManager.getMissionItems(mission);

		MapLine route = null;
		Iterator<MissionItem> it = missionItemList.iterator();
		ArrayList<Coordinate> points = new ArrayList<Coordinate>();
		int i = 0;
		while (it.hasNext()) {
			MissionItem item = it.next();
			//TODO: Remove print
			System.out.println(item.getClass());
			if (item instanceof Waypoint) {
				Waypoint wp = (Waypoint) item;
				//MapMarkerDot m = new MapMarkerDot(this,  MissionItemsType.WAYPOINT.getName() + i, wp.getCoordinate().getLat(), wp.getCoordinate().getLng());
				MapMarkerDot m = new MapMarkerDot("W" + i, wp.getLat(), wp.getLon());
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
			else if (item instanceof LoiterTurns) {
                LoiterTurns wp = (LoiterTurns) item;
				Coordinate coordinate = new Coordinate(wp.getLat(), wp.getLon());
				MapMarkerCircle m = new MapMarkerCircle("" + wp.getTurns() + "T", coordinate, GeoTools.metersTolat(10));
				addMapMarker(m);
				points.add(coordinate);
			}
            else if (item instanceof LoiterTime) {
                LoiterTime wp = (LoiterTime) item;
                Coordinate coordinate = new Coordinate(wp.getLat(), wp.getLon());
                MapMarkerCircle m = new MapMarkerCircle(wp.getSeconds() + "S", coordinate, GeoTools.metersTolat(10));
                addMapMarker(m);
                points.add(coordinate);
            }
            else if (item instanceof LoiterUnlimited) {
                LoiterUnlimited wp = (LoiterUnlimited) item;
                Coordinate coordinate = new Coordinate(wp.getLat(), wp.getLon());
                MapMarkerCircle m = new MapMarkerCircle("", coordinate, GeoTools.metersTolat(10));
                addMapMarker(m);
                points.add(coordinate);
            }
			else if (item instanceof Land) {
				Land lnd = (Land) item;
				Coordinate coordinate = new Coordinate(lnd.getLat(), lnd.getLon());
				MapMarkerDot m = new MapMarkerDot("L", Color.MAGENTA, coordinate);
				addMapMarker(m);
				points.add(coordinate);
			}
			else if (item instanceof ReturnToHome) {
				Coordinate c = points.get(points.size() - 1);
				MapMarkerDot m = new MapMarkerDot("RTL", Color.MAGENTA, c);
				addMapMarker(m);
			}
			else if (item instanceof RegionOfInterest) {
				RegionOfInterest regionOfInterest = (RegionOfInterest) item;
				Coordinate coordinate = new Coordinate(regionOfInterest.getLat(), regionOfInterest.getLon());
				MapMarkerDot m = new MapMarkerDot("ROI", Color.MAGENTA, coordinate);
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
			else if (item instanceof SplineWaypoint) {
				SplineWaypoint splineWaypoint = (SplineWaypoint) item;
				Coordinate coordinate = new Coordinate(splineWaypoint.getLat(), splineWaypoint.getLon());
				MapMarkerDot m = new MapMarkerDot("W" + i, Color.MAROON, coordinate);
				addMapMarker(m);
                points.add(coordinate);
			}
			else {
				throw new RuntimeException("Unexpected Type: " + item);
			}
			i++;
			System.err.println("Generate " + i + " points");
		}
		
		route = new MapLineImpl(points);
		addMapLine(route);
		System.err.println("updateLine");
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public String toString2() {
		return String.format("LayerMission: %s\nMission Name:%s ,Mission Items: %d",
				getName(true), mission.getName(), mission.getMissionItemsUids().size());
	}
}
