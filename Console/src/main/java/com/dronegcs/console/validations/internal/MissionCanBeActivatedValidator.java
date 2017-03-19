package com.dronegcs.console.validations.internal;

import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.waypoints.MavlinkWaypoint;
import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;
import com.dronegcs.mavlink.is.drone.mission.DroneMissionItem;
import com.dronegcs.console.validations.MissionCanBeActivated;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MissionCanBeActivatedValidator  implements ConstraintValidator<MissionCanBeActivated, DroneMission> {
	
	private static int MAX_DISTANCE_BETWEEN_MISSION_AND_DRONE = 200;

	@Override
	public void initialize(MissionCanBeActivated arg0) {

	}

	@Override
	public boolean isValid(DroneMission droneMission, ConstraintValidatorContext arg1) {
		MavlinkWaypoint wp = null;
		for (DroneMissionItem mi : droneMission.getItems()) {
			if (mi instanceof MavlinkWaypoint) {
				wp = (MavlinkWaypoint) mi;
				break;
			}
		}
		
		if (wp == null) {
			//disable existing violation message
			arg1.disableDefaultConstraintViolation();
		    //build new violation message and add it
			arg1.buildConstraintViolationWithTemplate("No waypoints found in droneMission").addConstraintViolation();
			return false;
		}
		
		Coordinate coord = wp.getCoordinate();
		double dist_between_drone_and_mission = GeoTools.getDistance(coord, droneMission.getDrone().getGps().getPosition());
		
		if (dist_between_drone_and_mission > MAX_DISTANCE_BETWEEN_MISSION_AND_DRONE) {
			//disable existing violation message
			arg1.disableDefaultConstraintViolation();
		    //build new violation message and add it
			arg1.buildConstraintViolationWithTemplate("DroneMission is to far from drone position (" + ((int) dist_between_drone_and_mission) + "m)").addConstraintViolation();
			return false;
		}
			
		return true;
	}

}