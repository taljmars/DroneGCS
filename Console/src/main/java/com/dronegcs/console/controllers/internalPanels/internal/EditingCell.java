package controllers.internalPanels.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import services.DialogManagerSvc;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javax.validation.constraints.NotNull;

public class EditingCell<T> extends TableCell<MissionItemTableEntry, T>
{
    @Autowired @NotNull(message = "Internal Error: Failed to get application context")
    private ApplicationContext applicationContext;

	private TextField textField;
	private StringConverter<T> convertor;
	 
    public EditingCell(StringConverter<T> converter) {
    	this.convertor = converter;
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

        setText("" + (Double) getItem());
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
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
        textField.focusedProperty().addListener( (ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) -> {
        	if (!arg2) {
        		T newVal;
        		try {
        			newVal = convertor.fromString(textField.getText());
        			commitEdit(newVal);
        		}
        		catch (NumberFormatException e) {
        			DialogManagerSvc dialogManager = applicationContext.getBean(DialogManagerSvc.class);
        			dialogManager.showErrorMessageDialog("Failed to convert value", e);
        		}
        	}
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
