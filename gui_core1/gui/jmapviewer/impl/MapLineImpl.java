// License: GPL. For details, see Readme.txt file.
package gui.jmapviewer.impl;

import gui.jmapviewer.MyStroke;
import gui.jmapviewer.Style;
import gui.jmapviewer.interfaces.MapLine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.io.Serializable;

import mavlink.is.utils.geoTools.GeoTools;

public class MapLineImpl extends MapObjectImpl implements MapLine ,Serializable /*TALMA add serilizebae*/ {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 2171913843995287441L;
	private Coordinate start = null;
    private Coordinate end = null;
	private double bearing = -1;

    public MapLineImpl(Coordinate start, Coordinate end) {
        this(null, null, start, end, -1);
    }
    
    public MapLineImpl(Coordinate start, Coordinate end, double bearing) {
        this(null, null, start, end, bearing);
    }

    public MapLineImpl(String name, Coordinate start, Coordinate end) {
        this(null, name, start, end, -1);
    }

    public MapLineImpl(Layer layer, Coordinate start, Coordinate end) {
        this(layer, null, start, end, -1);
    }

    public MapLineImpl(Layer layer, String name, Coordinate start, Coordinate end, double bearing) {
        this(layer, name, start, end, getDefaultStyle(), -1);
    }

    public MapLineImpl(Layer layer, String name, Coordinate start, Coordinate end, Style style, double bearing) {
        super(layer, name, style);
        
        this.start = start;
        this.end = end;
        if (bearing == -1)
        	this.bearing = GeoTools.getHeadingFromCoordinates(start, end);
        else
        	this.bearing = bearing;
    }
    
    public MapLineImpl(Layer layer, MapLineImpl mo) {
    	super(layer, mo);
    	this.start = mo.start;
    	this.end = mo.end;
    	this.bearing = mo.bearing;
	}

	@Override
    public void paint(Graphics g, Point start, Point end) {
    	// Prepare graphics
        Color oldColor = g.getColor();
        g.setColor(getColor());
        
    	if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(getStroke());
            
            Line2D line = new Line2D.Float(start, end);
    	
            if (bearing >= 0) {
            	AffineTransform at = AffineTransform.getRotateInstance(
    				Math.toRadians(bearing), 
    				line.getX1(), 
    				line.getY1());

    			//Draw the rotated line
    			g2.draw(at.createTransformedShape(line));
            }
            else {
            	g2.draw(line);
            }
    		
    		if (getBackColor() != null) {
                Composite oldComposite = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                g2.setPaint(getBackColor());
                g2.setComposite(oldComposite);
            }
            // Restore graphics
            g.setColor(oldColor);
            if (g instanceof Graphics2D) {
                ((Graphics2D) g).setStroke(oldStroke);
            }
            Rectangle rec = line.getBounds();
            Point corner = rec.getLocation();
            Point p = new Point(corner.x+(rec.width/2), corner.y+(rec.height/2));
            if (getLayer() == null || getLayer().isVisibleTexts()) paintText(g, p);
    	}
    }

    public static Style getDefaultStyle() {
        return new Style(Color.RED, new Color(100, 100, 100, 50), new MyStroke(2), getDefaultFont());
    }

    @Override
    public String toString() {
        return "MapLine [start=" + start + " end=" + end + ']';
    }

    @Override
	public Coordinate getStart() {
		return start;
	}

    @Override
	public Coordinate getEnd() {
		return end;
	}

	@Override
	public double getBearing() {
		return bearing;
	}
}
