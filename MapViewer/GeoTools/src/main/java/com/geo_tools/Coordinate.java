// License: GPL. For details, see Readme.txt file.
package com.geo_tools;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

/**
 * This class encapsulates a Point2D.Double and provide access
 * via <tt>lat</tt> and <tt>lon</tt>.
 *
 * @author Jan Peter Stotz
 *
 */
public class Coordinate {
    
	private transient Point2D.Double data;
	private double altitude;// in meters

    public Coordinate(double lat, double lon) {
        data = new Point2D.Double(lon, lat);
    }
    
    public Coordinate(double lat, double lon, double alt) {
        data = new Point2D.Double(lon, lat);
        altitude = alt;
    }
    
    public Coordinate(Coordinate coord) {
        data = new Point2D.Double(coord.getLon(), coord.getLat());
    }
    
    public Coordinate(Coordinate coord, double alt) {
        this(coord.getLat(), coord.getLon());
        altitude = alt;
    }
    
    public double getY() {
		return data.y;
	}
    
    public double getX() {
		return data.x;
	}

    public synchronized double getLat() {
        return data.y;
    }

    public synchronized void setLat(double lat) {
        data.y = lat;
    }

    public synchronized double getLon() {
        return data.x;
    }

    public synchronized void setLon(double lon) {
        data.x = lon;
    }
    
    public double getAltitude() {
		return altitude;
	}
    
    public void setAltitude(double alt) {
    	altitude = alt;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(data.x);
        out.writeObject(data.y);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        data = new Point2D.Double();
        data.x = (Double) in.readObject();
        data.y = (Double) in.readObject();
    }

    @Override
    public String toString() {
        return "Coordinate[" + data.y + ", " + data.x + ']';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coordinate other = (Coordinate) obj;
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

	public void set(double x, double y) {
		data.x = x;
		data.y = y;
	}
	
	public void set(double x, double y, double z) {
		data.x = x;
		data.y = y;
		altitude = z;
	}
	
	public Coordinate dot(double scalar) {
		return new Coordinate(getLat() * scalar, getLon() * scalar, altitude);
	}
	
	public Coordinate sum(Coordinate coord) {
		return new Coordinate(getLat() + coord.getLat(), getLon() + coord.getLon());
	}
	
	public Coordinate subtract(Coordinate coord) {
		return new Coordinate(getLat() - coord.getLat(), getLon() - coord.getLon());
	}
	
	public static Coordinate sum(Coordinate... toBeAdded) {
		double latitude = 0;
		double longitude = 0;
		for (Coordinate coord : toBeAdded) {
			latitude += coord.getLat();
			longitude += coord.getLon();
		}
		return new Coordinate(latitude, longitude);
	}
	
	public Coordinate negate() {
		return new Coordinate(getLat() * -1, getLon() * -1);
	}
    
}
