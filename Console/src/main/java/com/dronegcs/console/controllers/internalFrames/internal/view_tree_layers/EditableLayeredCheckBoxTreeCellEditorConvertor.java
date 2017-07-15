package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.gui.core.mapTree.internal.TreeCellEditorConvertor;
import com.gui.core.mapTreeObjects.Layer;
import javafx.scene.control.TreeItem;

/**
 * Created by taljmars on 4/30/17.
 */
public class EditableLayeredCheckBoxTreeCellEditorConvertor implements TreeCellEditorConvertor<Layer> {

    @Override
    public Layer fromString(TreeItem<Layer> treeItem, String s) {
        Layer layer = treeItem.getValue();
        if (layer instanceof EditedLayer) {
            if (!(((EditedLayer) layer).isEdited()))
                ((EditedLayer) layer).startEditing();
            if (s.startsWith(EditedLayer.EDIT_PREFIX)) {
                throw new RuntimeException("Layer name cannot start with '*' mark");
            }
        }

        layer.setName(s);
        return layer;
    }
}
