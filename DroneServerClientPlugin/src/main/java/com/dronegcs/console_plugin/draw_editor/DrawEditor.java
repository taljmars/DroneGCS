package com.dronegcs.console_plugin.draw_editor;

import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.Shape;
import com.geo_tools.Coordinate;

import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
public interface DrawEditor {

    Shape createMarker();

    Shape addMarker(Coordinate position);

    <T extends Shape> void removeItem(T item);

    <T extends Shape> T updateItem(T item);

    Layer update(Layer layer) ;

    Layer getModifiedLayer();

    <T extends Shape> List<T> getLayerItems();

    void deleteLayer();

    Layer setDrawLayerName(String name);
}
