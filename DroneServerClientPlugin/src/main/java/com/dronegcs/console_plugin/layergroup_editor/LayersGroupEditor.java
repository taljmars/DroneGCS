package com.dronegcs.console_plugin.layergroup_editor;

import com.db.gui.persistence.scheme.LayersGroup;

/**
 * Created by taljmars on 3/25/17.
 */
public interface LayersGroupEditor {

    LayersGroup update(LayersGroup layerGroup) throws LayersGroupUpdateException;

    LayersGroup getLayersGroup();

    LayersGroup delete() throws LayersGroupUpdateException;

    LayersGroup setLayersGroupName(String name) throws LayersGroupUpdateException;
}
