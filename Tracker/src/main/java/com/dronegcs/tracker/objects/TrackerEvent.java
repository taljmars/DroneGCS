package com.dronegcs.tracker.objects;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TrackerEvent {

    public enum Type {
        OP_BEGIN,
        SUCCESS,
        INFO,
        WARNING,
        ERROR
    }

    private final String eventSource;
    private final String id;
    private final Date date;
    private final String topic;
    private final Type type;
    private final String summary;
    private final String userName;
    private final Object payload;

    public TrackerEvent(String userName, String eventSource, Type type, String topic, String summary) {
        this(userName, eventSource, type, topic, summary, null);
    }

    public TrackerEvent(String userName, String eventSource, Type type, String topic, String summary, Object payload) {
        this(userName, eventSource, UUID.randomUUID().toString(), type, topic, summary, payload);
    }

    public TrackerEvent(String userName, String eventSource, String id, Type type, String topic, String summary, Object payload) {
        this(userName, eventSource, id, type, Calendar.getInstance().getTime(), topic, summary, payload);
    }

    public TrackerEvent(String userName, String eventSource, String id, Type type, Date date, String topic, String summary, Object payload) {
        this.id = id;
        this.eventSource = eventSource;
        this.date = date;
        this.topic = topic;
        this.type = type;
        this.summary = summary;
        this.userName = userName;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }
    
//    public void setId(UUID id) {
//        this.id = id;
//    }

    public Date getDate() {
        return date;
    }

//    public void setDate(Date date) {
//        this.date = date;
//    }

    public Type getType() {
        return type;
    }
    
//    public void setCode(String code) {
//        this.code = code;
//    }

    public String getTopic() {
        return topic;
    }
    
//    public void setTopic(String topic) {
//        this.topic = topic;

//    }

    public String getEventSource() {
        return eventSource;
    }

    public String getSummary() {
        return summary;
    }
//    public void setSummary(String summary) {
//        this.summary = summary;

//    }

    public String getUserName() {
        return userName;
    }

//    public void setUserName(String userName) {
//        this.userName = userName;
//    }


    public Object getPayload() {
        return payload;
    }
}
