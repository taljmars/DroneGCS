package com.dronegcs.console.controllers.internalFrames.internal.EventLogs;

import com.dronegcs.tracker.objects.TrackerEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class EventLogCard extends StackPane {

    @FXML private VBox headerBox;
    @FXML private Label uuid;
    @FXML private Label date;
    @FXML private Label user;
    @FXML private Label topic;
    @FXML private Label summary;
    @FXML private TextArea data;

    @FXML private StackPane root;

    @Override
    public void setUserData(Object value) {
        super.setUserData(value);
        EventLogTableEntry eventLogTableEntry = (EventLogTableEntry) value;
        setDate(eventLogTableEntry.getDate());
        setUser(eventLogTableEntry.getUserName());
        setType(eventLogTableEntry.getType());
        setSummary(eventLogTableEntry.getSummary());
        setTopic(eventLogTableEntry.getTopic());
        setUuid(eventLogTableEntry.getUid());
        if (eventLogTableEntry.getData() != null)
            setData(String.valueOf(eventLogTableEntry.getData()));
    }

    private void setType(TrackerEvent.Type type) {
        switch (type) {
            case SUCCESS:
                headerBox.setStyle("-fx-background-color: #96dd86;");
                break;
            case OP_BEGIN:
                headerBox.setStyle("-fx-background-color: #darkgrey;");
                break;
            case INFO:
                headerBox.setStyle("-fx-background-color: #ace7ec;");
                break;
            case WARNING:
                headerBox.setStyle("-fx-background-color: #dfe075;");
                break;
            case ERROR:
                headerBox.setStyle("-fx-background-color: #f98882;");
                break;

        }
    }

    public void setUuid(String uuid) {
        this.uuid.setText(uuid);
    }

    public void setDate(Date date) {
        this.date.setText(date.toString());
    }

    public void setUser(String user) {
        this.user.setText(user);
    }

    public void setTopic(String topic) {
        this.topic.setText(topic);
    }

    public void setSummary(String summary) {
        this.summary.setText(summary);
    }

    public void setData(String data) {
        this.data.setText(data);
    }

    @FXML
    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) this.root.getScene().getWindow();
        stage.close();
    }
}
