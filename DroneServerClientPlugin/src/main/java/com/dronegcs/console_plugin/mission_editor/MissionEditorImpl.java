package com.dronegcs.console_plugin.mission_editor;

import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.dronedb.persistence.scheme.*;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.remote_services_wrappers.MissionCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.geo_tools.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by taljmars on 3/25/17.
 */
@Scope(scopeName = "prototype")
@Component
public class MissionEditorImpl implements ClosableMissionEditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(MissionEditorImpl.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
    private ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

    @Autowired @NotNull(message = "Internal Error: Failed to get mission object crud")
    private MissionCrudSvcRemoteWrapper missionCrudSvcRemote;

    private Mission mission;

    private Map<String, MissionItem> objectInLayer = new HashMap<>();
    private boolean markDeleted = false;

    @Override
    public Mission open(Mission mission) throws MissionUpdateException {
        LOGGER.debug("Setting new mission to mission editor");
        try {
            this.mission = mission;
            return mission;
        }
        catch (Throwable e) {
            LOGGER.debug("Failed to open mission editor", e);
        }
        return null;
    }

    @Override
    public Mission open(String missionName) throws MissionUpdateException {
        LOGGER.debug("Setting new mission to mission editor");
        try {
            this.mission = objectCrudSvcRemote.create(Mission.class.getCanonicalName());
            this.mission.setName(missionName);
            this.mission = objectCrudSvcRemote.update(this.mission);
            return this.mission;
        }
        catch (ObjectInstanceRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
        catch (DatabaseValidationRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
    }

    @Override
    public ClosingPair<Mission> close(boolean shouldSave) {
        LOGGER.debug("Close, should save:" + shouldSave);
        ClosingPair<Mission> missionClosingPair = null;
        Mission res = this.mission;
        if (!shouldSave) {
            LOGGER.debug(String.format("Delete mission %s %s", this.mission.getKeyId().getObjId(), this.mission.getName()));
            objectInLayer.clear();
            markDeleted = false;
            try {
                res = objectCrudSvcRemote.readByClass(mission.getKeyId().getObjId(), Mission.class.getCanonicalName());
                LOGGER.debug("Found original mission " + res.getKeyId().getObjId() + " " + res.getName());
                missionClosingPair = new ClosingPair(res, false);
            }
            catch (ObjectNotFoundRemoteException e) {
                LOGGER.error("Mission doesn't exist");
                missionClosingPair = new ClosingPair(this.mission, true);
            }
        }
        else {
            try {
                if (markDeleted) {
                    res = objectCrudSvcRemote.delete(this.mission);
                }
                else {
                    this.mission.getMissionItemsUids().forEach(uid -> {
                        try {
                            objectCrudSvcRemote.update(objectInLayer.get(uid));
                        } catch (DatabaseValidationRemoteException e) {
                            e.printStackTrace();
                        } catch (ObjectInstanceRemoteException e) {
                            e.printStackTrace();
                        }
                    });
                    objectInLayer.clear();
                    res = objectCrudSvcRemote.update(this.mission);
                }
            }
            catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException | ObjectNotFoundRemoteException e) {
                e.printStackTrace();
            }
            missionClosingPair = new ClosingPair(res, false);
        }
        this.mission = null;
        LOGGER.debug("DroneMission editor finished");
        return missionClosingPair;
    }

    @Override
    public Waypoint createWaypoint() throws MissionUpdateException {
        try {
            Waypoint obj = (Waypoint) missionCrudSvcRemote.createMissionItem(Waypoint.class.getCanonicalName());
            objectInLayer.put(obj.getKeyId().getObjId(), obj);
            return obj;
        } catch (ObjectInstanceRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
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
    public SplineWaypoint createSplineWaypoint() throws MissionUpdateException {
        try {
            SplineWaypoint obj = (SplineWaypoint) missionCrudSvcRemote.createMissionItem(SplineWaypoint.class.getCanonicalName());
            objectInLayer.put(obj.getKeyId().getObjId(), obj);
            return obj;
        } catch (ObjectInstanceRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
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
    public LoiterTurns createLoiterTurns() throws MissionUpdateException {
        try {
            LoiterTurns obj = (LoiterTurns) missionCrudSvcRemote.createMissionItem(LoiterTurns.class.getCanonicalName());
            objectInLayer.put(obj.getKeyId().getObjId(), obj);
            return obj;
        } catch (ObjectInstanceRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
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
    public LoiterTime createLoiterTime() throws MissionUpdateException {
        try {
            LoiterTime obj = (LoiterTime) missionCrudSvcRemote.createMissionItem(LoiterTime.class.getCanonicalName());
            objectInLayer.put(obj.getKeyId().getObjId(), obj);
            return obj;
        } catch (ObjectInstanceRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
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
    public LoiterUnlimited createLoiterUnlimited() throws MissionUpdateException {
        try {
            LoiterUnlimited obj = (LoiterUnlimited) missionCrudSvcRemote.createMissionItem(LoiterUnlimited.class.getCanonicalName());
            objectInLayer.put(obj.getKeyId().getObjId(), obj);
            return obj;
        } catch (ObjectInstanceRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
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
    public Land createLandPoint() throws MissionUpdateException {
        try {
            Land obj = (Land) missionCrudSvcRemote.createMissionItem(Land.class.getCanonicalName());
            objectInLayer.put(obj.getKeyId().getObjId(), obj);
            return obj;
        } catch (ObjectInstanceRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
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
    public ReturnToHome createReturnToLaunch() throws MissionUpdateException {
        try {
            ReturnToHome obj = (ReturnToHome) missionCrudSvcRemote.createMissionItem(ReturnToHome.class.getCanonicalName());
            objectInLayer.put(obj.getKeyId().getObjId(), obj);
            return obj;
        }
        catch (ObjectInstanceRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
    }

    @Override
    public ReturnToHome addReturnToLaunch() throws MissionUpdateException {
        ReturnToHome returnToHome = createReturnToLaunch();
        returnToHome.setAltitude(0.0);
        return updateMissionItem(returnToHome);
    }

    @Override
    public Takeoff createTakeOff() throws MissionUpdateException {
        try {
            Takeoff obj = (Takeoff) missionCrudSvcRemote.createMissionItem(Takeoff.class.getCanonicalName());
            objectInLayer.put(obj.getKeyId().getObjId(), obj);
            return obj;
        }
        catch (ObjectInstanceRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
    }

    @Override
    public Takeoff addTakeOff(double altitude) throws MissionUpdateException {
        Takeoff takeoff = createTakeOff();
        takeoff.setFinishedAlt(altitude);
        return updateMissionItem(takeoff);
    }

    @Override
    public RegionOfInterest createRegionOfInterest() throws MissionUpdateException {
        try {
            RegionOfInterest obj = (RegionOfInterest) missionCrudSvcRemote.createMissionItem(RegionOfInterest.class.getCanonicalName());
            objectInLayer.put(obj.getKeyId().getObjId(), obj);
            return obj;
        }
        catch (ObjectInstanceRemoteException e) {
            throw new MissionUpdateException(e.getMessage());
        }
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
    public Mission getMission() {
        return this.mission;
    }

    @Override
    public Mission update(Mission mission) throws MissionUpdateException {
        try {
            LOGGER.debug("Current mission named '{}' have '{}' items", this.mission.getName(), this.mission.getMissionItemsUids().size());
            LOGGER.debug("After update, mission will be named '{}' with '{}' items", mission.getName(), mission.getMissionItemsUids().size());
            this.mission.setName(mission.getName());
//            this.mission = objectCrudSvcRemote.update(mission);
            this.mission = mission;
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
//        try {
//            mission = objectCrudSvcRemote.update(mission);
//        }
//        catch (DatabaseValidationRemoteException e) {
//            throw new MissionUpdateException(e.getMessage());
//        } catch (ObjectInstanceRemoteException e) {
//            throw new MissionUpdateException(e.getMessage());
//        }
    }

    @Override
    public List<MissionItem> getMissionItems() {
        List<MissionItem> missionItemList = new ArrayList<>();
        List<String> uuidList = mission.getMissionItemsUids();
        uuidList.forEach((String uuid) -> {
            try {
                MissionItem mItem = objectInLayer.get(uuid);
                if (mItem == null)
                    mItem = (MissionItem) objectCrudSvcRemote.readByClass(uuid, MissionItem.class.getCanonicalName());

                missionItemList.add(mItem);
            }
            catch (ObjectNotFoundRemoteException e) {
                LOGGER.error("Failed to get mission items", e);
            }
        });
        return missionItemList;
    }

    @Override
    public Mission delete() throws MissionUpdateException {
//        try {
//            this.mission = objectCrudSvcRemote.delete(mission);
            this.markDeleted = true;
            return this.mission;
//        }
//        catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException | ObjectNotFoundRemoteException e) {
//            throw new MissionUpdateException(e.getMessage());
//        }
    }

    @Override
    public Mission setMissionName(String name) throws MissionUpdateException {
        try {
            this.mission.setName(name);
//            this.mission = objectCrudSvcRemote.update(mission);
            return this.mission;
        }
        catch (Exception e) {
            throw new MissionUpdateException(e.getMessage());
        }
    }

    @Override
    public <T extends MissionItem> T updateMissionItem(T missionItem) throws MissionUpdateException {
        // Update Item
        T res = null;
//        try {
//            res = (T) objectCrudSvcRemote.update(missionItem);
        res = missionItem;
            if (!mission.getMissionItemsUids().contains(res.getKeyId().getObjId())) {
                LOGGER.debug("MissionItem {} is not part of the mission, adding it", res.getKeyId().getObjId());
                mission.getMissionItemsUids().add(res.getKeyId().getObjId());
                LOGGER.debug("Mission items amount is now {} ", mission.getMissionItemsUids().size());
                objectInLayer.replace(missionItem.getKeyId().getObjId(), missionItem);
            }
            else {
                objectInLayer.put(missionItem.getKeyId().getObjId(), missionItem);
            }
            // Update Mission
//            mission = objectCrudSvcRemote.update(mission);
            return res;
//        }
//        catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException e) {
//            throw new MissionUpdateException(e.getMessage());
//        }
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
