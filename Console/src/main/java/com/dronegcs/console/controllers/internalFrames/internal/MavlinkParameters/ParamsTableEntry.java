package com.dronegcs.console.controllers.internalFrames.internal.MavlinkParameters;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ParamsTableEntry {
	
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty title;
    private final SimpleStringProperty value;
    private final SimpleStringProperty defaultValue;
    private final SimpleStringProperty unit;
    private final SimpleIntegerProperty type;
    private final SimpleStringProperty description;
 
    public ParamsTableEntry(Integer pId, String pName, String pTitle, String pValue, String pDefaultValue, String pUnit, Integer pType, String pDescription) {
        this.id = new SimpleIntegerProperty(pId);
        this.name = new SimpleStringProperty(pName);
        this.title = new SimpleStringProperty(pTitle);
        this.value = new SimpleStringProperty(pValue);
        this.defaultValue = new SimpleStringProperty(pDefaultValue);
        this.unit = new SimpleStringProperty(pUnit);
        this.type = new SimpleIntegerProperty(pType);
        this.description = new SimpleStringProperty(pDescription);
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

    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
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

    public String getDefaultValue() {
        return defaultValue.get();
    }

    public SimpleStringProperty defaultValueProperty() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue.set(defaultValue);
    }

    public String getUnit() {
        return unit.get();
    }

    public SimpleStringProperty unitProperty() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit.set(unit);
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

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }
}
