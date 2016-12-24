package gui.core.mapTree.internal;

import gui.core.mapTree.ViewTree;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyCode;

public class CheckBoxTreeCellEditor<T> extends CheckBoxTreeCell<T> {
	 
    private TextField textField;
	private TreeCellEditorConvertor<T> convertor;
	private Node graphic;
    
    public CheckBoxTreeCellEditor( TreeCellEditorConvertor<T> treeCellEditorConvertor ) {
		this.convertor = treeCellEditorConvertor;
		graphic = new CheckBox();
	}

    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null)
            createTextField();

        setText(null);
        setGraphic(textField);
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(((T) getItem()).toString());
        setGraphic(graphic);
    }

    @Override
    public void updateItem(T item, boolean empty) {
    	super.updateItem(item, empty);

    	if (empty) {
    		setText(null);
    		setGraphic(null);
    	} else {
    		if (isEditing()) {
    			if (textField != null) {
    				textField.setText(getString());
    			}
    			setText(null);
    			setGraphic(textField);               
    		} 
    		else {
    			setText(getString());
    			setGraphic(graphic);
    			
    			ContextMenu menu = ((ViewTree<T>) getTreeView()).getPopupMenu(getTreeItem());
    			setContextMenu(menu);
    		}
    	}	    
    }
    
    private void createTextField() {
        textField = new TextField(getString());
        textField.setOnKeyReleased( t -> {
            if (t.getCode() == KeyCode.ENTER) {
                commitEdit(convertor.fromString(getTreeItem(), textField.getText()));
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });  
        
    }

    private String getString() {
        return getItem() == null ? "Null" : getItem().toString();
    }
}