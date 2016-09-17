// License: GPL. For details, see Readme.txt file.
package gui.jmapviewer.impl;

import gui.jmapviewer.JMapViewer;
import gui.jmapviewer.Style;
import gui.jmapviewer.interfaces.ICoordinate;
import gui.jmapviewer.interfaces.MapObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class LayerPerimeter extends Layer implements Serializable /*TALMA add serilizebae*/{    
    /**
	 * 
	 */
	private static final long serialVersionUID = -530213044144076365L;	
	private ArrayList<ICoordinate> points = new ArrayList<ICoordinate>();

    public LayerPerimeter(String name) {
        super(name);
    }

    public LayerPerimeter(String name, String description) {
        super(name, description);
    }

    public LayerPerimeter(String name, Style style) {
        super(name, style);
    }

    public LayerPerimeter(String name, String description, Style style) {
        super(name, description, style);
    }

    public LayerPerimeter(LayerGroup parent, String name) {
        super(parent, name);
    }

    public LayerPerimeter(LayerGroup parent, String name, Style style) {
        super(parent, name, style);
    }

    public LayerPerimeter(LayerGroup parent, String name, String description, Style style) {
        super(parent, name, description, style);
    }
    
    public LayerPerimeter(LayerPerimeter layer) {
    	super(layer);
    	points = new ArrayList<ICoordinate>();
    	Iterator<ICoordinate> it = layer.points.iterator();
    	while (it.hasNext()) {
    		points.add(it.next());
    	}
    }

    public LayerPerimeter add(MapObject element) {
    	super.add(element);
        return this;
    }
    
    public void unloadFromMap(JMapViewer map) {
    	super.unloadFromMap(map);
    }

	public void loadToMap(JMapViewer map) {
		super.loadToMap(map);
	}
	
	public void repaintPerimeter(JMapViewer map) {
		ArrayList<MapObject> arr = new ArrayList<MapObject>();
		arr.add(new MapPolygonImpl(this, points));
		setElements(arr);
	}
	
	// TALMA
	public void repaint(JMapViewer map) {
		unloadFromMap(map);
		repaintPerimeter(map);
		loadToMap(map);
	}
	
	@Override
	public void initialize() {
		super.initialize();
	}

	public void add(ICoordinate position) {
		points.add(position);
	}
}
