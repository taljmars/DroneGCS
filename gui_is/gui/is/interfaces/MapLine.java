// License: GPL. For details, see Readme.txt file.
package gui.is.interfaces;

import gui.core.mapObjects.Coordinate;

import java.awt.Graphics;
import java.awt.Point;

/**
 * Interface to be implemented by polygons that can be displayed on the map.
 *
 * @author Vincent Privat
 */
public interface MapLine extends MapObject {

    /**
     * @return Latitude/Longitude of each point of polygon
     */
    Coordinate getStart();
    
    
    Coordinate getEnd();

    /**
     * Paints the map polygon on the map. The <code>polygon</code>
     * is specifying the coordinates within <code>g</code>
     *
     * @param g graphics
     * @param polygon polygon to draw
     */
    void paint(Graphics g, Point start, Point end);


	double getBearing();
    
    //void paint(Graphics g, Point start, Point end, double rotate_in_deg);
}
