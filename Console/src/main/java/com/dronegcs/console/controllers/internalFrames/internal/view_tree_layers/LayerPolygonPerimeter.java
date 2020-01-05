package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.dronedb.persistence.scheme.Point;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.geo_tools.Coordinate;
import com.mapviewer.gui.core.mapViewer.LayeredViewMap;
import com.mapviewer.gui.core.mapViewerObjects.MapPolygonImpl;
import com.mapviewer.gui.is.interfaces.mapObjects.MapPolygon;

import java.util.List;

public class LayerPolygonPerimeter extends LayerPerimeter<PolygonPerimeter> {

	private MapPolygon currentPolygon;

	public LayerPolygonPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}
	
	public LayerPolygonPerimeter(PolygonPerimeter polygonPerimeter, LayeredViewMap viewMap) {
		super(polygonPerimeter.getName(), viewMap);
		this.perimeter = polygonPerimeter;
	}
	
	public void addPolygon(MapPolygon poly) {
		currentPolygon = poly;
		regenerateMapObjects();
	}

	@Override
	public void regenerateMapObjects() {
		removeAllMapObjects();

		if (perimeter == null)
			return;

		PerimetersManager perimetersManager = applicationContext.getBean(PerimetersManager.class);
		List<Point> pointList = perimetersManager.getPoints(perimeter);

		if (pointList.isEmpty())
			return;

		MapPolygon mapPolygon = new MapPolygonImpl();
		for (Point p : pointList)
			mapPolygon.addCoordinate(new Coordinate(p.getLat(), p.getLon()));
		currentPolygon = mapPolygon;
		addMapPolygon(currentPolygon);
	}

	public MapPolygon getPolygon() {
		if (getMapPolygons().size() == 0)
			return null;
		
		return getMapPolygons().get(0);
	}

	public PolygonPerimeter getPolygonPerimeter() {
		return (PolygonPerimeter) this.perimeter;
	}

	@Override
	public boolean isContained(Coordinate coord) {
		return currentPolygon.contains(coord);
	}
	
	@Override
	public Coordinate getClosestPointOnEdge(Coordinate coord) {
		return currentPolygon.getClosestPointOnEdge(coord);
	}

	public void setPolygonPerimeter(PolygonPerimeter polygonPerimeter) {
		this.perimeter = polygonPerimeter;
	}
}
