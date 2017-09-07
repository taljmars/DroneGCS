package com.dronegcs.console_plugin.mission_editor;

import com.dronedb.persistence.scheme.*;
import com.dronedb.persistence.ws.internal.DatabaseValidationRemoteException;
import com.dronedb.persistence.ws.internal.*;
import com.dronedb.persistence.ws.internal.ObjectNotFoundException;
import com.dronegcs.console_plugin.ClosingPair;
import com.geo_tools.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import sun.rmi.runtime.Log;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
@Scope(scopeName = "prototype")
@Component
public class MissionEditorImpl implements ClosableMissionEditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(MissionEditorImpl.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
    private DroneDbCrudSvcRemote droneDbCrudSvcRemote;

    @Autowired @NotNull(message = "Internal Error: Failed to get query")
    private QuerySvcRemote querySvcRemote;

    @Autowired @NotNull(message = "Internal Error: Failed to get mission object crud")
    private MissionCrudSvcRemote missionCrudSvcRemote;

    private Mission mission;

    @Override
    public Mission open(Mission mission) throws MissionUpdateException {
        LOGGER.debug("Setting new mission to mission editor");
        this.mission = mission;
        return this.mission;
    }

    @Override
    public Mission open(String missionName) throws MissionUpdateException {
        LOGGER.debug("Setting new mission to mission editor");
        try {
            this.mission = (Mission) droneDbCrudSvcRemote.create(Mission.class.getName());
            this.mission.setName(missionName);
            this.mission = (Mission) droneDbCrudSvcRemote.update(this.mission);
            return this.mission;
        }
        catch (DatabaseValidationRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
    }

    @Override
    public ClosingPair<Mission> close(boolean shouldSave) {
        System.err.println("Close, should save:" + shouldSave);
        ClosingPair<Mission> missionClosingPair = null;
        Mission res = this.mission;
        if (!shouldSave) {
            System.err.println(String.format("Delete mission %s %s", res.getKeyId().getObjId(), res.getName()));
            //droneDbCrudSvcRemote.delete(mission);
            try {
                res = (Mission) droneDbCrudSvcRemote.readByClass(mission.getKeyId().getObjId().toString(), Mission.class.getName());
                System.err.println("Found original mission " + res.getKeyId().getObjId() + " " + res.getName());
                missionClosingPair = new ClosingPair(res, false);
            } catch (ObjectNotFoundException e) {
                System.err.println("Mission doesn't exist");
                missionClosingPair = new ClosingPair(this.mission, true);
            }
        }
        else {
            missionClosingPair = new ClosingPair(res, false);
        }
        //System.err.println(String.format("Before resetting %s %s", res.getKeyId().getObjId(), res.getName()));
        this.mission = null;
        LOGGER.debug("DroneMission editor finished");
        //System.err.println(String.format("After resetting %s %s", res.getKeyId().getObjId(), res.getName()));
        return missionClosingPair;
    }

    @Override
    public Waypoint createWaypoint() {
        return (Waypoint) missionCrudSvcRemote.createMissionItem(Waypoint.class.getName());
    }

    @Override
    public Waypoint addWaypoint(Coordinate position) throws MissionUpdateException {
        Waypoint waypoint = createWaypoint();
        Coordinate c3 = new Coordinate(position, mission.getDefaultAlt());
        waypoint.setLat(c3.getLat());
        waypoint.setLon(c3.getLon());
        waypoint.setAltitude(c3.getAltitude());
        return updateMissionItem(waypoint);
    }

    @Override
    public SplineWaypoint createSplineWaypoint() {
        return (SplineWaypoint) missionCrudSvcRemote.createMissionItem(SplineWaypoint.class.getName());
    }

    @Override
    public SplineWaypoint addSplineWaypoint(Coordinate position) throws MissionUpdateException {
        SplineWaypoint splineWaypoint = createSplineWaypoint();
        Coordinate c3 = new Coordinate(position);
        splineWaypoint.setLat(c3.getLat());
        splineWaypoint.setLon(c3.getLon());
        return updateMissionItem(splineWaypoint);
    }

    @Override
    public LoiterTurns createLoiterTurns() {
        return (LoiterTurns) missionCrudSvcRemote.createMissionItem(LoiterTurns.class.getName());
    }

    @Override
    public LoiterTurns addLoiterTurns(Coordinate position, Integer turns) throws MissionUpdateException {
        LoiterTurns loiterTurns = createLoiterTurns();
        Coordinate c3 = new Coordinate(position, mission.getDefaultAlt());
        loiterTurns.setLon(c3.getLon());
        loiterTurns.setLat(c3.getLat());
        loiterTurns.setAltitude(c3.getAltitude());
        loiterTurns.setTurns(turns);
        return updateMissionItem(loiterTurns);
    }

    @Override
    public LoiterTime createLoiterTime() {
        return (LoiterTime) missionCrudSvcRemote.createMissionItem(LoiterTime.class.getName());
    }

    @Override
    public LoiterTime addLoiterTime(Coordinate position, Integer seconds) throws MissionUpdateException {
        LoiterTime loiterTime = createLoiterTime();
        Coordinate c3 = new Coordinate(position, mission.getDefaultAlt());
        loiterTime.setLon(c3.getLon());
        loiterTime.setLat(c3.getLat());
        loiterTime.setAltitude(c3.getAltitude());
        loiterTime.setSeconds(seconds);
        return updateMissionItem(loiterTime);
    }

    @Override
    public LoiterUnlimited createLoiterUnlimited() {
        return (LoiterUnlimited) missionCrudSvcRemote.createMissionItem(LoiterUnlimited.class.getName());
    }

    @Override
    public LoiterUnlimited addLoiterUnlimited(Coordinate position) throws MissionUpdateException {
        LoiterUnlimited loiterUnlimited = createLoiterUnlimited();
        Coordinate c3 = new Coordinate(position, mission.getDefaultAlt());
        loiterUnlimited.setLon(c3.getLon());
        loiterUnlimited.setLat(c3.getLat());
        loiterUnlimited.setAltitude(c3.getAltitude());
        return updateMissionItem(loiterUnlimited);
    }

    @Override
    public Land createLandPoint() {
        return (Land) missionCrudSvcRemote.createMissionItem(Land.class.getName());
    }

    @Override
    public Land addLandPoint(Coordinate position) throws MissionUpdateException {
        Land land = createLandPoint();
        land.setAltitude(1.0);
        land.setLat(position.getLat());
        land.setLon(position.getLon());
        return updateMissionItem(land);
    }

    @Override
    public ReturnToHome createReturnToLaunch() {
        return (ReturnToHome) missionCrudSvcRemote.createMissionItem(ReturnToHome.class.getName());
    }

    @Override
    public ReturnToHome addReturnToLaunch() throws MissionUpdateException {
        ReturnToHome returnToHome = createReturnToLaunch();
        returnToHome.setAltitude(0.0);
        return updateMissionItem(returnToHome);
    }

    @Override
    public Takeoff createTakeOff() {
        return (Takeoff) missionCrudSvcRemote.createMissionItem(Takeoff.class.getName());
    }

    @Override
    public Takeoff addTakeOff(double altitude) throws MissionUpdateException {
        Takeoff takeoff = createTakeOff();
        takeoff.setFinishedAlt(altitude);
        return updateMissionItem(takeoff);
    }

    @Override
    public RegionOfInterest createRegionOfInterest() {
        return (RegionOfInterest) missionCrudSvcRemote.createMissionItem(RegionOfInterest.class.getName());
    }

    @Override
    public RegionOfInterest addRegionOfInterest(Coordinate position) throws MissionUpdateException {
        RegionOfInterest regionOfInterest = createRegionOfInterest();
        regionOfInterest.setAltitude(mission.getDefaultAlt() * 1.0);
        regionOfInterest.setLat(position.getLat());
        regionOfInterest.setLon(position.getLon());
        return updateMissionItem(regionOfInterest);
    }

    @Override
    public Mission getModifiedMission() {
        return this.mission;
    }

    @Override
    public Mission update(Mission mission) throws MissionUpdateException {
        try {
            LOGGER.debug("Current mission named '{}' have '{}' items", this.mission.getName(), this.mission.getMissionItemsUids().size());
            LOGGER.debug("After update, mission will be named '{}' with '{}' items", mission.getName(), mission.getMissionItemsUids().size());
            this.mission = (Mission) droneDbCrudSvcRemote.update(mission);
            LOGGER.debug("Updated mission name is '{}' with '{}' items", this.mission.getName(), this.mission.getMissionItemsUids().size());
            return this.mission;
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new MissionUpdateException(e.getMessage());
        }
    }

    @Override
    public <T extends MissionItem> void removeMissionItem(T missionItem) throws MissionUpdateException {
        mission.getMissionItemsUids().remove(missionItem.getKeyId().getObjId());
        try {
            mission = (Mission) droneDbCrudSvcRemote.update(mission);
        }
        catch (DatabaseValidationRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
    }

    @Override
    public List<MissionItem> getMissionItems() {
        List<MissionItem> missionItemList = new ArrayList<>();
        List<String> uuidList = mission.getMissionItemsUids();
        uuidList.forEach((String uuid) -> {
            try {
                missionItemList.add((MissionItem) droneDbCrudSvcRemote.readByClass(uuid.toString(), MissionItem.class.getName()));
            } catch (ObjectNotFoundException e) {
                LOGGER.error("Failed to get mission items", e);
            }
        });
        return missionItemList;
    }

    @Override
    public void delete() throws MissionUpdateException {
        try {
            droneDbCrudSvcRemote.delete(mission);
        } catch (DatabaseValidationRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
    }

    @Override
    public <T extends MissionItem> T updateMissionItem(T missionItem) throws MissionUpdateException {
        // Update Item
        T res = null;
        try {
            res = (T) droneDbCrudSvcRemote.update(missionItem);
            if (!mission.getMissionItemsUids().contains(res.getKeyId().getObjId())) {
                LOGGER.debug("MissionItem {} is not part of the mission, adding it", res.getKeyId().getObjId());
                mission.getMissionItemsUids().add(res.getKeyId().getObjId());
                LOGGER.debug("Mission items amount is now {} ", mission.getMissionItemsUids().size());
            }
            // Update Mission
            mission = (Mission) droneDbCrudSvcRemote.update(mission);
            return res;
        }
        catch (DatabaseValidationRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Mission Editor: ");
        builder.append(mission.getKeyId().getObjId() + " ");
        builder.append(mission.getName());
        return builder.toString();
    }
}
