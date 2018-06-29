package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.gui.core.layers.LayerSingle;
import com.gui.core.mapViewer.LayeredViewMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Created by taljmars on 4/30/17.
 */
public class EditedLayerImpl extends LayerSingle implements EditedLayer {

    private final static Logger LOGGER = LoggerFactory.getLogger(EditedLayerImpl.class);

    protected ApplicationContext applicationContext;

    public EditedLayerImpl(String name, LayeredViewMap viewMap) {
        super(name, viewMap);
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private boolean isEdited = false;

    public void startEditing() {
        isEdited = true;
    }

    public void stopEditing() {
        isEdited = false;
    }

    public boolean isEdited() {
        return isEdited;
    }

}
