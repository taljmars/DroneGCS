package com.dronegcs.console_plugin.mission_editor;

import com.dronedb.persistence.scheme.*;
import com.dronedb.persistence.ws.internal.QuerySvcRemote;
import com.dronedb.persistence.ws.internal.DroneDbCrudSvcRemote;
import com.dronedb.persistence.ws.internal.MissionCrudSvcRemote;
import com.dronedb.persistence.ws.internal.DatabaseValidationRemoteException;
import com.dronedb.persistence.ws.internal.ObjectNotFoundException;
import com.dronegcs.console_plugin.ClosingPair;
import com.geo_tools.Coordinate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by taljmars on 3/25/17.
 */
@Scope(scopeName = "prototype")
@Component
public class MissionEditorImpl implements ClosableMissionEditor {

    private final static Logger logger = Logger.getLogger(MissionEditorImpl.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
    private DroneDbCrudSvcRemote droneDbCrudSvcRemote;

    @Autowired @NotNull(message = "Internal Error: Failed to get query")
    private QuerySvcRemote querySvcRemote;

    @Autowired @NotNull(message = "Internal Error: Failed to get mission object crud")
    private MissionCrudSvcRemote missionCrudSvcRemote;

    private Mission mission;

    @Override
    public Mission open(Mission mission) throws MissionUpdateException {
        logger.debug("Setting new mission to mission editor");
        this.mission = mission;
        return this.mission;
    }

    @Override
    public Mission open(String missionName) throws MissionUpdateException {
        logger.debug("Setting new mission to mission editor");
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
        logger.debug("DroneMission editor finished");
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
        Coordinate c3 = new Coordinate(position, 20);
        waypoint.setLat(c3.getLat());
        waypoint.setLon(c3.getLon());
        waypoint.setAltitude(c3.getAltitude());
        return updateMissionItem(waypoint);
    }

    @Override
    public Circle createCirclePoint() {
        return (Circle) missionCrudSvcRemote.createMissionItem(Circle.class.getName());
    }

    @Override
    public Circle addCirclePoint(Coordinate position) throws MissionUpdateException {
        Circle circle = createCirclePoint();
        Coordinate c3 = new Coordinate(position, 20);
        circle.setLon(c3.getLon());
        circle.setLat(c3.getLat());
        circle.setAltitude(c3.getAltitude());
        return updateMissionItem(circle);
    }

    @Override
    public Land createLandPoint() {
        return (Land) missionCrudSvcRemote.createMissionItem(Land.class.getName());
    }

    @Override
    public Land addLandPoint(Coordinate position) throws MissionUpdateException {
        Land land = createLandPoint();
        land.setAltitude(20.0);
        land.setLat(position.getLat());
        land.setLon(position.getLon());
        return updateMissionItem(land);
    }

    @Override
    public ReturnToHome createReturnToLunch() {
        return (ReturnToHome) missionCrudSvcRemote.createMissionItem(ReturnToHome.class.getName());
    }

    @Override
    public ReturnToHome addReturnToLunch() throws MissionUpdateException {
        ReturnToHome returnToHome = createReturnToLunch();
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
        regionOfInterest.setAltitude(20.0);
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
            this.mission = (Mission) droneDbCrudSvcRemote.update(mission);
            return this.mission;
        }
        catch (Exception e) {
            logger.error(e.getMessage());
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
                e.printStackTrace();
                // TODO
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
            mission.getMissionItemsUids().add(res.getKeyId().getObjId());
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
