package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.Shape;
import com.db.persistence.scheme.BaseObject;
import com.dronegcs.console_plugin.draw_editor.DrawManager;
import com.dronegcs.console_plugin.draw_editor.DrawUpdateException;
import com.mapviewer.gui.core.layers.AbstractLayer;
import com.mapviewer.gui.core.mapViewer.LayeredViewMap;
import com.mapviewer.gui.core.mapViewerObjects.MapMarkerDot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class LayerDraw extends EditedLayerImpl implements EditedLayer {

    private final static Logger LOGGER = LoggerFactory.getLogger(LayerDraw.class);

    public LayerDraw(String name, LayeredViewMap viewMap) {
        super(name, viewMap);
        startEditing();
    }

    @Override
    public void regenerateMapObjects() {
        removeAllMapObjects();

        Layer layer = (Layer) getPayload();
        if (layer == null)
            return;

        DrawManager layerManager = applicationContext.getBean(DrawManager.class);
        List<BaseObject> itemList = layerManager.getLayerItems(layer);
        LOGGER.debug("Regenerate map objects of layer draw (" + itemList.size() + ")");
        for (BaseObject object : itemList) {
            LOGGER.debug("Generate object: " + object);
            if (object instanceof Shape) {
                MapMarkerDot m = new MapMarkerDot("", ((Shape) object).getLat(), ((Shape) object).getLon());
                addMapMarker(m);
            }
        }
    }

    @Override
    public String getCaption() {
        return "Drawing";
    }
}
