package is.drone.variables;

import org.springframework.stereotype.Component;
import is.drone.DroneVariable;
import is.drone.parameters.Parameter;

@Component("speed")
public class Speed extends DroneVariable {

	public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
	public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
	public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;
	private is.units.Speed verticalSpeed = new is.units.Speed(0);
	private is.units.Speed groundSpeed = new is.units.Speed(0);
	private is.units.Speed airSpeed = new is.units.Speed(0);
	private is.units.Speed targetSpeed = new is.units.Speed(0);
	
	private is.units.Speed maxAirSpeed = new is.units.Speed(0);


	public is.units.Speed getVerticalSpeed() {
		return verticalSpeed;
	}

	public is.units.Speed getGroundSpeed() {
		return groundSpeed;
	}

	public is.units.Speed getAirSpeed() {
		return airSpeed;
	}

	public is.units.Speed getTargetSpeed() {
		return targetSpeed;
	}
	
	public is.units.Speed getMaxAirSpeed() {
		return maxAirSpeed;
	}

	public void setSpeedError(double aspd_error) {
		targetSpeed = new is.units.Speed(aspd_error
				+ airSpeed.valueInMetersPerSecond());
	}

	public void setGroundAndAirSpeeds(double groundSpeed, double airSpeed, double climb) {
		this.groundSpeed = new is.units.Speed(groundSpeed);
		this.airSpeed = new is.units.Speed(airSpeed);
		this.verticalSpeed = new is.units.Speed(climb);
		checkCollisionIsImminent();
		
		if (this.maxAirSpeed.valueInMetersPerSecond() < airSpeed) {
			this.maxAirSpeed = this.airSpeed;
		}
	}

	public is.units.Speed getSpeedParameter(){
		Parameter param = drone.getParameters().getParameter("WPNAV_SPEED");
		if (param == null ) {
			return null;			
		}else{
			return new is.units.Speed(param.value/100);
		}
			
	}
	
	/**
	 * if drone will crash in 2 seconds at constant climb rate and climb rate <
	 * -3 m/s and altitude > 1 meter
	 */
	private void checkCollisionIsImminent() {

		double altitude = drone.getAltitude().getAltitude();
		if (altitude + verticalSpeed.valueInMetersPerSecond() * COLLISION_SECONDS_BEFORE_COLLISION < 0
				&& verticalSpeed.valueInMetersPerSecond() < COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND
				&& altitude > COLLISION_SAFE_ALTITUDE_METERS) {
			drone.getAltitude().setCollisionImminent(true);
		} else {
			drone.getAltitude().setCollisionImminent(false);
		}
	}

}
