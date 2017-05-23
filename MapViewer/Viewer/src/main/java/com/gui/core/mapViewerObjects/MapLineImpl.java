//==============================================================================
//   JFXMapPane is a Java library for parsing raw weather data
//   Copyright (C) 2012 Jeffrey L Smith
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//    
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//    
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//    
//  For more information, please email jsmith.carlsbad@gmail.com
//    
//==============================================================================
package com.gui.core.mapViewerObjects;

import com.geo_tools.Coordinate;
import com.gui.core.mapViewer.ViewMap;
import com.gui.is.interfaces.mapObjects.MapLine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author smithjel
 */
public class MapLineImpl implements MapLine {
    
    String Name;
    Color color;
    double dashOffset;
    double width;
    ObservableList<Coordinate> Coordinates;
    
    public MapLineImpl() {
    	this("",Color.BLUE,1,1,new ArrayList<>());
    }
    
    public MapLineImpl(List<Coordinate> coords) {
    	this("",Color.BLUE,1,1,coords);
    }
    

    public MapLineImpl(String n, Color c, double width, double dashOffset, List<Coordinate> coords) {
        super();
        this.Name = n;
        this.color = c;
        this.dashOffset = dashOffset;
        this.width = width;
        this.Coordinates = FXCollections.observableList(coords);
    }
 
    

    public MapLineImpl(MapLineImpl mapLineImpl) {
		this.Name = mapLineImpl.Name;
		this.color = mapLineImpl.color;
		this.dashOffset = mapLineImpl.dashOffset;
		this.width = mapLineImpl.width;
		List<Coordinate> coords = new ArrayList<>();
		for (Coordinate coord : mapLineImpl.Coordinates) 
			coords.add(new Coordinate(coord.getLat(), coord.getLon()));
		this.Coordinates = FXCollections.observableList(coords);
	}

	@Override
    public void addCoordinate(Coordinate coordinate) {
        this.Coordinates.add(coordinate);
    }


    public void Render(ViewMap viewer, Group g) {
        Polyline polyline = new Polyline();
        
        polyline.setStrokeType(StrokeType.CENTERED);
        polyline.setStrokeDashOffset(this.dashOffset);
        polyline.setStroke( this.color );
        polyline.setStrokeWidth( this.width );
        polyline.setStrokeLineCap(StrokeLineCap.ROUND);        
        polyline.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        
        DropShadow dropShadow = new DropShadow();
        dropShadow.setOffsetX(10);
        dropShadow.setOffsetY(10);
        dropShadow.setColor(Color.rgb(50, 50, 50, 0.7));
        polyline.setEffect(dropShadow);
        
        for (Coordinate coordinate : this.Coordinates) {
            Point p = viewer.getMapPosition(coordinate.getLat(), coordinate.getLon(),false);
            polyline.getPoints().add((double)p.x);
            polyline.getPoints().add((double)p.y);
        }
        g.getChildren().add(polyline);
    }


    @Override
    public String toString() {
        return "MapLine" + Name;
    }

	@Override
	public double getBearing() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Coordinate> getCoordinates() {
		return this.Coordinates;
	}

    public MapLine clone() {
    	return new MapLineImpl(this);
    }
    
}
