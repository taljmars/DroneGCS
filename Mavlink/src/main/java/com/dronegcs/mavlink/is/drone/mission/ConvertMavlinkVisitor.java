package com.dronegcs.mavlink.is.drone.mission;

import com.dronegcs.mavlink.is.drone.mission.commands.*;
import com.dronegcs.mavlink.is.drone.mission.survey.MavlinkSurvey;
import com.dronegcs.mavlink.is.drone.mission.waypoints.*;

/**
 * Created by taljmars on 3/18/17.
 */
public interface ConvertMavlinkVisitor {

    void visit(MavlinkWaypoint mavlinkWaypoint);

    void visit(MavlinkCircle mavlinkCircle);

    void visit(MavlinkLand mavlinkLand);

    void visit(MavlinkReturnToHome mavlinkReturnToHome);

    void visit(MavlinkTakeoff mavlinkTakeoff);

    void visit(MavlinkStructureScanner mavlinkStructureScanner);

    void visit(MavlinkChangeSpeed mavlinkChangeSpeed);

    void visit(MavlinkRegionOfInterest mavlinkRegionOfInterest);

    void visit(MavlinkSurvey mavlinkSurvey);

    void visit(MavlinkEpmGripper mavlinkEpmGripper);

    void visit(MavlinkCameraTrigger mavlinkCameraTrigger);

    void visit(MavlinkSplineWaypoint mavlinkSplineWaypoint);
}
