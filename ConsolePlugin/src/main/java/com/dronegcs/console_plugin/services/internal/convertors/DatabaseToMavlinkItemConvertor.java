package com.dronegcs.console_plugin.services.internal.convertors;

import com.dronedb.persistence.scheme.mission.*;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.commands.MavlinkReturnToHome;
import com.dronegcs.mavlink.is.drone.mission.commands.MavlinkTakeoff;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkCircle;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkLand;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkRegionOfInterest;
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
        MavlinkLand mavlinkLand = new MavlinkLand(droneMission, new Coordinate(land.getLat(), land.getLon()));
        droneMission.addMissionItem(mavlinkLand);
    }

    @Override
    public void visit(Takeoff takeoff) {
        MavlinkTakeoff mavlinkTakeoff = new MavlinkTakeoff(droneMission, takeoff.getFinishedAlt());
        droneMission.addMissionItem(mavlinkTakeoff);
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
        MavlinkReturnToHome mavlinkReturnToHome = new MavlinkReturnToHome(droneMission);
        mavlinkReturnToHome.setHeight(returnToHome.getAltitude());
        droneMission.addMissionItem(mavlinkReturnToHome);
    }

    @Override
    public void visit(RegionOfInterest regionOfInterest) {
        // TODO: handle position value
        Coordinate coordinate = new Coordinate(regionOfInterest.getLat() ,regionOfInterest.getLon());
        MavlinkRegionOfInterest mavlinkRegionOfInterest = new MavlinkRegionOfInterest(droneMission, coordinate);
        mavlinkRegionOfInterest.setAltitude(regionOfInterest.getAltitude());
        droneMission.addMissionItem(mavlinkRegionOfInterest);
    }
}
