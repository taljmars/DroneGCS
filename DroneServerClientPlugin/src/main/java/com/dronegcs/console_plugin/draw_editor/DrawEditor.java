package com.dronegcs.console_plugin.draw_editor;

import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.Shape;
import com.geo_tools.Coordinate;

import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
public interface DrawEditor {

    Shape createMarker() throws DrawUpdateException;

    Shape addMarker(Coordinate position) throws DrawUpdateException;

    <T extends Shape> void removeItem(T item) throws DrawUpdateException;

    <T extends Shape> T updateItem(T item) throws DrawUpdateException;

    Layer update(Layer layer) throws DrawUpdateException;

    Layer getModifiedLayer();

    <T extends Shape> List<T> getLayerItems();

    Layer delete() throws DrawUpdateException;

    Layer setDrawLayerName(String name) throws DrawUpdateException;
}
