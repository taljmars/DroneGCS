package com.dronegcs.console.controllers.internalPanels.internal;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class TableItemEntry {
	
    private final SimpleIntegerProperty order;
    private final SimpleStringProperty type;
    private final SimpleDoubleProperty lat, lon, altitude, delayOrTime, radius;
    private final SimpleIntegerProperty turns;
    private final SimpleObjectProperty<Object> referedItem;
 
    public TableItemEntry(Integer pOrder,
                          String itemType,
                          Double pLat, Double pLon, Double pAltitude,
                          Double pDelayOrTime, Double pRadius, Integer pTurns,
                          Object pReferedItem) {
    	
        this.order = new SimpleIntegerProperty(pOrder);
        this.type = new SimpleStringProperty(itemType);
        this.lat = new SimpleDoubleProperty(pLat);
        this.lon = new SimpleDoubleProperty(pLon);
        this.altitude = new SimpleDoubleProperty(pAltitude);
        this.delayOrTime = new SimpleDoubleProperty(pDelayOrTime);
        this.radius = new SimpleDoubleProperty(pRadius);
        this.turns = new SimpleIntegerProperty(pTurns);
        this.referedItem = new SimpleObjectProperty<Object>(pReferedItem);
    }


	public Integer getOrder() {return this.order.get();}
    public void setOrder(Integer pOrder) {this.order.set(pOrder);}
        
    public String getType() {return this.type.get();}
    public void setType(String pType) {this.type.set(pType);}
    
    public Double getLat() {return this.lat.get();}
    public void setLat(Double pLat) {this.lat.set(pLat);}
    
    public Double getLon() {return this.lon.get();}
    public void setLon(Double pLon) {this.lon.set(pLon);}
    
    public Double getAltitude() {return this.altitude.get();}
    public void setAltitude(Double pHeight) {this.altitude.set(pHeight);}
    
    public Double getDelayOrTime() {return this.delayOrTime.get();}
    public void setDelayOrTime(Double pDelayOrTime) {this.delayOrTime.set(pDelayOrTime);}
    
    public Double getRadius() {return this.radius.get();}
    public void setRadius(Double pRadius) {this.radius.set(pRadius);}

    public Integer getTurns() {return this.turns.get();}
    public void setTurns(Integer pTurns) {this.turns.set(pTurns);}
    
    public Object getReferedItem() {return this.referedItem.get();}
    public void setReferedItem(Object object) {this.referedItem.set(object);}
}
