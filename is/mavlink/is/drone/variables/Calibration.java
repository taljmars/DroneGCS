package mavlink.is.drone.variables;


import org.springframework.stereotype.Component;


import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.protocol.msg_metadata.MAVLinkMessage;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_statustext;
import mavlink.is.protocol.msgbuilder.MavLinkCalibration;

@Component("calibrationSetup")
public class Calibration extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8920003037397720341L;

	private String mavMsg;
	private boolean calibrating;

	public boolean startCalibration() {
        if(drone.getState().isFlying()) {
            calibrating = false;
        }
        else {
            calibrating = true;
            MavLinkCalibration.sendStartCalibrationMessage(drone);
        }
        return calibrating;
	}

	public void sendAckk(int step) {
		MavLinkCalibration.sendCalibrationAckMessage(step, drone);
	}

	public void processMessage(MAVLinkMessage msg) {
		if (msg.msgid == msg_statustext.MAVLINK_MSG_ID_STATUSTEXT) {
			msg_statustext statusMsg = (msg_statustext) msg;
			mavMsg = statusMsg.getText();

			if (mavMsg.contains("Calibration"))
				calibrating = false;

			drone.notifyDroneEvent(DroneEventsType.CALIBRATION_IMU);
		}
	}

	public String getMessage() {
		return mavMsg;
	}

	public void setCalibrating(boolean flag) {
		calibrating = flag;
	}

	public boolean isCalibrating() {
		return calibrating;
	}
}
