package is.mavlink.drone.variables;

import is.gui.services.LoggerDisplayerSvc;
import is.logger.Logger;

import mavlink.core.connection.helper.BeaconData;
import is.mavlink.drone.DroneVariable;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.protocol.msg_metadata.ApmModes;
import tools.geoTools.Coordinate;

import javax.swing.SwingWorker;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("beacon")
public class Beacon extends DroneVariable {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;
	
	private boolean pIsActive;
	private Coordinate pLastPosition;
	private transient SwingWorker<Void, Void> pFollowThread = null;

	public Beacon() {
		pIsActive = false;
		pLastPosition = null;
	}
	
	public void setPosition(Coordinate coord) {
		pLastPosition = coord;
		drone.notifyDroneEvent(DroneEventsType.BEACON_BEEP);
	}
	
	public Coordinate getPosition() {
		return pLastPosition;
	}
	
	public void setActive(boolean should_be_active) {
		pIsActive = should_be_active;
		if (pIsActive) {
			drone.notifyDroneEvent(DroneEventsType.BEACON_LOCK_START);
			RunThread();
		}
		else {
			KillThread();
			drone.notifyDroneEvent(DroneEventsType.BEACON_LOCK_FINISH);
		}
	}
	
	public boolean isActive() {
		return pIsActive;
	}
	
	public void RunThread() {
		logger.LogGeneralMessege("Start Beacon Thread");
		if (pFollowThread != null) {
			logger.LogGeneralMessege("Terminate existing beacon thread");
			pFollowThread.cancel(true);
		}
		
		pFollowThread = new SwingWorker<Void, Void>(){
			
			@Override
   			protected Void doInBackground() throws Exception {
				loggerDisplayerSvc.logGeneral("Beacon Started");
				loggerDisplayerSvc.logGeneral("Beacon Mainloop Started");
				Coordinate tmpPos = null;
				boolean started = false;
				while (true) {
					Thread.sleep(1000);
					if (!drone.getGps().isPositionValid()) {
						loggerDisplayerSvc.logError("Drone doesn't have GPS location, skipp request to follow beacon");
						continue;
					}
					
					syncBeacon();

					// tmpPos != null mark that we are in the first iteration of the loop
					if (started && !GuidedPoint.isGuidedMode(drone)) {
						loggerDisplayerSvc.logError("Quad is must be in guided mode to follow beacon, operation canceled");
						drone.notifyDroneEvent(DroneEventsType.FOLLOW_STOP);
						started = false;
						break;
					}
					
					if (tmpPos == pLastPosition) {
						loggerDisplayerSvc.logGeneral("Same beacon position");
						continue;
					}
					
					drone.getGuidedPoint().forcedGuidedCoordinate(pLastPosition.dot(1));
					tmpPos = pLastPosition;
					started = true;
					loggerDisplayerSvc.logGeneral("Update Beacon position was sent to quad");
				}
				loggerDisplayerSvc.logGeneral("Beacon Mainloop Finished");
				return null;
			}
			
			@Override
            protected void done() {
				loggerDisplayerSvc.logGeneral("Beacon Thread Finish");
			}
		};
		
		pFollowThread.execute();
	}
	
	public void KillThread() {
		logger.LogGeneralMessege("Stop Beacon Thread");
		if (pFollowThread != null) {
			logger.LogGeneralMessege("Terminate existing beacon thread");
			pFollowThread.cancel(true);
		}
		logger.LogGeneralMessege("Change mode to Position Hold");
		drone.getState().changeFlightMode(ApmModes.ROTOR_POSHOLD);
		
		pFollowThread = null;
	}

	public void syncBeacon() {
		loggerDisplayerSvc.logGeneral("Sync Beacon");
		BeaconData beaconData = BeaconData.fetch();
		if (beaconData == null) {
			loggerDisplayerSvc.logError("Failed to get beacon point from the web");
			return;
		}
		setPosition(beaconData.getCoordinate());
	}
}
