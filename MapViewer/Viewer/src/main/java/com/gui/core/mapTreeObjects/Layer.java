package com.gui.core.mapTreeObjects;

public abstract class Layer {

	private Layer parent;
	private String name;
	
	public Layer(String name) {
		this.name = name;
	}
	
	public Layer(Layer layer) {
		this.parent = layer.parent;
		this.name = layer.name;
	}
	
	public Layer getParent() {
        return parent;
    }

    public void setParent(Layer parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
	
	public String toString() {
		return name;
	}
	
	public abstract void regenerateMapObjects();

	public abstract void removeAllMapObjects();
}
