package com.dronegcs.console_plugin.plugin_event;

import java.util.ArrayList;
import java.util.List;

public class ClientPluginEvent {

    private final TYPE type;

    public TYPE getType() {
        return type;
    }

    public List<Object> getPayload() {
        return payload;
    }

    public enum TYPE {
        SERVER_CLOCK, SERVER_LOST
    }

    public static ClientPluginEvent generate(TYPE type) {
        return new ClientPluginEvent(type);
    }

    private List<Object> payload;

    private ClientPluginEvent(TYPE type) {
        this.payload = new ArrayList<>();
        this.type = type;
    };

    public ClientPluginEvent add(Object o) {
        payload.add(o);
        return this;
    }


}
