package com.dronegcs.console_plugin.draw_editor;


import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.Shape;
import com.db.persistence.scheme.BaseObject;
import com.dronegcs.console_plugin.ClosingPair;

import java.util.Collection;
import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
public interface DrawManager {

    <T extends DrawEditor> T openDrawLayerEditor(String layerName) throws DrawUpdateException;

    <T extends DrawEditor> T openDrawLayerEditor(Layer layer) throws DrawUpdateException;

    <T extends DrawEditor> T getDrawLayerEditor(Layer layer);

    <T extends DrawEditor> ClosingPair<Layer> closeDrawLayerEditor(T drawLayerEditor, boolean shouldSave);

    List<BaseObject> getAllDrawLayers();

    List<BaseObject> getAllModifiedDrawLayers();

    void delete(Layer drawLayer);

    Layer update(Layer layer) throws DrawUpdateException;

    List<Shape> getDrawLayerItems(Layer layer);

    Layer cloneDrawLayer(Layer layer) throws DrawUpdateException;

    Collection<ClosingPair<Layer>> closeAllDrawLayersEditors(boolean shouldSave);

    List<BaseObject> getLayerItems(Layer layer);

    int loadEditors();
}
