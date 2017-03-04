package gui.controllers.internalFrames.internal.view_tree_layers;

import gui.core.mapViewer.LayeredViewMap;
import gui.core.mapViewerObjects.MapMarkerCircle;
import is.gui.services.DialogManagerSvc;
import is.springConfig.AppConfig;
import geoTools.Coordinate;
import geoTools.GeoTools;

public class LayerCircledPerimeter extends LayerPerimeter {
	
	private MapMarkerCircle currentMarker;
	
	public LayerCircledPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}
	
	public LayerCircledPerimeter(LayerCircledPerimeter layerCirclePerimeter, LayeredViewMap viewMap) {
		super(layerCirclePerimeter, viewMap);
		this.currentMarker = (MapMarkerCircle) layerCirclePerimeter.currentMarker.clone();
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

	@Override
	public void add(Coordinate position) {
		DialogManagerSvc dialogManagerSvc = (DialogManagerSvc) AppConfig.context.getBean(DialogManagerSvc.class);
		
		String val = (String) dialogManagerSvc.showInputDialog("Please choose radius", "Cyclic Perimeter",
				null, null, "10");
		
		if (currentMarker != null)
			removeMapMarker(currentMarker);
		
		int radi = Integer.parseInt(val);
		currentMarker = new MapMarkerCircle("GeoFence: " + radi + "m", position, radi);
		addMapMarker(currentMarker);
	}
}
