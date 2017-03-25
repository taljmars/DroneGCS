package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.dronedb.persistence.scheme.perimeter.CirclePerimeter;
import com.dronedb.persistence.scheme.perimeter.Point;
import com.gui.core.mapViewer.LayeredViewMap;
import com.gui.core.mapViewerObjects.MapMarkerCircle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.dronegcs.console.services.DialogManagerSvc;
import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;

import javax.validation.constraints.NotNull;

public class LayerCircledPerimeter extends LayerPerimeter {
	
	private MapMarkerCircle currentMarker;

	private CirclePerimeter circlePerimeter;
	
	public LayerCircledPerimeter(CirclePerimeter circlePerimeter, LayeredViewMap viewMap) {
		super(circlePerimeter.getName(), viewMap);
		this.circlePerimeter = circlePerimeter;
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
		DialogManagerSvc dialogManagerSvc = getApplicationContext().getBean(DialogManagerSvc.class);
		
		String val = (String) dialogManagerSvc.showInputDialog("Please choose radius", "Cyclic Perimeter",
				null, null, "10");
		
		if (currentMarker != null)
			removeMapMarker(currentMarker);
		
		int radi = Integer.parseInt(val);
		currentMarker = new MapMarkerCircle("GeoFence: " + radi + "m", position, radi);
		addMapMarker(currentMarker);

		circlePerimeter.setCenter(new Point(position.getLat(), position.getLon()));
		circlePerimeter.setRadius(radi * 1.0);
	}

    public CirclePerimeter getCirclePerimeter() {
        return this.circlePerimeter;
    }

	public void setCirclePerimeter(CirclePerimeter circlePerimeter) {
		this.circlePerimeter = circlePerimeter;
	}
}
