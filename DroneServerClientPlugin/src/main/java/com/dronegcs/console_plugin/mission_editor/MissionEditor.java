package com.dronegcs.console_plugin.mission_editor;

import com.dronedb.persistence.scheme.*;
import com.geo_tools.Coordinate;

import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
public interface MissionEditor {

    Waypoint createWaypoint();
    Waypoint addWaypoint(Coordinate position) throws MissionUpdateException;

    SplineWaypoint createSplineWaypoint();
    SplineWaypoint addSplineWaypoint (Coordinate position) throws MissionUpdateException;

    LoiterTurns createLoiterTurns();
    LoiterTurns addLoiterTurns(Coordinate position, int turns) throws MissionUpdateException;

    LoiterTime createLoiterTime();
    LoiterTime addLoiterTime(Coordinate position, int seconds) throws MissionUpdateException;

    LoiterUnlimited createLoiterUnlimited();
    LoiterUnlimited addLoiterUnlimited(Coordinate position) throws MissionUpdateException;

    ReturnToHome createReturnToLaunch();
    ReturnToHome addReturnToLaunch() throws MissionUpdateException;

    Land createLandPoint();
    Land addLandPoint(Coordinate position) throws MissionUpdateException;

    Takeoff createTakeOff();
    Takeoff addTakeOff(double altitude) throws MissionUpdateException;

    RegionOfInterest createRegionOfInterest();
    RegionOfInterest addRegionOfInterest(Coordinate position) throws MissionUpdateException;

    <T extends MissionItem> void removeMissionItem(T missionItem) throws MissionUpdateException;

    <T extends MissionItem> T updateMissionItem(T missionItem) throws MissionUpdateException;

    Mission update(Mission mission) throws MissionUpdateException;

    Mission getModifiedMission();

    List<MissionItem> getMissionItems();

    void delete() throws MissionUpdateException;
}
