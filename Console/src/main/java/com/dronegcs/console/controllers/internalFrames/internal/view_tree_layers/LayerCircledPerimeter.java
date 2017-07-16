package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.dronedb.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.gui.core.mapViewer.LayeredViewMap;
import com.gui.core.mapViewerObjects.MapMarkerCircle;
import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;

public class LayerCircledPerimeter extends LayerPerimeter {
	
	private MapMarkerCircle currentMarker;

	private CirclePerimeter circlePerimeter;

	public LayerCircledPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}
	
	public LayerCircledPerimeter(CirclePerimeter circlePerimeter, LayeredViewMap viewMap) {
		super(circlePerimeter.getName(), viewMap);
		this.circlePerimeter = circlePerimeter;
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
        return this.circlePerimeter;
    }

	public void setCirclePerimeter(CirclePerimeter circlePerimeter) {
		this.circlePerimeter = circlePerimeter;
	}
}
