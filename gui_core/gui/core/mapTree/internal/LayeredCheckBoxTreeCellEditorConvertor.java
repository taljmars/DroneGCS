package gui.core.mapTree.internal;

import gui.core.mapTreeObjects.Layer;
import javafx.scene.control.TreeItem;

public class LayeredCheckBoxTreeCellEditorConvertor implements TreeCellEditorConvertor<Layer>{

	@Override
	public Layer fromString(TreeItem<Layer> treeItem, String title) {
		Layer layer = treeItem.getValue();
		layer.setName(title);
		return layer;
	}
}
