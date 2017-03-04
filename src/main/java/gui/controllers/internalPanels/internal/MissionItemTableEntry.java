package main.java.gui_controllers.controllers.internalPanels.internal;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import main.java.is.mavlink.drone.mission.MissionItem;
import main.java.is.mavlink.drone.mission.MissionItemType;

public class MissionItemTableEntry {
	
    private final SimpleIntegerProperty order;
    private final SimpleStringProperty type;
    private final SimpleDoubleProperty lat, lon, height, delay, radius;
    private final SimpleObjectProperty<MissionItem> missionItem;
 
    public MissionItemTableEntry(	Integer pOrder, 
    				MissionItemType missionItemType, 
    				Double pLat, Double pLon, Double pHeight, Double pDelay, Double pRadius,
    				MissionItem pMissionItem) {
    	
        this.order = new SimpleIntegerProperty(pOrder);
        this.type = new SimpleStringProperty(missionItemType.getName());
        this.lat = new SimpleDoubleProperty(pLat);
        this.lon = new SimpleDoubleProperty(pLon);
        this.height = new SimpleDoubleProperty(pHeight);
        this.delay = new SimpleDoubleProperty(pDelay);
        this.radius = new SimpleDoubleProperty(pRadius);
        this.missionItem = new SimpleObjectProperty<MissionItem>(pMissionItem);
    }


	public Integer getOrder() {return this.order.get();}
    public void setOrder(Integer pOrder) {this.order.set(pOrder);}
        
    public String getType() {return this.type.get();}
    public void setType(String pType) {this.type.set(pType);}
    
    public Double getLat() {return this.lat.get();}
    public void setLat(Double pLat) {this.lat.set(pLat);}
    
    public Double getLon() {return this.lon.get();}
    public void setLon(Double pLon) {this.lon.set(pLon);}
    
    public Double getHeight() {return this.height.get();}
    public void setHeight(Double pHeight) {this.height.set(pHeight);}
    
    public Double getDelay() {return this.delay.get();}
    public void setDelay(Double pDelay) {this.delay.set(pDelay);}
    
    public Double getRadius() {return this.radius.get();}
    public void setRadius(Double pRadius) {this.radius.set(pRadius);}  
    
    public MissionItem getMissionItem() {return this.missionItem.get();}
    public void setMissionItem(MissionItem missionItem) {this.missionItem.set(missionItem);}
}
