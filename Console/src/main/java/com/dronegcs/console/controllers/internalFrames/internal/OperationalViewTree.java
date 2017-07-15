package com.dronegcs.console.controllers.internalFrames.internal;

import com.dronedb.persistence.scheme.BaseObject;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerPerimeter;
import com.gui.core.mapTree.CheckBoxViewTree;
import com.gui.core.mapTreeObjects.Layer;

/**
 * Created by oem on 5/5/17.
 */
public interface OperationalViewTree {

    public static final String UPLOADED_PREFIX = "(CURR) ";

    CheckBoxViewTree getTree();

    boolean hasPrivateSession();

    void regenerateTree();

    void removeItemByName(String name);

    Layer getLayerByName(String name);

    void addLayer(Layer layer);

    void removeLayer(Layer layer);

    String dumpTree();

    Layer getLayerByValue(BaseObject object);
}
