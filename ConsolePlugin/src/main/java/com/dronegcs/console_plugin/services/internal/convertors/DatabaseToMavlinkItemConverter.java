package com.dronegcs.console_plugin.services.internal.convertors;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.commands.MavlinkReturnToHome;
import com.dronegcs.mavlink.is.drone.mission.commands.MavlinkTakeoff;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkCircle;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkLand;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkRegionOfInterest;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkWaypoint;
import com.geo_tools.Coordinate;
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
public class DatabaseToMavlinkItemConverter {

    @Autowired @NotNull(message = "Internal Error: Failed to get mission manager")
    private MissionsManager missionsManager;

    private DroneMission droneMission;

    public DroneMission convert(Mission mission, DroneMission droneMission) {
        this.droneMission = droneMission;
        Iterator<MissionItem> itr = missionsManager.getMissionItems(mission).iterator();
        while (itr.hasNext())
            eval(itr.next());

        return this.droneMission;
    }

    public void eval(MissionItem missionItem) {
        // Using reflection to call the relevant function
        // It is a factory pattern based on reflection
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

    private void visit(Land land) {
        MavlinkLand mavlinkLand = new MavlinkLand(droneMission, new Coordinate(land.getLat(), land.getLon()));
        droneMission.addMissionItem(mavlinkLand);
    }

    private void visit(Takeoff takeoff) {
        MavlinkTakeoff mavlinkTakeoff = new MavlinkTakeoff(droneMission, takeoff.getFinishedAlt());
        droneMission.addMissionItem(mavlinkTakeoff);
    }

    private void visit(Waypoint waypoint) {
        MavlinkWaypoint mavlinkWaypoint = new MavlinkWaypoint(droneMission, new Coordinate(waypoint.getLat(), waypoint.getLon()));
        mavlinkWaypoint.setYawAngle(waypoint.getYawAngle());
        mavlinkWaypoint.setOrbitCCW(waypoint.isOrbitCCW());
        mavlinkWaypoint.setOrbitalRadius(waypoint.getOrbitalRadius());
        mavlinkWaypoint.setAcceptanceRadius(waypoint.getAcceptanceRadius());
        mavlinkWaypoint.setDelay(waypoint.getDelay());
        mavlinkWaypoint.setAltitude(waypoint.getAltitude());
        droneMission.addMissionItem(mavlinkWaypoint);
    }

    private void visit(Circle circle) {
        MavlinkCircle mavlinkCircle = new MavlinkCircle(droneMission, new Coordinate(circle.getLat(), circle.getLon()));
        mavlinkCircle.setAltitude(circle.getAltitude());
        mavlinkCircle.setRadius(circle.getRadius());
        mavlinkCircle.setTurns(circle.getTurns());
        droneMission.addMissionItem(mavlinkCircle);
    }

    private void visit(ReturnToHome returnToHome) {
        MavlinkReturnToHome mavlinkReturnToHome = new MavlinkReturnToHome(droneMission);
        mavlinkReturnToHome.setHeight(returnToHome.getAltitude());
        droneMission.addMissionItem(mavlinkReturnToHome);
    }

    private void visit(RegionOfInterest regionOfInterest) {
        // TODO: handle position value
        Coordinate coordinate = new Coordinate(regionOfInterest.getLat() ,regionOfInterest.getLon());
        MavlinkRegionOfInterest mavlinkRegionOfInterest = new MavlinkRegionOfInterest(droneMission, coordinate);
        mavlinkRegionOfInterest.setAltitude(regionOfInterest.getAltitude());
        droneMission.addMissionItem(mavlinkRegionOfInterest);
    }
}
