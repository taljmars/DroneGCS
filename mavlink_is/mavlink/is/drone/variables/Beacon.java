package mavlink.is.drone.variables;

import gui.core.dashboard.Dashboard;

import java.io.Serializable;

import logger.Logger;
import mavlink.core.connection.helper.BeaconData;
import mavlink.core.drone.MyDroneImpl;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.utils.coordinates.Coord3D;
import javax.swing.SwingWorker;

public class Beacon extends DroneVariable implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9030954282536999192L;
	
	private boolean pIsActive;
	private Coord3D pLastPosition;
	private transient SwingWorker<Void, Void> pFollowThread = null;

	public Beacon(MyDroneImpl myDroneImpl) {
		super(myDroneImpl);
		pIsActive = false;
		pLastPosition = null;
	}
	
	public void setPosition(Coord3D coord) {
		pLastPosition = coord;
		myDrone.notifyDroneEvent(DroneEventsType.BEACON_BEEP);
	}
	
	public Coord3D getPosition() {
		return pLastPosition;
	}
	
	public void setActive(boolean should_be_active) {
		pIsActive = should_be_active;
		if (pIsActive) {
			myDrone.notifyDroneEvent(DroneEventsType.BEACON_LOCK_START);
			RunThread();
		}
		else {
			KillThread();
			myDrone.notifyDroneEvent(DroneEventsType.BEACON_LOCK_FINISH);
		}
	}
	
	public boolean isActive() {
		return pIsActive;
	}
	
	public void RunThread() {
		Logger.LogGeneralMessege("Start Beacon Thread");
		if (pFollowThread != null) {
			Logger.LogGeneralMessege("Terminate existing beacon thread");
			pFollowThread.cancel(true);
		}
		
		pFollowThread = new SwingWorker<Void, Void>(){
			
			@Override
   			protected Void doInBackground() throws Exception {
				Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Beacon Started");
				Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Beacon Mainloop Started");
				Coord3D tmpPos = null;
				boolean started = false;
				while (true) {
					Thread.sleep(1000);
					if (!myDrone.getGps().isPositionValid()) {
						Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("Drone doesn't have GPS location, skipp request to follow beacon");
						continue;
					}
					
					syncBeacon();

					// tmpPos != null mark that we are in the first iteration of the loop
					if (started && !GuidedPoint.isGuidedMode(myDrone)) {
						Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("Quad is must be in guided mode to follow beacon, operation canceled");
						Dashboard.window.btnFollowBeaconStart.setSelected(false);
						started = false;
						break;
					}
					
					if (tmpPos == pLastPosition) {
						Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Same beacon position");
						continue;
					}
					
					myDrone.getGuidedPoint().forcedGuidedCoordinate(pLastPosition.dot(1));
					tmpPos = pLastPosition;
					started = true;
					Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Update Beacon position was sent to quad");
				}
				Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Beacon Mainloop Finished");
				return null;
			}
			
			@Override
            protected void done() {
				Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Beacon Thread Finish");
			}
		};
		
		pFollowThread.execute();
	}
	
	public void KillThread() {
		Logger.LogGeneralMessege("Stop Beacon Thread");
		if (pFollowThread != null) {
			Logger.LogGeneralMessege("Terminate existing beacon thread");
			pFollowThread.cancel(true);
		}
		Logger.LogGeneralMessege("Change mode to Position Hold");
		myDrone.getState().changeFlightMode(ApmModes.ROTOR_POSHOLD);
		
		pFollowThread = null;
	}

	public void syncBeacon() {
		Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Sync Beacon");
		BeaconData beaconData = BeaconData.fetch();
		if (beaconData == null) {
			Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("Failed to get beacon point from the web");
			return;
		}
		setPosition(beaconData.getCoordinate());
	}
}
