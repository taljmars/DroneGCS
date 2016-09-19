// License: GPL. For details, see Readme.txt file.
package gui.core.mapObjects;

import gui.is.classes.MyStroke;
import gui.is.classes.Style;
import gui.is.interfaces.ICoordinate;
import gui.is.interfaces.MapPath;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MapPathImpl extends MapObjectImpl implements MapPath, Serializable /*TALMA add serilizebae*/ {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3648216074195539526L;
	//
	private ArrayList<ICoordinate> points;

	public MapPathImpl(ICoordinate ... points) {
	    this(null, null, points);
	}

    public MapPathImpl(ArrayList<ICoordinate> points) {
        this(null, null, points);
    }

    public MapPathImpl(String name, ArrayList<ICoordinate> points) {
        this(null, name, points);
    }

    public MapPathImpl(String name, ICoordinate ... points) {
        this(null, name, points);
    }

    public MapPathImpl(Layer layer, ArrayList<ICoordinate> points) {
        this(layer, null, points);
    }

    public MapPathImpl(Layer layer, String name, ArrayList<ICoordinate> points) {
        this(layer, name, points, getDefaultStyle());
    }

    public MapPathImpl(Layer layer, String name, ICoordinate ... points) {
    	this(layer, name, new ArrayList<ICoordinate>(Arrays.asList(points)) , getDefaultStyle());
    }

    public MapPathImpl(Layer layer, String name, ArrayList<ICoordinate> points, Style style) {
        super(layer, name, style);
        this.points = points;
    }


	public MapPathImpl(Layer layer, MapPathImpl mo) {
		super(layer, mo);
		Iterator<ICoordinate> it = mo.points.iterator();
		points = new ArrayList<ICoordinate>();
		while (it.hasNext()) {
			this.points.add(it.next());
		}
	}

	public void AddPoint(ICoordinate pt){
    	this.points.add(pt);
    }

    @Override
    public List<? extends ICoordinate> getPoints() {
        return this.points;
    }

    @Override
    public void paint(Graphics g, List<Point> points) {
        Path2D.Double path = new Path2D.Double();
        int i = 0;
        for (Point p : points) {
        	if (i == 0)
        		path.moveTo(p.x, p.y);
        	else
        		path.lineTo(p.x, p.y);
        	
        	i++;
        }
        
        paint(g, path);
    }

    @Override
    public void paint(Graphics g, Path2D path) {
        // Prepare graphics
        Color oldColor = g.getColor();
        g.setColor(getColor());

        Stroke oldStroke = null;
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            oldStroke = g2.getStroke();
            // TALMA
            if (getStroke() != null)
            	g2.setStroke(getStroke());
            g2.draw(path);
        }

        // Restore graphics
        g.setColor(oldColor);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(oldStroke);
        }
    }

    public static Style getDefaultStyle() {
        return new Style(Color.BLUE, new Color(100, 100, 100, 50), new MyStroke(2), getDefaultFont());
    }

    @Override
    public String toString() {
        return "MapPath [points=" + points + ']';
    }
}
