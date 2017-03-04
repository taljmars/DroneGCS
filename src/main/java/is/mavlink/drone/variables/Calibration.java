package is.mavlink.drone.variables;


import org.springframework.stereotype.Component;

import is.mavlink.drone.DroneVariable;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.protocol.msg_metadata.MAVLinkMessage;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_statustext;
import is.mavlink.protocol.msgbuilder.MavLinkCalibration;

@Component("calibrationSetup")
public class Calibration extends DroneVariable {

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
