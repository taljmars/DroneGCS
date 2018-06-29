package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.gui.core.layers.LayerGroup;

public class LayerGroupEditable extends LayerGroup implements EditedLayer {

    public LayerGroupEditable(String name) {
        super(name);
    }

    public LayerGroupEditable(LayerGroupEditable layerGroup) {
        super(layerGroup);
    }

    private boolean isEdited = false;

    @Override
    public void startEditing() {
        isEdited = true;
    }

    @Override
    public void stopEditing() {
        isEdited = false;
    }

    @Override
    public boolean isEdited() {
        return isEdited;
    }
}
