package main.java.gui_controllers.controllers.internalPanels.internal;

public enum MissionItemTableColumn {
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
	
	MissionItemTableColumn (String name){
		this.name = name;
	}

	public int getNumber() {
		return this.ordinal();
	}

	public String getName() {
		return name;
	}
};