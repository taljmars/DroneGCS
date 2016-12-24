package gui.core.mapTree;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;

import gui.core.mapTreeObjects.Layer;
import gui.core.mapTreeObjects.LayerGroup;
import gui.core.mapViewer.OperationalViewMap;
import gui.is.services.DialogManagerSvc;
import javafx.scene.control.TreeItem;

@ComponentScan("gui.is.services")
public abstract class LayeredViewTree<S extends TreeItem<Layer>> extends ViewTree<Layer> {
	
	@SuppressWarnings("rawtypes")
	private Class<? extends TreeItem> clazz;
	
	@Resource(name = "map")
	@NotNull(message = "Internal Error: Failed to get map view")
	protected OperationalViewMap map;
	
	@Resource(name = "dialogManagerSvc")
	@NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;

	public LayeredViewTree(S instance) {
		super();
		this.clazz = instance.getClass();
	}

	@SuppressWarnings("unchecked")
	protected S addTreeNode(Layer layer, LayerGroup layerGroup) {
		S layerGroupTreeitem = findCheckBoxTreeItemByLayer(layerGroup);
		if (layerGroupTreeitem == null) {
			dialogManagerSvc.showAlertMessageDialog("Failed to find group");
			return null;
		}
		S newTreeItem;
		try {
			newTreeItem = (S) clazz.newInstance();
			newTreeItem.setValue(layer);
			return (S) super.addTreeNode(newTreeItem, layerGroupTreeitem);
		} catch (InstantiationException | IllegalAccessException e) {
			dialogManagerSvc.showAlertMessageDialog("Failed to instanciate Tree item");
			return null;
		}
	}
	
	protected void removeFromTreeGroup(Layer layer) {
		S treeitem = findCheckBoxTreeItemByLayer(layer);
		if (treeitem == null) {
			dialogManagerSvc.showAlertMessageDialog("Failed to find layer");
			return;
		}
		
		S parentTreeitem = findCheckBoxTreeItemByLayer(layer.getParent());
		if (parentTreeitem == null) {
			dialogManagerSvc.showAlertMessageDialog("Failed to find layer parent");
			return;
		}
		
		super.removeTreeNode(treeitem, parentTreeitem);
	}
	
	public S findCheckBoxTreeItemByLayer(Layer layer) {
		@SuppressWarnings("unchecked")
		S res = (S) super.findTreeItemByValue(layer, getRoot());
		return res;
	}
	
	@Override
	protected void handleRemoveTreeItem(TreeItem<Layer> treeItem) {
		Layer layer = treeItem.getValue();
		map.removeLayer(layer);
		super.handleRemoveTreeItem(treeItem);
	}
}
