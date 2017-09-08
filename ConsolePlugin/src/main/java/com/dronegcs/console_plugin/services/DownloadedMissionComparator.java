package com.dronegcs.console_plugin.services;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.services.internal.MissionComparatorException;
import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;
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

    public boolean isEqual(Mission mission, Mission downloadedMission) throws MissionComparatorException {
        LOGGER.debug("Comparing '{}' and '{}'", mission.getName(), downloadedMission.getName());
        // TODO: enable this code section
//        if (mission.getDefaultAlt() != downloadedMission.getDefaultAlt()) {
//            LOGGER.debug("Mission doesn't have the same default alt");
//            return false;
//        }

        if (mission.getMissionItemsUids().size() != downloadedMission.getMissionItemsUids().size()) {
            LOGGER.debug("Mission doesn't have the same amount of mission items");
            return false;
        }

        LOGGER.debug("Both mission have {} items", downloadedMission.getMissionItemsUids().size());

        Iterator<MissionItem> itrMission = missionsManager.getMissionItems(mission).iterator();
        Iterator<MissionItem> itrDownloadedMission = missionsManager.getMissionItems(downloadedMission).iterator();
        while (itrMission.hasNext()) {
            MissionItem missionItem = itrMission.next();
            MissionItem dMissionItem = itrDownloadedMission.next();

            if (!missionItem.getClass().equals(dMissionItem.getClass())) {
                LOGGER.debug("Mission doesn't have the same class order type");
                return false;
            }

            if (!eval(missionItem, dMissionItem)) {
                LOGGER.debug("Mission doesn't have an equal item");
                return false;
            }
        }

        LOGGER.debug("Mission are equal");
        return true;
    }

    public boolean eval(MissionItem missionItem, MissionItem downloadedMissionItem) throws MissionComparatorException {
        // Using reflection to call the relevant function
        // It is a factory pattern based on reflection
        LOGGER.debug("comparing '{}' to '{}'", missionItem, downloadedMissionItem);
        Method[] allMethods = this.getClass().getDeclaredMethods();
        for (int i = 0 ; i < allMethods.length ; i++) {
            Method method = allMethods[i];
            LOGGER.debug("Checking method: {}", method);

            if (!method.getName().equals("visit")) {
                LOGGER.debug("Failed to find visit function");
                continue;
            }
            if (method.getParameterTypes().length != 2) {
                LOGGER.debug("visit function receives {}, expected 2", method.getParameterTypes().length);
                continue;
            }
            if (method.getParameterTypes()[0] != missionItem.getClass()) {
                LOGGER.debug("First parameter of method is {}, expected {}", method.getParameterTypes()[0], missionItem.getClass());
                continue;
            }

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
        throw new MissionComparatorException("Failed to find comparator function for " + missionItem.getClass());
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
            waypoint.getDelay().equals(dWaypoint.getDelay()) &&
            waypoint.getAltitude().equals(dWaypoint.getAltitude()) &&
            waypoint.isOrbitCCW() == dWaypoint.isOrbitCCW())
        {
            return true;
        }

        return false;
    }

    private boolean visit(Circle circle, Circle dCircle) {
        if (CheckCoordinate(circle, dCircle) &&
            circle.getAltitude().equals(dCircle.getAltitude()) &&
            circle.getTurns() == dCircle.getTurns() &&
            circle.getRadius().equals(dCircle.getRadius()))
            return true;

        return false;
    }

    private boolean visit(ReturnToHome returnToHome, ReturnToHome dReturnToHome) {
        if (CheckCoordinate(returnToHome, dReturnToHome) && returnToHome.getAltitude().equals(dReturnToHome.getAltitude()))
            return true;

        return false;
    }

    private boolean visit(RegionOfInterest regionOfInterest, RegionOfInterest dRegionOfInterest) {
        if (CheckCoordinate(regionOfInterest, dRegionOfInterest) &&
                regionOfInterest.getAltitude().equals(dRegionOfInterest.getAltitude()))
            return true;

        return false;
    }

    private boolean visit(LoiterTurns loiterTurns, LoiterTurns dlLoiterTurns) {
        if (CheckCoordinate(loiterTurns, dlLoiterTurns) &&
                loiterTurns.getAltitude().equals(dlLoiterTurns.getAltitude()) &&
                loiterTurns.getTurns() == dlLoiterTurns.getTurns())
            return true;

        return false;
    }

    private boolean visit(LoiterTime loiterTime, LoiterTime dloiterTime) {
        if (CheckCoordinate(loiterTime, dloiterTime) &&
                loiterTime.getAltitude().equals(dloiterTime.getAltitude()) &&
                loiterTime.getSeconds() == dloiterTime.getSeconds())
            return true;

        return false;
    }

    private boolean visit(LoiterUnlimited loiterUnlimited, LoiterUnlimited dloiterUnlimited) {
        if (CheckCoordinate(loiterUnlimited, dloiterUnlimited) &&
                loiterUnlimited.getAltitude().equals(dloiterUnlimited.getAltitude()))
            return true;

        return false;
    }

    private boolean CheckCoordinate(MissionItem m1, MissionItem m2) {
        Coordinate m1Coordinate = new Coordinate(m1.getLat(), m1.getLon());
        Coordinate m2Coordinate = new Coordinate(m2.getLat(), m2.getLon());
        Double distance = GeoTools.getDistance(m1Coordinate, m2Coordinate);
        LOGGER.debug("Distance is {}", distance);
        // TODO: Fix the issue where the uploaded coordinate is abit different than in the DB
        if (distance < 1) {
            return true;
        }
        if (m1.getLat().equals(m2.getLat()) && m1.getLon().equals(m2.getLon()))
            return true;

        return false;
    }
}
