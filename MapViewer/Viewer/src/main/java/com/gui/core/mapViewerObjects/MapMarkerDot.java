package com.gui.core.mapViewerObjects;

import com.geo_tools.Coordinate;
import com.gui.is.interfaces.mapObjects.MapMarker;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.RadialGradientBuilder;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;

import java.awt.*;

/**
 * A simple implementation of the {@link MapMarker} interface. Each map marker
 * is painted as a circle with a black border line and filled with a specified
 * color.
 *
 * @author Jan Peter Stotz
 *
 */
@SuppressWarnings("deprecation")
public class MapMarkerDot implements MapMarker {

	private final static int DEF_RADIUS = 10;
	
    private double lat;
    private double lon;
    private Color color;
    private Circle sphere;
    private int radius;
    
    public MapMarkerDot(Color color, Coordinate coordinate) {
        this(DEF_RADIUS, color, coordinate.getLat(), coordinate.getLon());
    }
    
    public MapMarkerDot(Coordinate coordinate) {
        this(coordinate.getLat(), coordinate.getLon());
    }
    
    public MapMarkerDot(MapMarkerDot mapMarkerDot) {
		this.lat = mapMarkerDot.lat;
		this.lon = mapMarkerDot.lon;
		this.color = mapMarkerDot.color;
		this.sphere = mapMarkerDot.sphere;
		this.radius = mapMarkerDot.radius;
	}
    
    public MapMarkerDot(double lat, double lon) {
        this( DEF_RADIUS, Color.YELLOW, lat, lon);
    }

    private MapMarkerDot(int radius, Color color, double lat, double lon) {
        super();
        this.color = color;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        
        this.sphere = CircleBuilder.create()
                .centerX(radius)
                .centerY(radius)
                .radius(radius)
                .cache(true)
                .build();        

        RadialGradient rgrad = RadialGradientBuilder.create()
                    .centerX(sphere.getCenterX() - sphere.getRadius() / 3)
                    .centerY(sphere.getCenterY() - sphere.getRadius() / 3)
                    .radius(sphere.getRadius())
                    .proportional(false)
                    .stops(new Stop(0.0, color), new Stop(1.0, Color.BLACK))
                    .build();
        
        this.sphere.setFill(rgrad);        
    }

	public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void Render(Group g, Point position, Double radius) {
        this.sphere.setTranslateX(position.x - this.radius);
        this.sphere.setTranslateY(position.y - this.radius);
        g.getChildren().add(this.sphere);
    }

    @Override
    public String toString() {
        return "MapMarker at " + lat + " " + lon;
    }

	public void setColor(Color clr) {
		color = clr;
	}

	@Override
	public Coordinate getCoordinate() {
		return new Coordinate(lat, lon);
	}
	
	@Override
	public MapMarkerDot clone() {
		return new MapMarkerDot(this);
	}

	public Color getColor() {
		return color;
	}

	@Override
	public double getRadius() {
		return radius;
	}
}
