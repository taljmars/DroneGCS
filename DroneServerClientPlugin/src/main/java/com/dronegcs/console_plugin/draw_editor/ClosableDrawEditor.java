package com.dronegcs.console_plugin.draw_editor;

import com.db.gui.persistence.scheme.Layer;
import com.dronegcs.console_plugin.ClosingPair;

/**
 * Created by taljmars on 3/25/17.
 */
public interface ClosableDrawEditor extends DrawEditor {

    Layer open(Layer layer) throws DrawUpdateException;

    Layer open(String layerName) throws DrawUpdateException;

    ClosingPair<Layer> close(boolean shouldSave);
}
