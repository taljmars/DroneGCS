package com.dronegcs.console.controllers.internalFrames.internal.MavlinkParameters;

public enum ParamsTableColumn {
	Id ("Id"),
	Name ("Name"),
	Value ("Value"),
	Type ("Type"),
	Description ("Description");
	
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