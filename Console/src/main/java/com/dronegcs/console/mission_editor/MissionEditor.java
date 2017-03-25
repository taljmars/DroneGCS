package com.dronegcs.console.mission_editor;

import com.dronedb.persistence.scheme.mission.*;
import com.geo_tools.Coordinate;

import java.util.List;

/**
 * Created by oem on 3/25/17.
 */
public interface MissionEditor {

    Waypoint addWaypoint(Coordinate coord);

    Circle addCirclePoint(Coordinate coord);

    ReturnToHome addReturnToLunch();

    Land addLandPoint(Coordinate coord);

    Takeoff addTakeOff();

    RegionOfInterest addRegionOfInterest(Coordinate coord);

    <T extends MissionItem> void removeMissionItem(T missionItem);

    <T extends MissionItem> T updateMissionItem(T missionItem);

    Mission update(Mission mission);

    Mission getModifiedMission();

    List<MissionItem> getMissionItems();
}
