package gui.core.mapTreeObjects;

import gui.core.mapViewerObjects.MapPolygonImpl;
import gui.is.Coordinate;
import gui.is.interfaces.mapObjects.MapPolygon;

public class LayerPolygonPerimeter extends LayerPerimeter {

	private MapPolygon currentPolygon;
	
	public LayerPolygonPerimeter(String name) {
		super(name);
	}
	
	public LayerPolygonPerimeter(LayerPolygonPerimeter layerPerimeter) {
		super(layerPerimeter);
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
		regenerateMapObjects();
		
	}

	public void regenerateMapObjects() {
		removeAllMapObjects();
		addMapPolygon(currentPolygon);
	}

	public MapPolygon getPerimeter() {
		if (getMapPolygons().size() == 0)
			return null;
		
		return getMapPolygons().get(0);
	}

	@Override
	public boolean isContained(Coordinate coord) {
		return currentPolygon.contains(coord);
	}
	
	@Override
	public Coordinate getClosestPointOnEdge(Coordinate coord) {
		return currentPolygon.getClosestPointOnEdge(coord);
	}
}