package com.gui.core.mapTree.internal;

import com.gui.core.mapTree.CheckBoxViewTree;
import com.gui.core.mapTree.ViewTree;
import com.gui.core.mapTreeObjects.Layer;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyCode;

public class CheckBoxTreeCellEditor<T> extends CheckBoxTreeCell<T> {

    private TextField textField;
    private TreeCellEditorConvertor<T> convertor;
    private CheckBox graphic;

    public CheckBoxTreeCellEditor(TreeCellEditorConvertor<T> treeCellEditorConvertor) {
        this.convertor = treeCellEditorConvertor;
        graphic = new CheckBox();
        graphic.selectedProperty().addListener((observableValue, aBoolean, t1) -> handleMark(t1));
    }

    private void handleMark(Boolean aBoolean) {
        CheckBoxTreeItem<Layer> item = (CheckBoxTreeItem<Layer>) getTreeItem();
        ((CheckBoxViewTree) getTreeView()).handleTreeItemMark(item, aBoolean);
    }

    private String firstText;

    @Override
    public void startEdit() {
        super.startEdit();
        firstText = super.getText();
        System.out.println("First text " + firstText);

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
    public void commitEdit(T item) {
        System.out.println("From " + firstText + " to " + item.toString());
        super.commitEdit(item);
        ((ViewTree<T>) getTreeView()).updateTreeItemName(firstText, getTreeItem());
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            ObservableValue<Boolean> selectedState = getSelectedStateCallback().call(this.getTreeItem());
            if (selectedState != null) {
                //System.out.println("SEL");
            }

            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(graphic);

                ContextMenu menu = ((ViewTree<T>) getTreeView()).getPopupMenu(getTreeItem());
                setContextMenu(menu);
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setOnKeyReleased(t -> {
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