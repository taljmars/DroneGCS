package com.dronegcs.console.mission_editor;

import com.dronedb.persistence.scheme.mission.Mission;

/**
 * Created by oem on 3/25/17.
 */
public interface ClosableMissionEditor extends MissionEditor {

    Mission open(Mission mission);

    Mission open(String missionName);

    Mission close(boolean shouldSave);
}
