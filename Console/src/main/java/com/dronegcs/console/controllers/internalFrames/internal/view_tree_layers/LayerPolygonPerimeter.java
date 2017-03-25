package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.dronedb.persistence.scheme.perimeter.Point;
import com.dronedb.persistence.scheme.perimeter.PolygonPerimeter;
import com.gui.core.mapViewer.LayeredViewMap;
import com.gui.core.mapViewerObjects.MapPolygonImpl;
import com.gui.is.interfaces.mapObjects.MapPolygon;
import com.geo_tools.Coordinate;

public class LayerPolygonPerimeter extends LayerPerimeter {

	private MapPolygon currentPolygon;

	private PolygonPerimeter polygonPerimeter;
	
	public LayerPolygonPerimeter(PolygonPerimeter polygonPerimeter, LayeredViewMap viewMap) {
		super(polygonPerimeter.getName(), viewMap);
		this.polygonPerimeter = polygonPerimeter;
	}
	
	public LayerPolygonPerimeter(LayerPolygonPerimeter layerPerimeter, LayeredViewMap viewMap) {
		super(layerPerimeter, viewMap);
		this.currentPolygon = (MapPolygon) layerPerimeter.currentPolygon.clone();
	}

	public void addPolygon(MapPolygon poly) {
		currentPolygon = poly;
		regenerateMapObjects();
	}

	public void add(Coordinate position) {
		if (currentPolygon == null)
			currentPolygon = new MapPolygonImpl();
		
		currentPolygon.addCoordinate(position);
		this.polygonPerimeter.addPoint(new Point(position.getLat(), position.getLon()));
		regenerateMapObjects();
		
	}

	public void regenerateMapObjects() {
		removeAllMapObjects();
		addMapPolygon(currentPolygon);
	}

	public MapPolygon getPolygon() {
		if (getMapPolygons().size() == 0)
			return null;
		
		return getMapPolygons().get(0);
	}

	public PolygonPerimeter getPolygonPerimeter() {
		return this.polygonPerimeter;
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
		this.polygonPerimeter = polygonPerimeter;
	}
}
