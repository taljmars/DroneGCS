package com.dronegcs.console_plugin.services.internal.convertors;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.commands.MavlinkReturnToHome;
import com.dronegcs.mavlink.is.drone.mission.commands.MavlinkTakeoff;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkCircle;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkLand;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkRegionOfInterest;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkWaypoint;
import com.geo_tools.Coordinate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by taljmars on 3/18/17.
 */
public class DatabaseToMavlinkItemConvertor {

    private DroneMission droneMission;

    public void setDroneMission(DroneMission droneMission) {
        this.droneMission = droneMission;
    }

    public DroneMission getDroneMission() {
        return droneMission;
    }

    public void eval(MissionItem missionItem) {
        Class clz = missionItem.getClass();
        Method[] allMethods = this.getClass().getDeclaredMethods();
        for (int i = 0 ; i < allMethods.length ; i++) {
            Method method = allMethods[i];
            if (!method.getName().equals("visit"))
                continue;
            if (method.getParameterTypes().length != 1)
                continue;
            if (method.getParameterTypes()[0] != missionItem.getClass())
                continue;

            method.setAccessible(true);
            try {
                method.invoke(missionItem);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            break;
        }
    }

    public void visit(Land land) {
        MavlinkLand mavlinkLand = new MavlinkLand(droneMission, new Coordinate(land.getLat(), land.getLon()));
        droneMission.addMissionItem(mavlinkLand);
    }

    public void visit(Takeoff takeoff) {
        MavlinkTakeoff mavlinkTakeoff = new MavlinkTakeoff(droneMission, takeoff.getFinishedAlt());
        droneMission.addMissionItem(mavlinkTakeoff);
    }

    public void visit(Waypoint waypoint) {
        MavlinkWaypoint mavlinkWaypoint = new MavlinkWaypoint(droneMission, new Coordinate(waypoint.getLat(), waypoint.getLon()));
        mavlinkWaypoint.setYawAngle(waypoint.getYawAngle());
        mavlinkWaypoint.setOrbitCCW(waypoint.isOrbitCCW());
        mavlinkWaypoint.setOrbitalRadius(waypoint.getOrbitalRadius());
        mavlinkWaypoint.setAcceptanceRadius(waypoint.getAcceptanceRadius());
        mavlinkWaypoint.setDelay(waypoint.getDelay());
        mavlinkWaypoint.setAltitude(waypoint.getAltitude());
        droneMission.addMissionItem(mavlinkWaypoint);
    }

    public void visit(Circle circle) {
        MavlinkCircle mavlinkCircle = new MavlinkCircle(droneMission, new Coordinate(circle.getLat(), circle.getLon()));
        mavlinkCircle.setAltitude(circle.getAltitude());
        mavlinkCircle.setRadius(circle.getRadius());
        mavlinkCircle.setTurns(circle.getTurns());
        droneMission.addMissionItem(mavlinkCircle);
    }

    public void visit(ReturnToHome returnToHome) {
        MavlinkReturnToHome mavlinkReturnToHome = new MavlinkReturnToHome(droneMission);
        mavlinkReturnToHome.setHeight(returnToHome.getAltitude());
        droneMission.addMissionItem(mavlinkReturnToHome);
    }

    public void visit(RegionOfInterest regionOfInterest) {
        // TODO: handle position value
        Coordinate coordinate = new Coordinate(regionOfInterest.getLat() ,regionOfInterest.getLon());
        MavlinkRegionOfInterest mavlinkRegionOfInterest = new MavlinkRegionOfInterest(droneMission, coordinate);
        mavlinkRegionOfInterest.setAltitude(regionOfInterest.getAltitude());
        droneMission.addMissionItem(mavlinkRegionOfInterest);
    }
}
