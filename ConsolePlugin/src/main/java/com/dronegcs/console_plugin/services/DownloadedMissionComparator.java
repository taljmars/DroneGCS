package com.dronegcs.console_plugin.services;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * Created by taljmars on 3/18/17.
 */
@Scope(value = "prototype")
@Component
public class DownloadedMissionComparator {

    private final static Logger LOGGER = LoggerFactory.getLogger(DownloadedMissionComparator.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get mission manager")
    private MissionsManager missionsManager;

    public boolean isEqual(Mission mission, Mission downloadedMission) {
        if (mission.getDefaultAlt() != downloadedMission.getDefaultAlt())
            return false;

        if (mission.getMissionItemsUids().size() != downloadedMission.getMissionItemsUids().size())
            return false;


        Iterator<MissionItem> itrMission = missionsManager.getMissionItems(mission).iterator();
        Iterator<MissionItem> itrDownloadedMission = missionsManager.getMissionItems(downloadedMission).iterator();
        while (itrMission.hasNext()) {
            MissionItem missionItem = itrMission.next();
            MissionItem dMissionItem = itrDownloadedMission.next();

            if (!missionItem.getClass().equals(dMissionItem.getClass()))
                return false;

            if (!eval(missionItem, dMissionItem))
                return false;
        }

        return true;
    }

    public boolean eval(MissionItem missionItem, MissionItem downloadedMissionItem) {
        // Using reflection to call the relevant function
        // It is a factory pattern based on reflection
        LOGGER.debug("comparing {} to {}", missionItem, downloadedMissionItem);
        Method[] allMethods = this.getClass().getDeclaredMethods();
        for (int i = 0 ; i < allMethods.length ; i++) {
            Method method = allMethods[i];
            if (!method.getName().equals("visit"))
                continue;
            if (method.getParameterTypes().length != 2)
                continue;
            if (method.getParameterTypes()[0] != missionItem.getClass())
                continue;

            method.setAccessible(true);
            try {
                return (boolean) method.invoke(this, missionItem, downloadedMissionItem);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("Failed to convert missionItem from DB to Mavlink", e);
            }
            break;
        }

        LOGGER.error("Found unknown type: {}, Object: {}", missionItem.getClass(), missionItem);
        return false;
    }

    private boolean visit(Land land, Land dLand) {
        if (CheckCoordinate(land, dLand) && land.getAltitude().equals(dLand.getAltitude()))
            return true;

        return false;
    }

    private boolean visit(Takeoff takeoff, Takeoff dTakeoff) {
        if (CheckCoordinate(takeoff, dTakeoff) && takeoff.getFinishedAlt() == dTakeoff.getFinishedAlt())
            return true;

        return false;
    }

    private boolean visit(Waypoint waypoint, Waypoint dWaypoint) {
        if (CheckCoordinate(waypoint, dWaypoint) &&
            waypoint.getYawAngle() == dWaypoint.getYawAngle() &&
            waypoint.getOrbitalRadius() == dWaypoint.getOrbitalRadius() &&
            waypoint.getAcceptanceRadius() == dWaypoint.getAcceptanceRadius() &&
            waypoint.getDelay() == dWaypoint.getDelay() &&
            waypoint.getAltitude() == dWaypoint.getAltitude() &&
            waypoint.isOrbitCCW() == dWaypoint.isOrbitCCW())
            return true;

        return false;
    }

    private boolean visit(Circle circle, Circle dCircle) {
        if (CheckCoordinate(circle, dCircle) &&
            circle.getAltitude() == dCircle.getAltitude() &&
            circle.getTurns() == dCircle.getTurns() &&
            circle.getRadius() == dCircle.getRadius())
            return true;

        return false;
    }

    private boolean visit(ReturnToHome returnToHome, ReturnToHome dReturnToHome) {
        if (CheckCoordinate(returnToHome, dReturnToHome) && returnToHome.getAltitude().equals(dReturnToHome.getAltitude()))
            return true;

        return false;
    }

    private boolean visit(RegionOfInterest regionOfInterest, RegionOfInterest dRegionOfInterest) {
        // TODO
        return false;
    }

    private boolean CheckCoordinate(MissionItem m1, MissionItem m2) {
        if (m1.getLat().equals(m2.getLat()) && m1.getLon().equals(m2.getLon()))
            return true;

        return false;
    }
}
