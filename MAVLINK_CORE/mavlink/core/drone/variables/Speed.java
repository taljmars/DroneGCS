package mavlink.core.drone.variables;

import mavlink.core.drone.DroneVariable;
import mavlink.core.drone.MyDroneImpl;
import mavlink.core.parameters.Parameter;

public class Speed extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -428837366703590700L;
	public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
	public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
	public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;
	private mavlink.is.utils.units.Speed verticalSpeed = new mavlink.is.utils.units.Speed(
			0);
	private mavlink.is.utils.units.Speed groundSpeed = new mavlink.is.utils.units.Speed(
			0);
	private mavlink.is.utils.units.Speed airSpeed = new mavlink.is.utils.units.Speed(
			0);
	private mavlink.is.utils.units.Speed targetSpeed = new mavlink.is.utils.units.Speed(
			0);

	public Speed(MyDroneImpl myDroneImpl) {
		super(myDroneImpl);
	}

	public mavlink.is.utils.units.Speed getVerticalSpeed() {
		return verticalSpeed;
	}

	public mavlink.is.utils.units.Speed getGroundSpeed() {
		return groundSpeed;
	}

	public mavlink.is.utils.units.Speed getAirSpeed() {
		return airSpeed;
	}

	public mavlink.is.utils.units.Speed getTargetSpeed() {
		return targetSpeed;
	}

	public void setSpeedError(double aspd_error) {
		targetSpeed = new mavlink.is.utils.units.Speed(aspd_error
				+ airSpeed.valueInMetersPerSecond());
	}

	public void setGroundAndAirSpeeds(double groundSpeed, double airSpeed, double climb) {
		this.groundSpeed = new mavlink.is.utils.units.Speed(groundSpeed);
		this.airSpeed = new mavlink.is.utils.units.Speed(airSpeed);
		this.verticalSpeed = new mavlink.is.utils.units.Speed(climb);
		checkCollisionIsImminent();
	}

	public mavlink.is.utils.units.Speed getSpeedParameter(){
		Parameter param = myDrone.getParameters().getParameter("WPNAV_SPEED");
		if (param == null ) {
			return null;			
		}else{
			return new mavlink.is.utils.units.Speed(param.value/100);
		}
			
	}
	
	/**
	 * if drone will crash in 2 seconds at constant climb rate and climb rate <
	 * -3 m/s and altitude > 1 meter
	 */
	private void checkCollisionIsImminent() {

		double altitude = myDrone.getAltitude().getAltitude();
		if (altitude + verticalSpeed.valueInMetersPerSecond() * COLLISION_SECONDS_BEFORE_COLLISION < 0
				&& verticalSpeed.valueInMetersPerSecond() < COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND
				&& altitude > COLLISION_SAFE_ALTITUDE_METERS) {
			myDrone.getAltitude().setCollisionImminent(true);
		} else {
			myDrone.getAltitude().setCollisionImminent(false);
		}
	}

}
