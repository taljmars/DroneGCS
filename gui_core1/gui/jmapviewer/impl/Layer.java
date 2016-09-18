// License: GPL. For details, see Readme.txt file.
package gui.jmapviewer.impl;

import gui.jmapviewer.JMapViewer;
import gui.jmapviewer.Style;
import gui.jmapviewer.interfaces.AbstractLayer;
import gui.jmapviewer.interfaces.MapObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public class Layer extends AbstractLayer implements Serializable /*TALMA add serilizebae*/ {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4875679178196577828L;
	private List<MapObject> elements;

    public Layer(String name) {
        super(name);
    }

    public Layer(String name, String description) {
        super(name, description);
    }

    public Layer(String name, Style style) {
        super(name, style);
    }

    public Layer(String name, String description, Style style) {
        super(name, description, style);
    }

    public Layer(LayerGroup parent, String name) {
        super(parent, name);
    }

    public Layer(LayerGroup parent, String name, Style style) {
        super(parent, name, style);
    }

    public Layer(LayerGroup parent, String name, String description, Style style) {
        super(parent, name, description, style);
    }
    
    public Layer(Layer layer) {
    	super(layer);
    	
    	if (layer.getElements() != null) {
    		Iterator<MapObject> it = layer.getElements().iterator();
    		while (it.hasNext()) {
    			MapObject mo = it.next();
    			if (mo instanceof MapMarkerCircle) {
    				this.add(new MapMarkerCircle(null, (MapMarkerCircle)mo));
    				continue;
    			}
    			
    			if (mo instanceof MapMarkerDot) {
    				this.add(new MapMarkerDot(null, (MapMarkerDot)mo));
    				continue;
    			}
    			
    			if (mo instanceof MapPolygonImpl) {
    				this.add(new MapPolygonImpl(null, (MapPolygonImpl) mo));
    				continue;
    			}
    			
    			if (mo instanceof MapPathImpl) {
    				this.add(new MapPathImpl(null, (MapPathImpl)mo));
    				continue;
    			}
    			
    			if (mo instanceof MapLineImpl) {
    				this.add(new MapLineImpl(null, (MapLineImpl)mo));
    				continue;
    			}
    			
    			System.out.println(getClass().getName() + " Failed to find MapObject type");
    		}
    	}
    }

	public List<MapObject> getElements() {
        return elements;
    }

    public void setElements(List<MapObject> elements) {
        this.elements = elements;
    }

    public Layer add(MapObject element) {
    	if (element == null)
    		return this;
    	
        element.setLayer(this);
        elements = add(elements, element);
        return this;
    }
    
    public void unloadFromMap(JMapViewer map) {
    	if (elements != null) {
    		Iterator<MapObject> it = elements.iterator();
    		while (it.hasNext()) {
    			MapObject m = it.next();
    			map.removeMapObject(m);
    			it.remove();
    		}
		}
    }

	public void loadToMap(JMapViewer map) {
		if (elements != null) {
    		Iterator<MapObject> it = elements.iterator();
    		while (it.hasNext()) {
    			map.addMapObject(it.next());
    		}
		}
	}
	
	// TALMA
	public void repaint(JMapViewer map) {
		unloadFromMap(map);
		loadToMap(map);
	}

	public void initialize() {
		if (elements != null) {
    		Iterator<MapObject> it = elements.iterator();
    		while (it.hasNext()) {
    			it.next().setLayer(this);
    		}
		}
	}
}
