package com.dronegcs.console_plugin.mission_editor;

import com.dronedb.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.Mission;
import com.dronedb.persistence.scheme.MissionItem;

import java.util.List;

/**
 * Created by oem on 3/25/17.
 */
public interface MissionsManager {

    <T extends MissionEditor> T openMissionEditor(String missionName);

    <T extends MissionEditor> T openMissionEditor(Mission mission);

    <T extends MissionEditor> T getMissionEditor(Mission mission);

    <T extends MissionEditor> Mission closeMissionEditor(T missionEditor, boolean shouldSave);

    List<BaseObject> getAllMissions();

    void delete(Mission mission);

    Mission update(Mission mission);

    List<MissionItem> getMissionItems(Mission mission);
}
