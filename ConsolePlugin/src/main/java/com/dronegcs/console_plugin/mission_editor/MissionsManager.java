package com.dronegcs.console_plugin.mission_editor;

import com.dronedb.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.Mission;
import com.dronedb.persistence.scheme.MissionItem;

import java.util.Collection;
import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
public interface MissionsManager {

    <T extends MissionEditor> T openMissionEditor(String missionName) throws MissionUpdateException;

    <T extends MissionEditor> T openMissionEditor(Mission mission) throws MissionUpdateException;

    <T extends MissionEditor> T getMissionEditor(Mission mission);

    <T extends MissionEditor> MissionClosingPair closeMissionEditor(T missionEditor, boolean shouldSave);

    List<BaseObject> getAllMissions();

    List<BaseObject> getAllModifiedMissions();

    void delete(Mission mission);

    Mission update(Mission mission) throws MissionUpdateException;

    List<MissionItem> getMissionItems(Mission mission);

    Mission cloneMission(Mission mission) throws MissionUpdateException;

    Collection<MissionClosingPair> closeAllMissionEditors(boolean shouldSave);
}
