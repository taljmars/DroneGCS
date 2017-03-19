package com.dronegcs.console.services.internal.convertors;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkCircle;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkWaypoint;
import com.geo_tools.Coordinate;

/**
 * Created by taljmars on 3/18/17.
 */
public class DatabaseToMavlinkItemConvertor implements ConvertDatabaseVisitor {

    private DroneMission droneMission;

    public void setDroneMission(DroneMission droneMission) {
        this.droneMission = droneMission;
    }

    public DroneMission getDroneMission() {
        return droneMission;
    }

    @Override
    public void visit(Land land) {

    }

    @Override
    public void visit(Takeoff takeoff) {

    }

    @Override
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

    @Override
    public void visit(Circle circle) {
        MavlinkCircle mavlinkCircle = new MavlinkCircle(droneMission, new Coordinate(circle.getLat(), circle.getLon()));
        mavlinkCircle.setAltitude(circle.getAltitude());
        mavlinkCircle.setRadius(circle.getRadius());
        mavlinkCircle.setTurns(circle.getTurns());
        droneMission.addMissionItem(mavlinkCircle);
    }

    @Override
    public void visit(ReturnToHome returnToHome) {

    }
}
