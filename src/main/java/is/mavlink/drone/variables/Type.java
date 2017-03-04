package is.mavlink.drone.variables;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import is.logger.Logger;
import mavlink.core.firmware.FirmwareType;
import is.mavlink.drone.Drone;
import is.mavlink.drone.DroneInterfaces;
import is.mavlink.drone.DroneVariable;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.protocol.msg_metadata.enums.MAV_TYPE;

@Component("type")
public class Type extends DroneVariable implements DroneInterfaces.OnDroneListener {
	
	private static final int DEFAULT_TYPE = MAV_TYPE.MAV_TYPE_GENERIC;
	
	@Autowired
	@NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;

	private int type = DEFAULT_TYPE;
	private String firmwareVersion = null;

	static int called;
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		drone.addDroneListener(this);
	}

	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
			drone.notifyDroneEvent(DroneEventsType.TYPE);
			drone.loadVehicleProfile();
		}
	}

	public int getType() {
		return type;
	}

	public FirmwareType getFirmwareType() {
		if (drone.getMavClient().isConnected()) {
			switch (this.type) {

			case MAV_TYPE.MAV_TYPE_FIXED_WING:
				return FirmwareType.ARDU_PLANE;

			case MAV_TYPE.MAV_TYPE_GENERIC:
			case MAV_TYPE.MAV_TYPE_QUADROTOR:
			case MAV_TYPE.MAV_TYPE_COAXIAL:
			case MAV_TYPE.MAV_TYPE_HELICOPTER:
			case MAV_TYPE.MAV_TYPE_HEXAROTOR:
			case MAV_TYPE.MAV_TYPE_OCTOROTOR:
			case MAV_TYPE.MAV_TYPE_TRICOPTER:
				return FirmwareType.ARDU_COPTER;

			default:
				// unsupported - fall thru to offline condition
				logger.LogErrorMessege("Unsupported Profile");
			}
		}
		return drone.getPreferences().getVehicleType(); // offline or
															// unsupported
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String message) {
		firmwareVersion = message;
		drone.notifyDroneEvent(DroneEventsType.FIRMWARE);
	}

    public static boolean isCopter(int type){
        switch (type) {
            case MAV_TYPE.MAV_TYPE_TRICOPTER:
            case MAV_TYPE.MAV_TYPE_QUADROTOR:
            case MAV_TYPE.MAV_TYPE_HEXAROTOR:
            case MAV_TYPE.MAV_TYPE_OCTOROTOR:
            case MAV_TYPE.MAV_TYPE_HELICOPTER:
                return true;

            default:
                return false;
        }
    }

    public static boolean isPlane(int type){
        return type == MAV_TYPE.MAV_TYPE_FIXED_WING;
    }

    @SuppressWarnings("incomplete-switch")
	@Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch(event){
            case DISCONNECTED:
                setType(DEFAULT_TYPE);
                break;
        }
    }
}