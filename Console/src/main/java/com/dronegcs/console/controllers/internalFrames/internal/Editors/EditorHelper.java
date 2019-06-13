package com.dronegcs.console.controllers.internalFrames.internal.Editors;

import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.EditedLayer;
import com.gui.core.layers.AbstractLayer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;

import java.awt.*;

public interface EditorHelper<T extends EditedLayer> {

	boolean isBuildMode();

	void setBuildMode(boolean buildMode);

	ContextMenu buildTreeViewPopup(OperationalViewTree layerViewTree, OperationalViewMap layerViewMap, TreeItem treeItem);

	ContextMenu buildMapViewPopup(OperationalViewMap layerViewMap, Point point);

	void saveEditor();

	T startEditing(T layer);

	void removeItem(T value);

	void renameItem(T value);

	<P extends AbstractLayer> boolean isEdited(P abstractLayer);
}
