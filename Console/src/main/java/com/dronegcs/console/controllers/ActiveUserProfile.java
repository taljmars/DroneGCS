package com.dronegcs.console.controllers;

import org.springframework.stereotype.Component;

@Component
public class ActiveUserProfile {

    public enum Mode {
        OFFLINE,
        ONLINE
    }

    private Mode mode;
    private String username;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
