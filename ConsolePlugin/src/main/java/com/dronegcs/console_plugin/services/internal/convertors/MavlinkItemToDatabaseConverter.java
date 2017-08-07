package com.dronegcs.console_plugin.services.internal.convertors;

import com.dronedb.persistence.scheme.Circle;
import com.dronedb.persistence.scheme.Mission;
import com.dronedb.persistence.scheme.Takeoff;
import com.dronedb.persistence.scheme.Waypoint;
import com.dronedb.persistence.scheme.ReturnToHome;
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
//            modifiedMission.setName("AutoGenerated_Name_" + System.currentTimeMillis());

            Iterator<DroneMissionItem> itr = droneMission.getItems().iterator();
            while (itr.hasNext()) {
                DroneMissionItem droneMissionItem = itr.next();
                droneMissionItem.accept(this);
            }

            LOGGER.debug("Conversion done");
            return mission;
        }
        catch (MavlinkConvertionException e) {
            throw new MissionCompilationException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkLand mavlinkLand) throws MavlinkConvertionException {
        try{
            throw new MissionUpdateException("Not implemented yet");
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkReturnToHome mavlinkReturnToHome) throws MavlinkConvertionException {
        try{
            LOGGER.debug("Converting Mavlink ReturnToLunch to DB ReturnToLunch");
            ReturnToHome returnToHome = missionEditor.createReturnToLunch();

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
            throw new MissionUpdateException("Not implemented yet");
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkSurvey mavlinkSurvey) throws MavlinkConvertionException {
        try{
            throw new MissionUpdateException("Not implemented yet");
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkEpmGripper mavlinkEpmGripper) throws MavlinkConvertionException {
        try{
            throw new MissionUpdateException("Not implemented yet");
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkCameraTrigger mavlinkCameraTrigger) throws MavlinkConvertionException {
        try{
            throw new MissionUpdateException("Not implemented yet");
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }

    @Override
    public void visit(MavlinkSplineWaypoint mavlinkSplineWaypoint) throws MavlinkConvertionException {
        try{
            throw new MissionUpdateException("Not implemented yet");
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
    public void visit(MavlinkCircle mavlinkCircle) throws MavlinkConvertionException {
        try {
            LOGGER.debug("Converting Mavlink Circle to DB Circle");
            Circle circle = missionEditor.createCirclePoint();

            circle.setAltitude(mavlinkCircle.getAltitude());
            circle.setTurns(mavlinkCircle.getNumberOfTurns());
            circle.setLat(mavlinkCircle.getCoordinate().getLat());
            circle.setLon(mavlinkCircle.getCoordinate().getLon());
            circle.setRadius(mavlinkCircle.getRadius());
            LOGGER.debug("Mavlink MavlinkCircle:\n{}\nWas converted to:\n{}", mavlinkCircle, circle);
            missionEditor.updateMissionItem(circle);
        }
        catch (MissionUpdateException e) {
            throw new MavlinkConvertionException(e.getMessage());
        }
    }
}
