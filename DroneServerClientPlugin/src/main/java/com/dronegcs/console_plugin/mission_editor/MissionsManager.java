package com.dronegcs.console_plugin.mission_editor;

import com.db.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.Mission;
import com.dronedb.persistence.scheme.MissionItem;
import com.dronegcs.console_plugin.ClosingPair;

import java.util.Collection;
import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
public interface MissionsManager {

    <T extends MissionEditor> T openMissionEditor(String missionName);

    <T extends MissionEditor> T openMissionEditor(Mission mission);

    List<BaseObject> getAllMissions();

    List<BaseObject> getAllModifiedMissions();

    void removeItem(BaseObject object);

    void updateItem(BaseObject object);

    Mission getMission(String missionUid);

    MissionItem getMissionItem(String missionItemUid);
    List<MissionItem> getMissionItems(Mission mission);

    Collection<ClosingPair<BaseObject>> flushAllItems(boolean isPublish);

    boolean isDirty(BaseObject item);

    void load(BaseObject item);
}
