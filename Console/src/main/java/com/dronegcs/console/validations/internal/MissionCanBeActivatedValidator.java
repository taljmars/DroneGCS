package com.dronegcs.console.validations.internal;

import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;
import com.dronegcs.mavlink.is.drone.mission.Mission;
import com.dronegcs.mavlink.is.drone.mission.MissionItem;
import com.dronegcs.mavlink.is.drone.mission.waypoints.Waypoint;
import com.dronegcs.console.validations.MissionCanBeActivated;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MissionCanBeActivatedValidator  implements ConstraintValidator<MissionCanBeActivated, Mission> {
	
	private static int MAX_DISTANCE_BETWEEN_MISSION_AND_DRONE = 200;

	@Override
	public void initialize(MissionCanBeActivated arg0) {

	}

	@Override
	public boolean isValid(Mission mission, ConstraintValidatorContext arg1) {
		Waypoint wp = null;
		for (MissionItem mi : mission.getItems()) {
			if (mi instanceof Waypoint) {
				wp = (Waypoint) mi;
				break;
			}
		}
		
		if (wp == null) {
			//disable existing violation message
			arg1.disableDefaultConstraintViolation();
		    //build new violation message and add it
			arg1.buildConstraintViolationWithTemplate("No waypoints found in mission").addConstraintViolation();
			return false;
		}
		
		Coordinate coord = wp.getCoordinate();
		double dist_between_drone_and_mission = GeoTools.getDistance(coord, mission.getDrone().getGps().getPosition());
		
		if (dist_between_drone_and_mission > MAX_DISTANCE_BETWEEN_MISSION_AND_DRONE) {
			//disable existing violation message
			arg1.disableDefaultConstraintViolation();
		    //build new violation message and add it
			arg1.buildConstraintViolationWithTemplate("Mission is to far from drone position (" + ((int) dist_between_drone_and_mission) + "m)").addConstraintViolation();
			return false;
		}
			
		return true;
	}

}