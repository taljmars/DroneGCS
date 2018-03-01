package com.ngxbridge.svc.internal;

import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmModes;
import com.dronegcs.mavlink.is.protocol.msgbuilder.MavLinkArm;
import com.dronegcs.mavlink.is.protocol.msgbuilder.MavLinkModes;
import com.generic_tools.devices.SerialConnection;
import com.ngxbridge.svc.CommunicationSvc;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommunicationSvcImpl implements CommunicationSvc {

    private final static Logger LOGGER = Logger.getLogger(CommunicationSvcImpl.class);

    @Autowired private SerialConnection serialConnection;
    @Autowired private Drone drone;
    @Autowired private LoggerDisplayerSvc loggerDisplayerSvc;

    @Override
    public Object[] listPorts() {
        return serialConnection.listPorts();
    }

    @Override
    public void sync() {
        drone.getParameters().refreshParameters();
    }

    @Override
    public void armDisarm(boolean shouldArm) {
        if (shouldArm) {
            LOGGER.debug("arm");
            MavLinkArm.sendArmMessage(drone, true);
        }
        else {
            // Not selected
            if (drone.getState().isFlying()) {
                LOGGER.warn("Drone is flying, dis-arming motor is dangerous");
//                if (!TryLand())
//                    btnArm.setSelected(true);
                rtl();
            }
            else {
                LOGGER.debug("disarm");
                MavLinkArm.sendArmMessage(drone, false);
            }
        }
    }

    @Override
    public void land() {
        MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_LAND);
        loggerDisplayerSvc.logGeneral("Landing");
    }

    @Override
    public void rtl() {
        MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_RTL);
        loggerDisplayerSvc.logGeneral("Comming back to lunch position");
    }

    @Override
    public void holdPosition() {
        TryPoshold();
    }

    @Override
    public void startStopMission(boolean shouldStart) {
        if (shouldStart) {
            try {

            } catch (Exception e1) {
                LOGGER.error("Failed to start droneMission, please resolve issue and try again", e1);
//                btnStartMission.setSelected(false);
            }
        }
        else {
            TryPoshold();
        }
    }

    @Override
    public void enforcePerimeter(boolean isOn) {
        drone.getPerimeter().setEnforce(isOn);
    }

    @Override
    public void downloadMission() {
        drone.getWaypointManager().getWaypoints();
    }


//    private boolean TryLand() {
//        boolean result = true;
//        String[] options = {"MavlinkLand", "RTL", "Cancel"};
//        int n = dialogManagerSvc.showOptionsDialog("Choose MavlinkLand Option", "", null, options, drone.getGps().isPositionValid() ? options[1] : options[0]);
//        if (n == 0) {
//            MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_LAND);
//            loggerDisplayerSvc.logGeneral("Landing");
//            textNotificationPublisherSvc.publish("Landing");
//        }
//        else if(n == 1) {
//            MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_RTL);
//            loggerDisplayerSvc.logGeneral("Comming back to lunch position");
//            textNotificationPublisherSvc.publish("Return To Lunch");
//        }
//        else
//            result = false;
//
//        return result;
//    }

    private void TryPoshold() {
        if (drone.getGps().isPositionValid()) {
            drone.getState().changeFlightMode(ApmModes.ROTOR_POSHOLD);
            loggerDisplayerSvc.logGeneral("Flight Mode set to 'Position Hold' - GPS");
        }
        else {
            drone.getState().changeFlightMode(ApmModes.ROTOR_ALT_HOLD);
            loggerDisplayerSvc.logGeneral("Flight Mode set to 'Altitude Hold' - Barometer");
        }
    }
}
