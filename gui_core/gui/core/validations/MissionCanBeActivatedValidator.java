package gui.core.validations;

import gui.is.Coordinate;
import gui.is.validations.MissionCanBeActivated;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.MissionItem;
import mavlink.is.drone.mission.waypoints.Waypoint;
import mavlink.is.utils.geoTools.GeoTools;
import mavlink.is.utils.units.Length;

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
		
		Coordinate coord = wp.getCoordinate().convertToCoordinate();
		Length dist_between_drone_and_mission = GeoTools.getDistance(coord.ConvertToCoord2D(), mission.getDrone().getGps().getPosition());
		
		if (dist_between_drone_and_mission.valueInMeters() > MAX_DISTANCE_BETWEEN_MISSION_AND_DRONE) {
			//disable existing violation message
			arg1.disableDefaultConstraintViolation();
		    //build new violation message and add it
			arg1.buildConstraintViolationWithTemplate("Mission is to far from drone position (" + ((int) dist_between_drone_and_mission.valueInMeters()) + "m)").addConstraintViolation();
			return false;
		}
			
		return true;
	}

}