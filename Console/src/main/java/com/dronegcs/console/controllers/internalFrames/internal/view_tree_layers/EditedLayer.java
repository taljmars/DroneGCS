package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.gui.core.mapTreeObjects.LayerSingle;
import com.gui.core.mapViewer.ViewMap;

/**
 * Created by oem on 4/30/17.
 */
public class EditedLayer extends LayerSingle {

    private boolean isEdited = false;

    public static final String EDIT_PREFIX = "*";

    public EditedLayer(String name, ViewMap viewMap) {
        super(name, viewMap);
    }

    public EditedLayer(LayerSingle layer, ViewMap viewMap) {
        super(layer, viewMap);
    }

    public void startEditing() {
        isEdited = true;
        setName(getName());
    }

    public void stopEditing() {
        isEdited = false;
        setName(getName(true));
    }

    @Override
    public void setName(String name) {
        if (isEdited() && !name.startsWith(EDIT_PREFIX))
            name = EDIT_PREFIX + name;
        super.setName(name);
    }

    public boolean isEdited() {
        return isEdited;
    }

    public String getName(boolean actualName) {
        String name = getName();
        if (name.startsWith(EDIT_PREFIX))
            name = name.substring(EDIT_PREFIX.length(), name.length());
        return name;
    }
}
