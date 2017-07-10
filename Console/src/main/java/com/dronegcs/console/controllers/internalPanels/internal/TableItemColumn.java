package com.dronegcs.console.controllers.internalPanels.internal;

public enum TableItemColumn {
	Order ("Order"), 
	Type ("Type"),
	Lat ("Lat"),
	Lon ("Lon"),
	Height ("Height"),
	Delay ("Delay"),
	Radius ("Radius"),
	SetUp ("SetUp"),
	SetDown ("SetDown"),
	Remove ("Remove"),
	PTR ("PTR");
	
    private final String name;
	
	TableItemColumn(String name){
		this.name = name;
	}

	public int getNumber() {
		return this.ordinal();
	}

	public String getName() {
		return name;
	}
};