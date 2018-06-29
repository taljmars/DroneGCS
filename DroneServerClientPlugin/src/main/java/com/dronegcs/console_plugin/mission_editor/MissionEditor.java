package com.dronegcs.console_plugin.mission_editor;

import com.dronedb.persistence.scheme.*;
import com.geo_tools.Coordinate;

import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
public interface MissionEditor {

    Waypoint createWaypoint() throws MissionUpdateException;
    Waypoint addWaypoint(Coordinate position) throws MissionUpdateException;

    SplineWaypoint createSplineWaypoint() throws MissionUpdateException;
    SplineWaypoint addSplineWaypoint (Coordinate position) throws MissionUpdateException;

    LoiterTurns createLoiterTurns() throws MissionUpdateException;
    LoiterTurns addLoiterTurns(Coordinate position, Integer turns) throws MissionUpdateException;

    LoiterTime createLoiterTime() throws MissionUpdateException;
    LoiterTime addLoiterTime(Coordinate position, Integer seconds) throws MissionUpdateException;

    LoiterUnlimited createLoiterUnlimited() throws MissionUpdateException;
    LoiterUnlimited addLoiterUnlimited(Coordinate position) throws MissionUpdateException;

    ReturnToHome createReturnToLaunch() throws MissionUpdateException;
    ReturnToHome addReturnToLaunch() throws MissionUpdateException;

    Land createLandPoint() throws MissionUpdateException;
    Land addLandPoint(Coordinate position) throws MissionUpdateException;

    Takeoff createTakeOff() throws MissionUpdateException;
    Takeoff addTakeOff(double altitude) throws MissionUpdateException;

    RegionOfInterest createRegionOfInterest() throws MissionUpdateException;
    RegionOfInterest addRegionOfInterest(Coordinate position) throws MissionUpdateException;

    <T extends MissionItem> void removeMissionItem(T missionItem) throws MissionUpdateException;

    <T extends MissionItem> T updateMissionItem(T missionItem) throws MissionUpdateException;

    Mission update(Mission mission) throws MissionUpdateException;

    Mission getMission();

    List<MissionItem> getMissionItems();

    Mission delete() throws MissionUpdateException;

    Mission setMissionName(String name) throws MissionUpdateException;
}
