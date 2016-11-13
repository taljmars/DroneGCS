package gui.core.mapTreeObjects;

import gui.core.mapViewerObjects.MapMarkerCircle;
import gui.core.springConfig.AppConfig;
import gui.is.Coordinate;
import gui.is.services.DialogManagerSvc;
import mavlink.is.utils.geoTools.GeoTools;

public class LayerCircledPerimeter extends LayerPerimeter {
	
	private MapMarkerCircle currentMarker;
	
	public LayerCircledPerimeter(String name) {
		super(name);
	}
	
	public LayerCircledPerimeter(LayerCircledPerimeter layerCirclePerimeter) {
		super(layerCirclePerimeter);
		this.currentMarker = (MapMarkerCircle) layerCirclePerimeter.currentMarker.clone();
	}

	@Override
	public boolean isContained(Coordinate coord) {
		return currentMarker.contains(coord);
	}

	@Override
	public Coordinate getClosestPointOnEdge(Coordinate coord) {
		double heading = GeoTools.getHeadingFromCoordinates(currentMarker.getCoordinate(), coord);
		return GeoTools.newCoordFromBearingAndDistance(currentMarker.getCoordinate().ConvertToCoord2D(), heading, currentMarker.getRadius()).convertToCoordinate();
	}

	@Override
	public void add(Coordinate position) {
		DialogManagerSvc dialogManagerSvc = (DialogManagerSvc) AppConfig.context.getBean("dialogManagerSvc");
		
		String val = (String) dialogManagerSvc.showInputDialog("Please choose radius", "Cyclic Perimeter",
				null, null, "10");
		
		if (currentMarker != null)
			removeMapMarker(currentMarker);
		
		int radi = Integer.parseInt(val);
		currentMarker = new MapMarkerCircle("GeoFence: " + radi + "m", position, radi);
		addMapMarker(currentMarker);
	}
}
