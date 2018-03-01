package com.ngxbridge.svc;

import com.dronegcs.console_plugin.services.internal.logevents.LogAbstractDisplayerEvent;
import com.generic_tools.logger.Logger;

public class EventMessage {

    private Logger.Type type;
    private String message;

    public EventMessage(Logger.Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public Logger.Type getType() {
        return type;
    }

    public void setType(Logger.Type type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "type=" + type +
                ", message='" + message + '\'' +
                '}';
    }
}
