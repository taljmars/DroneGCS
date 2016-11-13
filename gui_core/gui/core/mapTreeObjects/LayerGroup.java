package gui.core.mapTreeObjects;

import java.util.Vector;

public class LayerGroup extends Layer {

	private Vector<Layer> childrens;
	
	public LayerGroup(String name) {
		super(name);
		childrens = new Vector<>();
	}
    
    public void addChildren(Layer layer) {
    	layer.setParent(this);
    	this.childrens.addElement(layer);    	
    }
    
    public Vector<Layer> getChildens() {
    	return childrens;
    }

	public void removeChildren(Layer layer) {
		childrens.remove(layer);
		layer.setParent(null);
	}

	@Override
	public void regenerateMapObjects() {
		for (Layer child : childrens)
			child.regenerateMapObjects();
	}

	@Override
	public void removeAllMapObjects() {
		for (Layer child : childrens)
			child.removeAllMapObjects();
	}
	
	
}
