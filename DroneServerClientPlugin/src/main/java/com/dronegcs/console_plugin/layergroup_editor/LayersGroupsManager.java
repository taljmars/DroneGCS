package com.dronegcs.console_plugin.layergroup_editor;


import com.db.gui.persistence.scheme.BaseLayer;
import com.db.gui.persistence.scheme.LayersGroup;
import com.db.persistence.scheme.BaseObject;
import com.dronegcs.console_plugin.ClosingPair;

import java.util.Collection;
import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
public interface LayersGroupsManager {

    <T extends LayersGroupEditor> T openLayersGroupEditor(String layersGroupName);

    <T extends LayersGroupEditor> T openLayersGroupEditor(LayersGroup layersGroup);

    List<BaseObject> getAllLayers();

    void refreshAllLayers();

    List<BaseObject> getAllLayersGroup();

    List<BaseObject> getAllModifiedLayersGroup();

    Collection<ClosingPair<LayersGroup>> flushAllItems(boolean shouldSave);

    BaseLayer getLayerItem(String uuid);

    void removeItem(BaseLayer obj);

    void updateItem(BaseLayer layer);

    void load(BaseLayer layer);

}
