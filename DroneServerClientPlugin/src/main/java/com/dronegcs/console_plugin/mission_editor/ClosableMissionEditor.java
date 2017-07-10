package com.dronegcs.console_plugin.mission_editor;


import com.dronedb.persistence.scheme.Mission;

/**
 * Created by taljmars on 3/25/17.
 */
public interface ClosableMissionEditor extends MissionEditor {

    Mission open(Mission mission) throws MissionUpdateException;

    Mission open(String missionName) throws MissionUpdateException;

    MissionClosingPair close(boolean shouldSave);
}
