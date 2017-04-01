package com.dronegcs.console_plugin.mission_editor;

import com.dronedb.persistence.scheme.*;
import com.geo_tools.Coordinate;

import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
public interface MissionEditor {

    Waypoint createWaypoint();
    Waypoint addWaypoint(Coordinate position);

    Circle createCirclePoint();
    Circle addCirclePoint(Coordinate position);

    ReturnToHome createReturnToLunch();
    ReturnToHome addReturnToLunch();

    Land createLandPoint();
    Land addLandPoint(Coordinate position);

    Takeoff createTakeOff();
    Takeoff addTakeOff();

    RegionOfInterest createRegionOfInterest();
    RegionOfInterest addRegionOfInterest(Coordinate position);

    <T extends MissionItem> void removeMissionItem(T missionItem);

    <T extends MissionItem> T updateMissionItem(T missionItem);

    Mission update(Mission mission);

    Mission getModifiedMission();

    List<MissionItem> getMissionItems();
}
