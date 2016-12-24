package gui.core.mapTree;

import java.util.Collections;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public abstract class ViewTree<T> extends TreeView<T> {

	public ViewTree() {
		getSelectionModel().selectedItemProperty().addListener(listener -> handleTreeItemClick(getSelectionModel().getSelectedItem()));
		setEditable(true);
	}

	public ContextMenu getPopupMenu(TreeItem<T> treeItem) {
		
		ContextMenu popup = new ContextMenu();		
		
		MenuItem menuItemDelete = new MenuItem("Delete");
		menuItemDelete.setOnAction(handler -> handleRemoveTreeItem(treeItem));
		
		popup.getItems().add(menuItemDelete);
		
		return popup;
	}

	public TreeItem<T> findTreeItemByValue(T value, TreeItem<T> node) {
		if (node.getValue() == value)
			return node;
		
		for (TreeItem<T> child : node.getChildren()) {
			TreeItem<T> res = findTreeItemByValue(value, child);
			if (res != null && res.getValue() == value)
				return res;
		}
		
		return null;
	}

	public TreeItem<T> addTreeNode(TreeItem<T> item, TreeItem<T> parent) {
		Platform.runLater( () -> parent.getChildren().add(item));
		return item;
	}

	public void removeTreeNode(TreeItem<T> item, TreeItem<T> parent) {
		Platform.runLater( () -> parent.getChildren().remove(item));
	}
	
	public String dumpTree() {
		return dumpTree(getRoot(), 0);
	}
	
	private String dumpTree(TreeItem<T> item, int depth) {
		String ans = String.join("", Collections.nCopies(depth, " ")) + item.toString() + "\n"; 
		for (TreeItem<T> child : item.getChildren()) {
			ans += dumpTree(child, depth + 1);
		}
		return ans;
	}
	
	protected void handleRemoveTreeItem(TreeItem<T> treeItem) {
		removeTreeNode(treeItem, treeItem.getParent());
	}

	abstract void handleTreeItemClick(TreeItem<T> treeItem);
}
