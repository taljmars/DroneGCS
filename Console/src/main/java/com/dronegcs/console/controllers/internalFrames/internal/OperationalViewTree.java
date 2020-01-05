package com.dronegcs.console.controllers.internalFrames.internal;

import com.mapviewer.gui.core.layers.AbstractLayer;
import com.mapviewer.gui.core.layers.LayerGroup;
import com.mapviewer.gui.core.mapTree.LayeredViewTree;

/**
 * Created by taljmars on 5/5/17.
 */
public interface OperationalViewTree extends LayeredViewTree {

    public static final String UPLOADED_PREFIX = "(CURR) ";

    AbstractLayer switchCurrentLayer(AbstractLayer fromLayer, AbstractLayer toLayer);

    boolean hasPrivateSession();

}
