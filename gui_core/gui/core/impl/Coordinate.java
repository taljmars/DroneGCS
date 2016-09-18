// License: GPL. For details, see Readme.txt file.
package gui.core.impl;

import gui.is.interfaces.ICoordinate;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import mavlink.is.utils.coordinates.Coord2D;

/**
 * This class encapsulates a Point2D.Double and provide access
 * via <tt>lat</tt> and <tt>lon</tt>.
 *
 * @author Jan Peter Stotz
 *
 */
public class Coordinate implements ICoordinate ,Serializable /*TALMA add serilizebae*/ {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3470284987884728225L;
	private transient Point2D.Double data;

    public Coordinate(double lat, double lon) {
        data = new Point2D.Double(lon, lat);
    }

    @Override
    public double getLat() {
        return data.y;
    }

    @Override
    public void setLat(double lat) {
        data.y = lat;
    }

    @Override
    public double getLon() {
        return data.x;
    }

    @Override
    public void setLon(double lon) {
        data.x = lon;
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
    
    public Coord2D ConvertToCoord2D() {
    	return new Coord2D(getLat(), getLon());
    	
    }
}
