package com.gui.core.mapTreeObjects;

import com.gui.core.mapViewer.ViewMap;
import com.gui.is.interfaces.mapObjects.MapLine;
import com.gui.is.interfaces.mapObjects.MapMarker;
import com.gui.is.interfaces.mapObjects.MapPolygon;

import java.util.Vector;

public class LayerSingle extends Layer {
	
	private Vector<MapMarker> mapMarkers;
	private Vector<MapLine> mapLines;
	private Vector<MapPolygon> mapPolygons;
	private ViewMap viewMap;
	
	public LayerSingle(String name, ViewMap viewMap) {
		super(name);
		this.viewMap = viewMap;
		initLocals();
	}
	
	public LayerSingle(LayerSingle layer, ViewMap viewMap) {
		super(layer);
		this.viewMap = viewMap;
		initLocals();
		
		for (MapMarker m : layer.mapMarkers) this.mapMarkers.addElement((MapMarker) m.clone());
		for (MapLine m : layer.mapLines) this.mapLines.addElement((MapLine) m.clone());
		for (MapPolygon m : layer.mapPolygons) this.mapPolygons.addElement((MapPolygon) m.clone());
	}
	
	private void initLocals() {
		mapMarkers = new Vector<>();
		mapPolygons = new Vector<>();
		mapLines = new Vector<>();
//		viewMap = (ViewMap) AppConfig.context.getBean("map");
	}
	
	protected Vector<MapPolygon> getMapPolygons() {
		return mapPolygons;
	}
		
	protected void addMapMarker(MapMarker marker) {
		viewMap.addMapMarker(marker);
    	mapMarkers.addElement(marker);
    }
	
	protected void removeMapMarker(MapMarker marker) {
		viewMap.removeMapMarker(marker);
    	mapMarkers.removeElement(marker);
	}
	
	protected void showAllMapMarkers() {
		for (MapMarker mapMarker : mapMarkers)
			viewMap.addMapMarker(mapMarker);
    }
	
	protected void addMapPolygon(MapPolygon polygon) {
		viewMap.addMapPolygon(polygon);
		mapPolygons.addElement(polygon);
    }
	
	protected void showAllMapPolygons() {
		for (MapPolygon polygon : mapPolygons)
			viewMap.addMapPolygon(polygon);
    }
    
	protected void addMapLine(MapLine line) {
		viewMap.addMapLine(line);
		mapLines.addElement(line);
	}
	
	protected void showAllMapLines() {
		for (MapLine mapLine : mapLines)
			viewMap.addMapLine(mapLine);
	}
	
	protected void removeAllMapLines() {
		hideAllMapLines();
		mapLines.removeAllElements();
	}
	
	protected void hideAllMapLines() {
		for (MapLine mapLine : mapLines)
			viewMap.removeMapLine(mapLine);
	}

	protected void removeAllMapMarkers() {
		hideAllMapMarkers();
		mapMarkers.removeAllElements();
	}
	
	protected void hideAllMapMarkers() {
		for (MapMarker mapMarker : mapMarkers)
			viewMap.removeMapMarker(mapMarker);
	}
	
	protected void removeAllMapPolygons() {
		hideAllMapPolygons();
		mapPolygons.removeAllElements();
	}
	
	protected void hideAllMapPolygons() {
		for (MapPolygon polygon : mapPolygons)
			viewMap.removeMapPolygon(polygon);
	}
	
	public void removeAllMapObjects() {
		System.err.println("Remove all map obejct");
		removeAllMapLines();
		removeAllMapMarkers();
		removeAllMapPolygons();
	}
	
	public void hideAllMapObjects() {
		hideAllMapLines();
		hideAllMapMarkers();
		hideAllMapPolygons();
	}
	
	public void showAllMapObjects() {
		showAllMapLines();
		showAllMapMarkers();
		showAllMapPolygons();
	}
	
	@Override
	public void regenerateMapObjects() {
		hideAllMapObjects();
		showAllMapObjects();
	}
	
}
