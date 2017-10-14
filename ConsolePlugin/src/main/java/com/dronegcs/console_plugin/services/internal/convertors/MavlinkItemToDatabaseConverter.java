package com.dronegcs.console_plugin.services.internal.convertors;

import com.dronedb.persistence.scheme.*;
import com.dronedb.persistence.ws.*;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.mavlink.is.drone.mission.ConvertMavlinkVisitor;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.DroneMissionItem;
import com.dronegcs.mavlink.is.drone.mission.MavlinkConvertionException;
import com.dronegcs.mavlink.is.drone.mission.commands.*;
import com.dronegcs.mavlink.is.drone.mission.survey.MavlinkSurvey;
import com.dronegcs.mavlink.is.drone.mission.waypoints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Iterator;

/**
 * Created by taljmars on 3/18/17.
 */
@Scope(value = "prototype")
@Component
public class MavlinkItemToDatabaseConverter implements ConvertMavlinkVisitor
{
    private final static Logger LOGGER = LoggerFactory.getLogger(MavlinkItemToDatabaseConverter.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get mission manager")
    private MissionsManager missionsManager;

    private MissionEditor missionEditor;

    public Mission convert(DroneMission droneMission, Mission mission) throws MissionCompilationException {
        try {
            LOGGER.debug("Converting Mavlink mission with {} items", droneMission.getItems().size());
            missionEditor = missionsManager.getMissionEditor(mission);

            Mission modifiedMission = missionEditor.getModifiedMission();
            modifiedMission.setDefaultAlt(droneMission.getDefaultAlt());
            modifiedMission = missionEditor.update(modifiedMission);

            LOGGER.debug("Before conversion {}", modifiedMission.getMissionItemsUids().size());

            Iterator<DroneMissionItem> itr = droneMission.getItems().iterator();
            while (itr.hasNext()) {
                DroneMissionItem droneMissionItem = itr.next();
                droneMissionItem.accept(this);
            }

            mission = modifiedMission = missionEditor.getModifiedMission();

            LOGGER.debug("After conversion: Name{} Items {}", modifiedMission, modifiedMission.getMissionItemsUids().size());
            LOGGER.debug("Conversion done");
            return mission;
        }
        catch (MavlinkConvertionException | MissionUpdateException e) {
            LOGGER.error("Failed to convert mission", e);
            throw new MissionCompilationException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkLand mavlinkLand) throws MavlinkConvertionException {
        try{
            LOGGER.debug("Converting Mavlink Land to DB Land");
            Land land = missionEditor.createLandPoint();

            land.setAltitude(mavlinkLand.getAltitude());
            land.setLat(mavlinkLand.getCoordinate().getLat());
            land.setLon(mavlinkLand.getCoordinate().getLon());
            LOGGER.debug("Mavlink Land:\n{}\nWas converted to:\n{}", mavlinkLand, land);
            missionEditor.updateMissionItem(land);

        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkReturnToHome mavlinkReturnToHome) throws MavlinkConvertionException {
        try{
            LOGGER.debug("Converting Mavlink ReturnToLunch to DB ReturnToLunch");
            ReturnToHome returnToHome = missionEditor.createReturnToLaunch();

            returnToHome.setAltitude(mavlinkReturnToHome.getHeight());
            LOGGER.debug("Mavlink MavlinkReturnToHome:\n{}\nWas converted to:\n{}", mavlinkReturnToHome, returnToHome);
            missionEditor.updateMissionItem(returnToHome);
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkTakeoff mavlinkTakeoff) throws MavlinkConvertionException {
        try {
            LOGGER.debug("Converting Mavlink Takeoff to DB Takeoff");
            Takeoff takeoff = missionEditor.createTakeOff();

            takeoff.setFinishedAlt(mavlinkTakeoff.getFinishedAlt());
            LOGGER.debug("Mavlink MavlinkTakeoff:\n{}\nWas converted to:\n{}", mavlinkTakeoff, takeoff);
            missionEditor.updateMissionItem(takeoff);
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkStructureScanner mavlinkStructureScanner) throws MavlinkConvertionException {
        try{
            LOGGER.debug("Should be implemented\n{}", mavlinkStructureScanner);
            throw new MissionUpdateException("Not implemented yet");
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkChangeSpeed mavlinkChangeSpeed) throws MavlinkConvertionException {
        try{
            throw new MissionUpdateException("Not implemented yet");
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkRegionOfInterest mavlinkRegionOfInterest) throws MavlinkConvertionException {
        try{
            LOGGER.debug("Converting Mavlink RegionOfInterest to DB ROI");
            RegionOfInterest regionOfInterest = missionEditor.createRegionOfInterest();

            regionOfInterest.setAltitude(mavlinkRegionOfInterest.getAltitude());
            regionOfInterest.setLat(mavlinkRegionOfInterest.getCoordinate().getLat());
            regionOfInterest.setLon(mavlinkRegionOfInterest.getCoordinate().getLon());

            LOGGER.debug("Mavlink MavlinkROI:\n{}\nWas converted to:\n{}", mavlinkRegionOfInterest, regionOfInterest);
            missionEditor.updateMissionItem(regionOfInterest);
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkSurvey mavlinkSurvey) throws MavlinkConvertionException {
        try{
            LOGGER.debug("Should be implemented\n{}", mavlinkSurvey);
            throw new MissionUpdateException("Not implemented yet");
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkEpmGripper mavlinkEpmGripper) throws MavlinkConvertionException {
        try{
            LOGGER.debug("Should be implemented\n{}", mavlinkEpmGripper);
            throw new MissionUpdateException("Not implemented yet");
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkCameraTrigger mavlinkCameraTrigger) throws MavlinkConvertionException {
        try{
            LOGGER.debug("Should be implemented\n{}", mavlinkCameraTrigger);
            throw new MissionUpdateException("Not implemented yet");
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkSplineWaypoint mavlinkSplineWaypoint) throws MavlinkConvertionException {
        try{
            LOGGER.debug("Converting Mavlink SplineWaypoint to DB SplineWaypoint");
            SplineWaypoint splineWaypoint = missionEditor.createSplineWaypoint();

            splineWaypoint.setDelay(mavlinkSplineWaypoint.getDelay());
            splineWaypoint.setLat(mavlinkSplineWaypoint.getCoordinate().getLat());
            splineWaypoint.setLon(mavlinkSplineWaypoint.getCoordinate().getLon());
            LOGGER.debug("Mavlink MavlinkSplineWaypoint:\n{}\nWas converted to:\n{}", mavlinkSplineWaypoint, splineWaypoint);
            missionEditor.updateMissionItem(splineWaypoint);
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkWaypoint mavlinkWaypoint) throws MavlinkConvertionException {
        try {
            LOGGER.debug("Converting Mavlink Waypoint to DB Waypoint");
            Waypoint waypoint = missionEditor.createWaypoint();

            waypoint.setDelay(mavlinkWaypoint.getDelay());
            waypoint.setAcceptanceRadius(mavlinkWaypoint.getAcceptanceRadius());
            waypoint.setOrbitalRadius(mavlinkWaypoint.getOrbitalRadius());
            waypoint.setOrbitCCW(mavlinkWaypoint.isOrbitCCW());
            waypoint.setYawAngle(mavlinkWaypoint.getYawAngle());
            waypoint.setLat(mavlinkWaypoint.getCoordinate().getLat());
            waypoint.setLon(mavlinkWaypoint.getCoordinate().getLon());
            waypoint.setAltitude(mavlinkWaypoint.getAltitude());
            LOGGER.debug("Mavlink MavlinkWaypoint:\n{}\nWas converted to:\n{}", mavlinkWaypoint, waypoint);
            missionEditor.updateMissionItem(waypoint);
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkLoiterTurns mavlinkLoiterTurns) throws MavlinkConvertionException {
        try {
            LOGGER.debug("Converting Mavlink LoiterTurns to DB LoiterTurns");
            LoiterTurns loiterTurns = missionEditor.createLoiterTurns();

            loiterTurns.setAltitude(mavlinkLoiterTurns.getAltitude());
            loiterTurns.setTurns(mavlinkLoiterTurns.getTurns());
            loiterTurns.setLat(mavlinkLoiterTurns.getCoordinate().getLat());
            loiterTurns.setLon(mavlinkLoiterTurns.getCoordinate().getLon());
            LOGGER.debug("Mavlink MavlinkLoiterTurns:\n{}\nWas converted to:\n{}", mavlinkLoiterTurns, loiterTurns);
            missionEditor.updateMissionItem(loiterTurns);
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkLoiterUnlimited mavlinkLoiterUnlimited) throws MavlinkConvertionException {
        try {
            LOGGER.debug("Converting Mavlink LoiterUnlimited to DB Loiter Unlimited");
            LoiterUnlimited loiterUnlimited = missionEditor.createLoiterUnlimited();

            loiterUnlimited.setAltitude(mavlinkLoiterUnlimited.getAltitude());
            loiterUnlimited.setLat(mavlinkLoiterUnlimited.getCoordinate().getLat());
            loiterUnlimited.setLon(mavlinkLoiterUnlimited.getCoordinate().getLon());
            LOGGER.debug("Mavlink MavlinkLoiterUnlimited:\n{}\nWas converted to:\n{}", mavlinkLoiterUnlimited, loiterUnlimited);
            missionEditor.updateMissionItem(loiterUnlimited);
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkLoiterTime mavlinkLoiterTime) throws MavlinkConvertionException {
        try {
            LOGGER.debug("Converting Mavlink LoiterTime to DB LoiterTime");
            LoiterTime loiterTime = missionEditor.createLoiterTime();

            loiterTime.setAltitude(mavlinkLoiterTime.getAltitude());
            LOGGER.error("TAL {} {} ", loiterTime.getAltitude(), mavlinkLoiterTime.getAltitude());
            loiterTime.setSeconds(mavlinkLoiterTime.getSeconds());
            loiterTime.setLat(mavlinkLoiterTime.getCoordinate().getLat());
            loiterTime.setLon(mavlinkLoiterTime.getCoordinate().getLon());
            LOGGER.debug("Mavlink MavlinkLoiterTime:\n{}\nWas converted to:\n{}", mavlinkLoiterTime, loiterTime);
            missionEditor.updateMissionItem(loiterTime);
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }
}
