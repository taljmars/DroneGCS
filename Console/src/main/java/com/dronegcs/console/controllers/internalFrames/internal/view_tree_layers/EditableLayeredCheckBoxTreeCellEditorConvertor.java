package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.mapviewer.gui.core.layers.AbstractLayer;
import com.mapviewer.gui.core.mapTree.internal.TreeCellEditorConvertor;
import javafx.scene.control.TreeItem;

/**
 * Created by taljmars on 4/30/17.
 */
public class EditableLayeredCheckBoxTreeCellEditorConvertor implements TreeCellEditorConvertor<AbstractLayer> {

    @Override
    public AbstractLayer fromString(TreeItem<AbstractLayer> treeItem, String s) {
        AbstractLayer layer = treeItem.getValue();
        if (layer instanceof EditedLayer) {
            if (!(((EditedLayer) layer).isEdited()))
                ((EditedLayer) layer).startEditing();
        }

        layer.setName(s);
        return layer;
    }
}
