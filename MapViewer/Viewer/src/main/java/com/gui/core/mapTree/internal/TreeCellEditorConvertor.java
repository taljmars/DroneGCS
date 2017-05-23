package com.gui.core.mapTree.internal;

import javafx.scene.control.TreeItem;

public interface TreeCellEditorConvertor<T> {
	
	public T fromString(TreeItem<T> treeItem, String title);
	
}
