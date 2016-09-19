// License: GPL. For details, see Readme.txt file.
package gui.core.mapObjects;

import gui.is.classes.MyStroke;
import gui.is.classes.Style;
import gui.is.interfaces.ICoordinate;
import gui.is.interfaces.MapPolygon;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MapPolygonImpl extends MapObjectImpl implements MapPolygon, Serializable /*TALMA add serilizebae*/ {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1922036423834067349L;
	//private List<? extends ICoordinate> points;
	private ArrayList<ICoordinate> points;
	private Polygon poly = null;

    public MapPolygonImpl(ICoordinate ... points) {
        this(null, null, points);
    }

    //public MapPolygonImpl(List<? extends ICoordinate> points) {
    public MapPolygonImpl(ArrayList<ICoordinate> points) {
        this(null, null, points);
    }

    //public MapPolygonImpl(String name, List<? extends ICoordinate> points) {
    public MapPolygonImpl(String name, ArrayList<ICoordinate> points) {
        this(null, name, points);
    }

    public MapPolygonImpl(String name, ICoordinate ... points) {
        this(null, name, points);
    }

    //public MapPolygonImpl(Layer layer, List<? extends ICoordinate> points) {
    public MapPolygonImpl(Layer layer, ArrayList<ICoordinate> points) {
        this(layer, null, points);
    }

    //public MapPolygonImpl(Layer layer, String name, List<? extends ICoordinate> points) {
    public MapPolygonImpl(Layer layer, String name, ArrayList<ICoordinate> points) {
        this(layer, name, points, getDefaultStyle());
    }

    public MapPolygonImpl(Layer layer, String name, ICoordinate ... points) {
        //this(layer, name, Arrays.asList(points), getDefaultStyle());
    	this(layer, name, new ArrayList<ICoordinate>(Arrays.asList(points)) , getDefaultStyle());
    }

    //public MapPolygonImpl(Layer layer, String name, List<? extends ICoordinate> points, Style style) {
    public MapPolygonImpl(Layer layer, String name, ArrayList<ICoordinate> points, Style style) {
        super(layer, name, style);
        this.points = points;
    }


	public MapPolygonImpl(Layer layer, MapPolygonImpl mo) {
		super(layer, mo);
		this.poly = mo.poly;
		this.points = new ArrayList<ICoordinate>();
		Iterator<ICoordinate> it = mo.points.iterator();
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
        Polygon polygon = new Polygon();
        for (Point p : points) {
            polygon.addPoint(p.x, p.y);
        }
        paint(g, polygon);
    }

    @Override
    public void paint(Graphics g, Polygon polygon) {
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
        }
        // Draw
        g.drawPolygon(polygon);
        if (g instanceof Graphics2D && getBackColor() != null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(getBackColor());
            g2.fillPolygon(polygon);
            g2.setComposite(oldComposite);
        }
        // Restore graphics
        g.setColor(oldColor);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(oldStroke);
        }
        Rectangle rec = polygon.getBounds();
        Point corner = rec.getLocation();
        Point p = new Point(corner.x+(rec.width/2), corner.y+(rec.height/2));
        if (getLayer() == null || getLayer().isVisibleTexts()) paintText(g, p);
        
        poly = polygon;
    }

    public static Style getDefaultStyle() {
        return new Style(Color.BLUE, new Color(100, 100, 100, 50), new MyStroke(2), getDefaultFont());
    }

    @Override
    public String toString() {
        return "MapPolygon [points=" + points + ']';
    }
	
    @Override
	public boolean contains(ICoordinate p) {
		int i;
		int j;
		boolean result = false;
		for (i = 0, j = points.size() - 1; i < points.size() ; j = i++) {
			if ((points.get(i).getLat() > p.getLat()) != (points.get(j).getLat() > p.getLat()) &&
				(p.getLon() < (points.get(j).getLon() - points.get(i).getLon()) * (p.getLat() - points.get(i).getLat()) / (points.get(j).getLat()-points.get(i).getLat()) + points.get(i).getLon())) {
				result = !result;
			}
		}
		return result;
	}
}
