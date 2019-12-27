package com.dronegcs.console.controllers.internalFrames.internal.EventLogs;

import com.dronegcs.tracker.objects.TrackerEvent;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Date;
import java.util.UUID;

public class EventLogTableEntry {

    private final SimpleObjectProperty icon;
    private final SimpleStringProperty eventSource;
    private final SimpleStringProperty date;
    private final SimpleStringProperty userName;
    private final SimpleObjectProperty<TrackerEvent.Type> type;
    private final SimpleStringProperty topic;
    private final SimpleStringProperty summary;
    private final SimpleObjectProperty data;
    private UUID uid;
    private Object payload;
//    private String data;

    public EventLogTableEntry() {
        this(null, "", "", "", TrackerEvent.Type.INFO, "", "", "");
    }

    public EventLogTableEntry(UUID uid, String eventSource, String date, String userName, TrackerEvent.Type type, String topic, String summary, Object data) {
        this.uid = uid;
        this.eventSource = new SimpleStringProperty(eventSource);
        this.date = new SimpleStringProperty(date);
        this.userName = new SimpleStringProperty(userName);
        this.type = new SimpleObjectProperty<TrackerEvent.Type>(type);
        this.icon = new SimpleObjectProperty(null);
        this.topic = new SimpleStringProperty(topic);
        this.summary = new SimpleStringProperty(summary);
        this.data = new SimpleObjectProperty(data);
//        this.payload = payload;

        generateIcon(type);
    }

    public String getEventSource() {
        return eventSource.get();
    }

    public SimpleStringProperty eventSourceProperty() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource.set(eventSource);
    }

    public Date getDate() {
//        return new Date(Date.parse(date.get()));
        return new Date();
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public void setDate(Date date) {
        this.date.set(date.toString());
    }

    public TrackerEvent.Type getType() {
        return type.get();
    }

    public SimpleObjectProperty<TrackerEvent.Type> typeProperty() {
        return type;
    }

    public void setType(TrackerEvent.Type type) {
        this.type.set(type);
        generateIcon(type);
    }

    private void generateIcon(TrackerEvent.Type type) {
        Image img = null;
        switch (type) {
            case WARNING:
                img = new Image(getClass().getResourceAsStream("/com/dronegcs/console/guiImages/alerts/warn.png"));
                break;
            case ERROR:
                img = new Image(getClass().getResourceAsStream("/com/dronegcs/console/guiImages/alerts/error.png"));
                break;
            case SUCCESS:
                img = new Image(getClass().getResourceAsStream("/com/dronegcs/console/guiImages/alerts/succ.png"));
                break;
            default:
                img = new Image(getClass().getResourceAsStream("/com/dronegcs/console/guiImages/alerts/info.png"));
                break;
        }
        ImageView iView = new ImageView(img);
        iView.setFitHeight(15);
        iView.setFitWidth(15);
        this.iconProperty().set(iView);
    }

    public String getTopic() {
        return topic.get();
    }

    public SimpleStringProperty topicProperty() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic.set(topic);
    }

    public String getSummary() {
        return summary.get();
    }

    public SimpleStringProperty summaryProperty() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary.set(summary);
    }

    public String getUserName() {
        return userName.get();
    }

    public SimpleStringProperty userNameProperty() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public Object getIcon() {
        return icon.get();
    }

    public SimpleObjectProperty iconProperty() {
        return icon;
    }

    public void setIcon(Object icon) {
        this.icon.set(icon);
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

//    public void setPayload(Object payload) {
//        this.payload = payload;
//    }
//
//    public Object getPayload() {
//        return payload;
//    }

    public void setData(Object data) {
        this.data.set(data);
    }

    public Object getData() {
        return data.get();
    }


    @Override
    public String toString() {
        return "EventLogEntry{" +
                "icon=" + icon +
                ", eventSource =" + eventSource +
                ", date=" + date +
                ", userName=" + userName +
                ", type=" + type+
                ", topic=" + topic +
                ", summary=" + summary +
                '}';
    }
}
