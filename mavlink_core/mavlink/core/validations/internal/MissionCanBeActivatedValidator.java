package mavlink.core.validations.internal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import mavlink.core.validations.MissionCanBeActivated;
import mavlink.drone.mission.Mission;
import mavlink.drone.mission.MissionItem;
import mavlink.drone.mission.waypoints.Waypoint;
import tools.geoTools.Coordinate;
import tools.geoTools.GeoTools;

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