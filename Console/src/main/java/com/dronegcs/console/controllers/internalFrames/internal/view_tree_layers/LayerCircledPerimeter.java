package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Point;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;
import com.gui.core.mapViewer.LayeredViewMap;
import com.gui.core.mapViewerObjects.MapMarkerCircle;

import java.util.List;

public class LayerCircledPerimeter extends LayerPerimeter<CirclePerimeter> {
	
	private MapMarkerCircle currentMarker;

	public LayerCircledPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}
	
	public LayerCircledPerimeter(CirclePerimeter circlePerimeter, LayeredViewMap viewMap) {
		super(circlePerimeter.getName(), viewMap);
		this.perimeter = circlePerimeter;
	}
	
	public LayerCircledPerimeter(LayerCircledPerimeter layerCirclePerimeter, LayeredViewMap viewMap) throws PerimeterUpdateException {
		super(layerCirclePerimeter, viewMap);
		this.currentMarker = layerCirclePerimeter.currentMarker.clone();
	}

	public LayerCircledPerimeter(CirclePerimeter perimeter, LayeredViewMap layeredViewMap, boolean isEditing) {
		super(perimeter, layeredViewMap, isEditing);
	}

	@Override
	public boolean isContained(Coordinate coord) {
		return currentMarker.contains(coord);
	}

	@Override
	public Coordinate getClosestPointOnEdge(Coordinate coord) {
		double heading = GeoTools.getHeadingFromCoordinates(currentMarker.getCoordinate(), coord);
		return GeoTools.newCoordFromBearingAndDistance(currentMarker.getCoordinate(), heading, currentMarker.getRadius());
	}

    public CirclePerimeter getCirclePerimeter() {
        return this.perimeter;
    }

	public void setCirclePerimeter(CirclePerimeter circlePerimeter) {
		this.perimeter = circlePerimeter;
	}

	public MapMarkerCircle getCircle() {
		return currentMarker;
	}

	/////

	public void regenerateMapObjects() {
		removeAllMapObjects();

		if (perimeter == null)
			return;

		PerimetersManager perimetersManager = applicationContext.getBean(PerimetersManager.class);
		List<Point> pointList = perimetersManager.getPoints(perimeter);
		if (pointList == null || pointList.isEmpty()) {
			//TODO: Logger messege
			return;
		}
		Point point = pointList.get(0);

		MapMarkerCircle mapPolygon = new MapMarkerCircle(getName(true), point.getLat(), point.getLon(), perimeter.getRadius());
		currentMarker = mapPolygon;
		addMapMarker(currentMarker);
	}
}
