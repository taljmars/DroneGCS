package com.dronegcs.console_plugin.services.internal.convertors;

import com.dronedb.persistence.scheme.Circle;
import com.dronedb.persistence.scheme.Mission;
import com.dronedb.persistence.scheme.Takeoff;
import com.dronedb.persistence.scheme.Waypoint;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.mavlink.is.drone.mission.ConvertMavlinkVisitor;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.DroneMissionItem;
import com.dronegcs.mavlink.is.drone.mission.commands.*;
import com.dronegcs.mavlink.is.drone.mission.survey.MavlinkSurvey;
import com.dronegcs.mavlink.is.drone.mission.waypoints.*;
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
    @Autowired @NotNull(message = "Internal Error: Failed to get mission manager")
    private MissionsManager missionsManager;

    private MissionEditor missionEditor;

    public Mission convert(DroneMission droneMission, Mission mission) {
        missionEditor = missionsManager.openMissionEditor(mission);

        Iterator<DroneMissionItem> itr = droneMission.getItems().iterator();
        while (itr.hasNext()) {
            DroneMissionItem droneMissionItem = itr.next();
            droneMissionItem.accept(this);
        }
        return missionsManager.closeMissionEditor(missionEditor, true);
    }

    @Override
    public void visit(MavlinkLand mavlinkLand) {

    }

    @Override
    public void visit(MavlinkReturnToHome mavlinkReturnToHome) {

    }

    @Override
    public void visit(MavlinkTakeoff mavlinkTakeoff) {
        Takeoff takeoff = missionEditor.createTakeOff();

        takeoff.setFinishedAlt(mavlinkTakeoff.getFinishedAlt());

        missionEditor.updateMissionItem(takeoff);
    }

    @Override
    public void visit(MavlinkStructureScanner mavlinkStructureScanner) {

    }

    @Override
    public void visit(MavlinkChangeSpeed mavlinkChangeSpeed) {

    }

    @Override
    public void visit(MavlinkRegionOfInterest mavlinkRegionOfInterest) {

    }

    @Override
    public void visit(MavlinkSurvey mavlinkSurvey) {

    }

    @Override
    public void visit(MavlinkEpmGripper mavlinkEpmGripper) {

    }

    @Override
    public void visit(MavlinkCameraTrigger mavlinkCameraTrigger) {

    }

    @Override
    public void visit(MavlinkSplineWaypoint mavlinkSplineWaypoint) {

    }

    @Override
    public void visit(MavlinkWaypoint mavlinkWaypoint) {
        Waypoint waypoint = missionEditor.createWaypoint();

        waypoint.setDelay(mavlinkWaypoint.getDelay());
        waypoint.setAcceptanceRadius(mavlinkWaypoint.getAcceptanceRadius());
        waypoint.setOrbitalRadius(mavlinkWaypoint.getOrbitalRadius());
        waypoint.setOrbitCCW(mavlinkWaypoint.isOrbitCCW());
        waypoint.setYawAngle(mavlinkWaypoint.getYawAngle());
        waypoint.setLat(mavlinkWaypoint.getCoordinate().getLat());
        waypoint.setLon(mavlinkWaypoint.getCoordinate().getLon());
        waypoint.setAltitude(mavlinkWaypoint.getAltitude());

        missionEditor.updateMissionItem(waypoint);
    }

    @Override
    public void visit(MavlinkCircle mavlinkCircle) {
        Circle circle = missionEditor.createCirclePoint();

        circle.setAltitude(mavlinkCircle.getAltitude());
        circle.setTurns(mavlinkCircle.getNumberOfTurns());
        circle.setLat(mavlinkCircle.getCoordinate().getLat());
        circle.setLon(mavlinkCircle.getCoordinate().getLon());
        circle.setRadius(mavlinkCircle.getRadius());

        missionEditor.updateMissionItem(circle);
    }
}
