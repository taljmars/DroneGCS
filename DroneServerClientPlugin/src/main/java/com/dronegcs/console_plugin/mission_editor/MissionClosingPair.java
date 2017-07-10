package com.dronegcs.console_plugin.mission_editor;

import com.dronedb.persistence.scheme.Mission;

/**
 * Created by oem on 5/6/17.
 */
public class MissionClosingPair {

    private Mission mission;
    private boolean isDeleted = false;

    public MissionClosingPair(Mission mission, boolean isDeleted) {
        this.mission = mission;
        this.isDeleted = isDeleted;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Mission getMission() {
        return mission;
    }
}
