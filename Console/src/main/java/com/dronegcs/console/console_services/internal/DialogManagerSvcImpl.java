package com.dronegcs.console.console_services.internal;

import com.dronegcs.console_plugin.services.DialogManagerSvc;
import com.dronegcs.console_plugin.flightControllers.KeyBoardController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Optional;
import java.util.Vector;

/**
 * Created by taljmars on 3/8/17.
 */
@Component
public class DialogManagerSvcImpl implements DialogManagerSvc {

    private final static Logger LOGGER = LoggerFactory.getLogger(DialogManagerSvcImpl.class);
    @Autowired
    @NotNull(message = "Internal Error: Failed to get keyboard controller")
    private KeyBoardController keyboardController;

    @Resource(name = "GuiCSS")
    @NotNull(message = "Internal Error: Failed to get CSS style doc")
    private String CSS_STYLE;


    public int showConfirmDialog(String text, String title) {
        Alert alert = genAlert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE)
            return YES_OPTION;
        else
            return NO_OPTION;
    }

    public boolean showAlertMessageDialog(String msg) {
        Alert alert = genAlert(Alert.AlertType.WARNING);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == ButtonType.OK;
    }

    public boolean showErrorMessageDialog(String msg, Exception exception) {
        Alert alert = genAlert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == ButtonType.OK;
    }

    public int showOptionsDialog(String text, String title, Object object2, String[] options,
                                 String initialValue) {

        Alert alert = genAlert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);

        ButtonType defaultButton = null;
        Vector<ButtonType> buttonOptions = new Vector<>();
        for (String s : options) {
            ButtonType btn = new ButtonType(s);
            if (s.equals(initialValue))
                defaultButton = btn;
            buttonOptions.add(btn);
        }

        alert.getButtonTypes().removeAll(alert.getButtonTypes());
        for (ButtonType button : buttonOptions)
            alert.getButtonTypes().add(button);

        if (defaultButton != null)
            alert.getDialogPane().lookupButton(defaultButton);

        Optional<ButtonType> result = alert.showAndWait();
        return buttonOptions.indexOf(result.get());
    }

    public int showYesNoDialog(String text, String title, boolean defaultNo) {
        String[] options = {"Yes", "No"};
        return showOptionsDialog(text, title, null, options, defaultNo ? options[1] : options[0]);
    }

    public String showInputDialog(String text, String title, Object object2, Object object3, String initialValue) {
        keyboardController.HoldIfNeeded();
        TextInputDialog dialog = new TextInputDialog(initialValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(text);

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            keyboardController.ReleaseIfNeeded();
            return result.get();
        }
        keyboardController.ReleaseIfNeeded();
        return null;
    }

    public Pair<Object, Object> showMuliComboBoxMessageDialog(String labelList1, Object[] list1, Object list1default, String labelList2, Object[] list2, Object list2default) {
        Alert alert = genAlert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);

        // Create expandable Exception.
        ComboBox<Object> cmbList1 = new ComboBox<Object>();
        cmbList1.getItems().addAll(new Vector<Object>(Arrays.asList(list1)));
        if (!cmbList1.getItems().isEmpty())
            cmbList1.setValue(list1default);

        ComboBox<Object> cmbList2 = new ComboBox<Object>();
        cmbList2.getItems().addAll(list2);
        if (!cmbList2.getItems().isEmpty())
            cmbList2.setValue(list2default);

        Label label1 = new Label(labelList1);
        Label label2 = new Label(labelList2);

        GridPane.setVgrow(cmbList1, Priority.ALWAYS);
        GridPane.setHgrow(cmbList1, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label1, 0, 0);
        expContent.add(cmbList1, 1, 0);

        expContent.add(label2, 0, 1);
        expContent.add(cmbList2, 1, 1);
        alert.getDialogPane().setContent(expContent);

        Optional<ButtonType> result = alert.showAndWait();
        LOGGER.debug(result.toString() + " " + cmbList2.getValue() + " " + cmbList1.getValue());

        return result.get() == ButtonType.OK ? new Pair<Object, Object>(cmbList1.getValue(), cmbList2.getValue()) : null;
    }

    private Alert genAlert(Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add(CSS_STYLE);
        return alert;
    }
}
