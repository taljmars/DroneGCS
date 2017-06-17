package com.dronegcs.console.controllers.internalFrames.internal;

public enum ParamsTableColumn {
	Id ("Id"),
	Name ("Name"),
	Value ("Value"),
	Type ("Type");
	
    private final String name;
	
	ParamsTableColumn(String name){
		this.name = name;
	}

	public int getNumber() {
		return this.ordinal();
	}

	public String getName() {
		return name;
	}
};