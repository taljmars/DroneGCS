package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.mapviewer.gui.core.layers.LayerWithNameAndPayload;

/**
 * Created by taljmars on 4/30/17.
 */
public interface EditedLayer extends LayerWithNameAndPayload {

    void startEditing();

    void stopEditing();

    boolean isEdited();
}
