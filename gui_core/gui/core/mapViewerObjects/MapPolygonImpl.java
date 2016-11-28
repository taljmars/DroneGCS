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
package gui.core.mapViewerObjects;


import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import gui.core.mapViewer.ViewMap;
import gui.is.Coordinate;
import gui.is.interfaces.mapObjects.MapPolygon;
import javafx.scene.Group;
import javafx.scene.effect.Bloom;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import mavlink.is.utils.geoTools.GeoTools;

/**
 *
 * @author smithjel
 */
public class MapPolygonImpl implements MapPolygon {
    
    private final List<Coordinate> coordinates;
    private Color FillColor;
    private Color OutlineColor;
    
    public MapPolygonImpl() {
        this(Color.YELLOW, Color.TRANSPARENT, new ArrayList<>() );
    }
    
    public MapPolygonImpl( List<Coordinate> points ) {
        this(Color.YELLOW, Color.TRANSPARENT, points );
    }

    public MapPolygonImpl(Color fcolor, Color ocolor, List<Coordinate> coords ) {
        super();
        this.FillColor      = fcolor;
        this.OutlineColor   = ocolor;
        this.coordinates    = coords;
    }

    public MapPolygonImpl(MapPolygonImpl mapPolygonImpl) {
    	this.coordinates = new ArrayList<>();
    	for (Coordinate coord : coordinates) this.coordinates.add(new Coordinate(coord.getLat(), coord.getLon()));
		this.FillColor = mapPolygonImpl.FillColor;
		this.OutlineColor = mapPolygonImpl.OutlineColor;
	}

	@Override
    public List<Coordinate> getCoordinates() {
        return this.coordinates;
    }

    @Override
    public Color getOutlineColor() {
        return this.OutlineColor;
    }

    @Override
    public Color getFillColor() {
        return this.FillColor;
    }

    @Override
    public void Render( ViewMap viewer, Group g ) {
        int nPoints = this.coordinates.size();
        Path path = new Path();
        Polyline polyline = new Polyline();
        Coordinate c0 = this.coordinates.get(0);
        Point p0 = viewer.getMapPoint( c0 );
        path.getElements().add( new MoveTo( p0.x, p0.y ) );
        for (int p = 0; p < nPoints; p++ ) {
        	Coordinate coordinate = this.coordinates.get(p);
            Point point = viewer.getMapPoint( coordinate );
            path.getElements().add(new LineTo(point.x, point.y));
            polyline.getPoints().add((double)point.x);
            polyline.getPoints().add((double)point.y);
        }
        path.getElements().add( new LineTo(p0.x, p0.y) );
        
        polyline.getPoints().add((double)p0.x);
        polyline.getPoints().add((double)p0.y);

        path.setStrokeWidth(5);
        path.setStroke( this.FillColor );

        Bloom bloom = new Bloom();
        bloom.setThreshold(0.0);
        
//        InnerShadow is = new InnerShadow();
//        is.setOffsetX(0.0f);
//        is.setOffsetY(0.0f);
//        is.setWidth(30.0);
//        is.setColor(this.OutlineColor);
        path.setEffect(bloom);
        
        //path.setStroke( this.OutlineColor );
        //path.setFill( this.FillColor ); 
        g.getChildren().add(path);
        
        polyline.setFill( this.OutlineColor );
        

//        DropShadow ds = new DropShadow();
//        ds.setOffsetY(10.0f);
//        ds.setOffsetX(10.0f);
//        ds.setColor( this.OutlineColor ); // Color.color(0.4f, 0.4f, 0.4f));
//        polyline.setEffect(ds);
        
        g.getChildren().add(polyline);
        
    }

	@Override
	public void addCoordinate(Coordinate coordinate) {
		coordinates.add(coordinate);
	}
	
	@Override
	public MapPolygon clone() {
		return new MapPolygonImpl(this);
	}
	
	@Override
	public boolean contains(Coordinate coordinate) {
		int i;
		int j;
		boolean result = false;
		for (i = 0, j = coordinates.size() - 1; i < coordinates.size() ; j = i++) {
			if ((coordinates.get(i).getLat() > coordinate.getLat()) != (coordinates.get(j).getLat() > coordinate.getLat()) &&
				(coordinate.getLon() < (coordinates.get(j).getLon() - coordinates.get(i).getLon()) * (coordinate.getLat() - coordinates.get(i).getLat()) / (coordinates.get(j).getLat()-coordinates.get(i).getLat()) + coordinates.get(i).getLon())) {
				result = !result;
			}
		}
		return result;
	}
	
	@Override
	public Coordinate getClosestPointOnEdge(Coordinate coord) {
		Coordinate closestPoint = new Coordinate(0,0);
		double min_dist = Integer.MAX_VALUE;
		
		List<? extends Coordinate> lst = getCoordinates();
		for (int i = 0 ; i < lst.size() ; i++) {
			Coordinate corner = lst.get(i);
			
			double val = GeoTools.getDistance(coord.ConvertToCoord2D(), corner.ConvertToCoord2D()).valueInMeters();
			if (val < min_dist) {
				min_dist = val;
				closestPoint = corner;
			}
		}
		
		return closestPoint;
	}
    
}
