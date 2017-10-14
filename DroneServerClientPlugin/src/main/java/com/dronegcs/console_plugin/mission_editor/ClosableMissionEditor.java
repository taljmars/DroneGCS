package com.dronegcs.console_plugin.mission_editor;

import com.dronedb.persistence.scheme.Mission;
import com.dronegcs.console_plugin.ClosingPair;

/**
 * Created by taljmars on 3/25/17.
 */
public interface ClosableMissionEditor extends MissionEditor {

    Mission open(Mission mission) throws MissionUpdateException;

    Mission open(String missionName) throws MissionUpdateException;

    ClosingPair<Mission> close(boolean shouldSave);
}
