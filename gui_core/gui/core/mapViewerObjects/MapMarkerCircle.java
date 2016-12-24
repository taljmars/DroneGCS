package gui.core.mapViewerObjects;

import java.awt.Point;

import gui.is.Coordinate;
import gui.is.interfaces.mapObjects.MapMarker;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.geoTools.GeoTools;

/**
 * A simple implementation of the {@link MapMarker} interface. Each map marker
 * is painted as a circle with a black border line and filled with a specified
 * color.
 *
 * @author taljmars
 *
 */
public class MapMarkerCircle implements MapMarker {

	private String name;
	private double lat;
	private double lon;
	private Color color;
	private Circle sphere;
	private double radius;
    
	/**
	 * 
	 * @param string name
	 * @param iCoord position
	 * @param radius in meters
	 */
    public MapMarkerCircle(String string, Coordinate iCoord, double radius) {
		this(iCoord, radius);
	}
    
    /**
     * 
     * @param iCoordinate position
     * @param radius in meters
     */
    public MapMarkerCircle(Coordinate iCoordinate, double radius) {
        this(iCoordinate.getLat(), iCoordinate.getLon(), radius);
    }
    
    /**
     * 
     * @param lat
     * @param lon
     * @param radius in meters
     */
    public MapMarkerCircle(double lat, double lon, double radius) {
        this(lat, lon, radius, Color.TRANSPARENT);
    }
    
	public MapMarkerCircle(MapMarkerCircle mapMarkerCircle) {
		this.name = mapMarkerCircle.name;
		this.lat = mapMarkerCircle.lat;
		this.lon = mapMarkerCircle.lon;
		this.color = mapMarkerCircle.color;
		this.sphere = mapMarkerCircle.sphere;
		this.radius = mapMarkerCircle.radius;
	}

	/**
	 * 
	 * @param lat
	 * @param lon
	 * @param radius in meters
	 * @param color
	 */
    private MapMarkerCircle(double lat, double lon, double radius, Color color) {
        super();
        this.name = "";
        this.color = color;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;

        this.sphere = new Circle(radius);
        this.sphere.setStroke(Color.ORANGE);
        this.sphere.setStrokeWidth(1);
        this.sphere.setStrokeType(StrokeType.OUTSIDE);
        this.sphere.setFill(Color.TRANSPARENT);        
    }

	public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void Render(Group g, Point position, Double radius) {
        this.sphere.setTranslateX(position.x);
        this.sphere.setTranslateY(position.y);
        this.sphere.setRadius(radius);
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
	public MapMarkerCircle clone() {
		return new MapMarkerCircle(this);
	}

	@Override
	public double getRadius() {
		return radius;
	}

	public boolean contains(Coordinate coord) {
		double dist = GeoTools.getDistance(coord.ConvertToCoord2D(), new Coord2D(lat, lon)).valueInMeters();
		if (dist < radius) 
			return true;
		
		return false;
	}
}
