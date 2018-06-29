package com.dronegcs.console_plugin.layergroup_editor;


import com.db.gui.persistence.scheme.LayersGroup;
import com.db.persistence.scheme.BaseObject;
import com.dronegcs.console_plugin.ClosingPair;

import java.util.Collection;
import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
public interface LayersGroupsManager {

    <T extends LayersGroupEditor> T openLayersGroupEditor(String layersGroupName) throws LayersGroupUpdateException;

    <T extends LayersGroupEditor> T openLayersGroupEditor(LayersGroup layersGroup) throws LayersGroupUpdateException;

    <T extends LayersGroupEditor> T getLayersGroupEditor(LayersGroup layersGroup);

    <T extends LayersGroupEditor> ClosingPair<LayersGroup> closeLayersGroupEditor(T layersGroupEditor, boolean shouldSave);

    List<BaseObject> getAllLayersGroup();

    List<BaseObject> getAllModifiedLayersGroup();

    void delete(LayersGroup layersGroup);

    LayersGroup update(LayersGroup layersGroup) throws LayersGroupUpdateException;

    LayersGroup cloneLayersGroup(LayersGroup layersGroup) throws LayersGroupUpdateException;

    Collection<ClosingPair<LayersGroup>> closeAllLayersGroupEditors(boolean shouldSave);

    int reloadEditors();
}
