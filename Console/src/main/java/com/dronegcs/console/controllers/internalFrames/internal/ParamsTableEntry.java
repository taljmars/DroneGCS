package com.dronegcs.console.controllers.internalFrames.internal;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ParamsTableEntry {
	
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty value;
    private final SimpleIntegerProperty type;
 
    public ParamsTableEntry(Integer pId, String pName, String pValue, Integer pType) {
        this.id = new SimpleIntegerProperty(pId);
        this.name = new SimpleStringProperty(pName);
        this.value = new SimpleStringProperty(pValue);
        this.type = new SimpleIntegerProperty(pType);
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getValue() {
        return value.get();
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public int getType() {
        return type.get();
    }

    public SimpleIntegerProperty typeProperty() {
        return type;
    }

    public void setType(int type) {
        this.type.set(type);
    }
}
