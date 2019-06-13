package com.dronegcs.console_plugin.mission_editor;

import com.db.gui.persistence.scheme.BaseLayer;
import com.dronedb.persistence.scheme.*;
import com.geo_tools.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Created by taljmars on 3/25/17.
 */
@Scope(scopeName = "prototype")
@Component
public class MissionEditorImpl implements ClosableMissionEditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(MissionEditorImpl.class);

    @Autowired
    private MissionsManager missionsManager;

    private Mission mission;

    @Override
    public Mission open(Mission mission) {
        LOGGER.debug("Setting new mission to mission editor");
        this.mission = mission;
        missionsManager.updateItem(this.mission);
        return mission;
    }

    @Override
    public Mission open(String missionName){
        LOGGER.debug("Setting new mission to mission editor");
        this.mission = new Mission();
        this.mission.getKeyId().setObjId("DUMMY" + UUID.randomUUID().toString());
        this.mission.setName(missionName);
        this.mission.setMissionItemsUids(new ArrayList<>());
        missionsManager.updateItem(this.mission);
        return this.mission;
    }

    @Override
    public Waypoint createWaypoint() {
        return createTemplate(Waypoint.class);
    }

    @Override
    public Waypoint addWaypoint(Coordinate position) {
        Waypoint waypoint = createWaypoint();
        Coordinate c3 = new Coordinate(position, mission.getDefaultAlt());
        waypoint.setLat(c3.getLat());
        waypoint.setLon(c3.getLon());
        waypoint.setAltitude(c3.getAltitude());
        return updateMissionItem(waypoint);
    }

    @Override
    public SplineWaypoint createSplineWaypoint() {
        return createTemplate(SplineWaypoint.class);

    }

    @Override
    public SplineWaypoint addSplineWaypoint(Coordinate position) {
        SplineWaypoint splineWaypoint = createSplineWaypoint();
        Coordinate c3 = new Coordinate(position);
        splineWaypoint.setLat(c3.getLat());
        splineWaypoint.setLon(c3.getLon());
        return updateMissionItem(splineWaypoint);
    }

    @Override
    public LoiterTurns createLoiterTurns() {
        return createTemplate(LoiterTurns.class);
    }

    @Override
    public LoiterTurns addLoiterTurns(Coordinate position, Integer turns) {
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
        return createTemplate(LoiterTime.class);
    }

    @Override
    public LoiterTime addLoiterTime(Coordinate position, Integer seconds) {
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
        return createTemplate(LoiterUnlimited.class);
    }

    @Override
    public LoiterUnlimited addLoiterUnlimited(Coordinate position) {
        LoiterUnlimited loiterUnlimited = createLoiterUnlimited();
        Coordinate c3 = new Coordinate(position, mission.getDefaultAlt());
        loiterUnlimited.setLon(c3.getLon());
        loiterUnlimited.setLat(c3.getLat());
        loiterUnlimited.setAltitude(c3.getAltitude());
        return updateMissionItem(loiterUnlimited);
    }

    @Override
    public Land createLandPoint() {
        return createTemplate(Land.class);

    }

    @Override
    public Land addLandPoint(Coordinate position) {
        Land land = createLandPoint();
        land.setAltitude(1.0);
        land.setLat(position.getLat());
        land.setLon(position.getLon());
        return updateMissionItem(land);
    }

    @Override
    public ReturnToHome createReturnToLaunch() {
        return createTemplate(ReturnToHome.class);
    }

    @Override
    public ReturnToHome addReturnToLaunch() {
        ReturnToHome returnToHome = createReturnToLaunch();
        returnToHome.setAltitude(0.0);
        return updateMissionItem(returnToHome);
    }

    @Override
    public Takeoff createTakeOff() {
        return createTemplate(Takeoff.class);
    }

    @Override
    public Takeoff addTakeOff(double altitude) {
        Takeoff takeoff = createTakeOff();
        takeoff.setFinishedAlt(altitude);
        return updateMissionItem(takeoff);
    }

    @Override
    public RegionOfInterest createRegionOfInterest() {
        return createTemplate(RegionOfInterest.class);
    }

    @Override
    public RegionOfInterest addRegionOfInterest(Coordinate position) {
        RegionOfInterest regionOfInterest = createRegionOfInterest();
        regionOfInterest.setAltitude(mission.getDefaultAlt() * 1.0);
        regionOfInterest.setLat(position.getLat());
        regionOfInterest.setLon(position.getLon());
        return updateMissionItem(regionOfInterest);
    }

    private <T extends MissionItem> T createTemplate(Class<T> clz) {
        try {
            T obj = clz.newInstance();
            obj.getKeyId().setObjId("DUMMY" + UUID.randomUUID().toString());
            missionsManager.updateItem(obj);
            return obj;
        }
        catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Mission getMission() {
        return this.mission;
    }

    @Override
    public Mission update(Mission mission) {
        LOGGER.debug("Current mission named '{}' have '{}' items", this.mission.getName(), this.mission.getMissionItemsUids().size());
        LOGGER.debug("After update, mission will be named '{}' with '{}' items", mission.getName(), mission.getMissionItemsUids().size());
        this.mission = mission;
        LOGGER.debug("Updated mission name is '{}' with '{}' items", this.mission.getName(), this.mission.getMissionItemsUids().size());
        return this.mission;
    }

    @Override
    public <T extends MissionItem> void removeMissionItem(T missionItem) {
        String key = missionItem.getKeyId().getObjId();
        mission.getMissionItemsUids().remove(key);
        missionsManager.removeItem(missionItem);
    }

    @Override
    public List<MissionItem> getMissionItems() {
        List<MissionItem> missionItemList = new ArrayList<>();
        List<String> uuidList = mission.getMissionItemsUids();
        uuidList.forEach((String uuid) -> {
                MissionItem mItem = missionsManager.getMissionItem(uuid);
                missionItemList.add(mItem);
        });
        return missionItemList;
    }

    @Override
    public void deleteMission() {
        String key = mission.getKeyId().getObjId();
        for (String child : this.mission.getMissionItemsUids()) {
            MissionItem obj = missionsManager.getMissionItem(child);
            missionsManager.removeItem(obj);
        }
        missionsManager.removeItem(mission);
    }

    @Override
    public Mission setMissionName(String name) {
        this.mission.setName(name);
        return this.mission;
    }

    @Override
    public <T extends MissionItem> T updateMissionItem(T missionItem) {
        T res = missionItem;
        if (!mission.getMissionItemsUids().contains(res.getKeyId().getObjId())) {
            LOGGER.debug("MissionItem {} is not part of the mission, adding it", res.getKeyId().getObjId());
            mission.getMissionItemsUids().add(res.getKeyId().getObjId());
            LOGGER.debug("Mission items amount is now {} ", mission.getMissionItemsUids().size());
        }
        missionsManager.updateItem(res);
        return res;
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
