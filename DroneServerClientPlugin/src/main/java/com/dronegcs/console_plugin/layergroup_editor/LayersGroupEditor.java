package com.dronegcs.console_plugin.layergroup_editor;

import com.db.gui.persistence.scheme.BaseLayer;
import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.LayersGroup;

/**
 * Created by taljmars on 3/25/17.
 */
public interface LayersGroupEditor {

    LayersGroup getLayersGroup();

    LayersGroup setLayersGroupName(String name);

    Layer addSubLayer(String name);

    LayersGroup addSubGroupLayer(String name);

    void deleteLayer();
}
