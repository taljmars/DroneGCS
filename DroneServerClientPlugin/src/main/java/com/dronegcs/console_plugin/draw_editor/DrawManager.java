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

    <T extends DrawEditor> T openDrawLayerEditor(String layerName);

    <T extends DrawEditor> T openDrawLayerEditor(Layer layer);

    Collection<ClosingPair<Layer>> flushAllItems(boolean isPublish);

    List<BaseObject> getAllDrawLayers();

    List<BaseObject> getAllModifiedDrawLayers();

    Shape getLayerItems(String drawItemUid);

    List<BaseObject> getLayerItems(Layer layer);

    void updateItem(BaseObject object);

    void removeItem(BaseObject object);

    boolean isDirty(BaseObject item);

    void load(BaseObject item);
}
