package is.mavlink.drone.variables;

import org.springframework.stereotype.Component;
import is.mavlink.drone.DroneVariable;
import is.mavlink.drone.parameters.Parameter;

@Component("speed")
public class Speed extends DroneVariable {

	public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
	public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
	public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;
	private is.mavlink.utils.units.Speed verticalSpeed = new is.mavlink.utils.units.Speed(0);
	private is.mavlink.utils.units.Speed groundSpeed = new is.mavlink.utils.units.Speed(0);
	private is.mavlink.utils.units.Speed airSpeed = new is.mavlink.utils.units.Speed(0);
	private is.mavlink.utils.units.Speed targetSpeed = new is.mavlink.utils.units.Speed(0);
	
	private is.mavlink.utils.units.Speed maxAirSpeed = new is.mavlink.utils.units.Speed(0);


	public is.mavlink.utils.units.Speed getVerticalSpeed() {
		return verticalSpeed;
	}

	public is.mavlink.utils.units.Speed getGroundSpeed() {
		return groundSpeed;
	}

	public is.mavlink.utils.units.Speed getAirSpeed() {
		return airSpeed;
	}

	public is.mavlink.utils.units.Speed getTargetSpeed() {
		return targetSpeed;
	}
	
	public is.mavlink.utils.units.Speed getMaxAirSpeed() {
		return maxAirSpeed;
	}

	public void setSpeedError(double aspd_error) {
		targetSpeed = new is.mavlink.utils.units.Speed(aspd_error
				+ airSpeed.valueInMetersPerSecond());
	}

	public void setGroundAndAirSpeeds(double groundSpeed, double airSpeed, double climb) {
		this.groundSpeed = new is.mavlink.utils.units.Speed(groundSpeed);
		this.airSpeed = new is.mavlink.utils.units.Speed(airSpeed);
		this.verticalSpeed = new is.mavlink.utils.units.Speed(climb);
		checkCollisionIsImminent();
		
		if (this.maxAirSpeed.valueInMetersPerSecond() < airSpeed) {
			this.maxAirSpeed = this.airSpeed;
		}
	}

	public is.mavlink.utils.units.Speed getSpeedParameter(){
		Parameter param = drone.getParameters().getParameter("WPNAV_SPEED");
		if (param == null ) {
			return null;			
		}else{
			return new is.mavlink.utils.units.Speed(param.value/100);
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
