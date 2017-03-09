package controllers.internalFrames.internal.view_tree_layers;

import gui.core.mapViewer.LayeredViewMap;
import gui.core.mapViewerObjects.MapMarkerCircle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import services.DialogManagerSvc;
import geoTools.Coordinate;
import geoTools.GeoTools;

import javax.validation.constraints.NotNull;

public class LayerCircledPerimeter extends LayerPerimeter {

	@Autowired @NotNull(message = "Internal Error: Fail to gett application context")
	private ApplicationContext applicationContext;
	
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
		DialogManagerSvc dialogManagerSvc = applicationContext.getBean(DialogManagerSvc.class);
		
		String val = (String) dialogManagerSvc.showInputDialog("Please choose radius", "Cyclic Perimeter",
				null, null, "10");
		
		if (currentMarker != null)
			removeMapMarker(currentMarker);
		
		int radi = Integer.parseInt(val);
		currentMarker = new MapMarkerCircle("GeoFence: " + radi + "m", position, radi);
		addMapMarker(currentMarker);
	}
}
