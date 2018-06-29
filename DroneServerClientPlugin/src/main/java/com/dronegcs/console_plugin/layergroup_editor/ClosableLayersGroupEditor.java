package com.dronegcs.console_plugin.layergroup_editor;

import com.db.gui.persistence.scheme.LayersGroup;
import com.dronegcs.console_plugin.ClosingPair;

/**
 * Created by taljmars on 3/25/17.
 */
public interface ClosableLayersGroupEditor extends LayersGroupEditor {

    LayersGroup open(LayersGroup layersGroup) throws LayersGroupUpdateException;

    LayersGroup open(String missionName) throws LayersGroupUpdateException;

    ClosingPair<LayersGroup> close(boolean shouldSave);
}
