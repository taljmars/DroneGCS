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

    SplineWaypoint createSplineWaypoint();
    SplineWaypoint addSplineWaypoint (Coordinate position);

    LoiterTurns createLoiterTurns();
    LoiterTurns addLoiterTurns(Coordinate position, Integer turns);

    LoiterTime createLoiterTime();
    LoiterTime addLoiterTime(Coordinate position, Integer seconds);

    LoiterUnlimited createLoiterUnlimited();
    LoiterUnlimited addLoiterUnlimited(Coordinate position);

    ReturnToHome createReturnToLaunch();
    ReturnToHome addReturnToLaunch();

    Land createLandPoint();
    Land addLandPoint(Coordinate position);

    Takeoff createTakeOff();
    Takeoff addTakeOff(double altitude);

    RegionOfInterest createRegionOfInterest();
    RegionOfInterest addRegionOfInterest(Coordinate position);

    <T extends MissionItem> void removeMissionItem(T missionItem);

    <T extends MissionItem> T updateMissionItem(T missionItem);

    Mission update(Mission mission);

    Mission getMission();

    List<MissionItem> getMissionItems();

    void deleteMission();

    Mission setMissionName(String name);
}
