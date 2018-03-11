package com.dronegcs.console.controllers.internalFrames.internal.EventLogs;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Date;

public class EventLogEntry {

    private final SimpleObjectProperty icon;
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty date;
    private final SimpleStringProperty userName;
    private final SimpleStringProperty code;
    private final SimpleStringProperty topic;
    private final SimpleStringProperty summary;

    public EventLogEntry() {
        this("", 0, "", "", "", "", "");
    }

    public EventLogEntry(String icon, Integer id, String date, String userName, String code, String topic, String summary) {SimpleObjectProperty icon1;
        this.icon = new SimpleObjectProperty();
        this.id = new SimpleIntegerProperty(id);
        this.date = new SimpleStringProperty(date);
        this.userName = new SimpleStringProperty(userName);
        this.code = new SimpleStringProperty(code);
        this.topic = new SimpleStringProperty(topic);
        this.summary = new SimpleStringProperty(summary);

    }

//    public String getIcon() {
//        return icon.get();
//    }
//
//    public SimpleStringProperty iconProperty() {
//        return icon;
//    }
//
//    public void setIcon(String icon) {
//        this.icon.set(icon);
//    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public Date getDate() {
        return new Date(Date.parse(date.get()));
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public void setDate(Date date) {
        this.date.set(date.toString());
    }

    public String getCode() {
        return code.get();
    }

    public SimpleStringProperty codeProperty() {
        return code;
    }

    public void setCode(String code) {
        this.code.set(code);
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

    @Override
    public String toString() {
        return "EventLogEntry{" +
                "icon=" + icon +
                ", id=" + id +
                ", date=" + date +
                ", userName=" + userName +
                ", code=" + code +
                ", topic=" + topic +
                ", summary=" + summary +
                '}';
    }
}
