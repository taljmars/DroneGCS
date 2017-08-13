package com.dronegcs.console.controllers;

import com.dronegcs.console_plugin.services.DialogManagerSvc;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;
import org.springframework.context.ApplicationContext;

public class EditingCell<TE,T> extends TableCell<TE, T> {

    private ApplicationContext applicationContext;

    protected StringConverter<T> converter;

    private TextField textField;

    public EditingCell(StringConverter<T> converter) {
        this.converter = converter;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText("" + (T) getItem());
        setGraphic(null);
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
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
//        textField.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) -> {
//            if (!arg2) {
//                commitFromString(textField.getText());
//            }
//        });
        textField.setOnKeyReleased(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                commitFromString(textField.getText());
                event.consume();
            }
        });
    }

    private void commitFromString(String str) {
        try {
            T newVal = converter.fromString(str);
            if (preCommit(newVal)) {
                commitEdit(newVal);
                postCommit(newVal);
            }
        }
        catch (NumberFormatException e) {
            DialogManagerSvc dialogManager = applicationContext.getBean(DialogManagerSvc.class);
            dialogManager.showErrorMessageDialog("Failed to convert value", e);
        }
    }

    protected String getString() {
        return getItem() == null ? "" : getItem().toString();
    }

    protected boolean preCommit(T val) {
        return true;
    }

    protected void postCommit(T val) {
        return;
    }
}
