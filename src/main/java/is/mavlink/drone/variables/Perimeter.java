package is.mavlink.drone.variables;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import is.gui.services.DialogManagerSvc;
import is.gui.services.LoggerDisplayerSvc;
import is.logger.Logger;
import is.mavlink.drone.DroneVariable;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.protocol.msg_metadata.ApmModes;
import tools.geoTools.Coordinate;

@ComponentScan("tools.logger")
@Component("perimeter")
public class Perimeter extends DroneVariable {

	private Compound pCompound;
	private boolean pEnforce;
	private boolean pAlertOnly;
	private Coordinate pLastPosition = null;
	private Coordinate pLastPositionInPerimeter = null;
	private ApmModes pMode;
	private boolean pEnforcePermeterRunning = false;
	
	@Resource(name = "loggerDisplayerSvc")
	@NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "dialogManagerSvc")
	@NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	@Resource(name = "logger")
	@NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;
	
	static int called;
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		pEnforce = false;
		pAlertOnly = false;
		pCompound = null;
		pMode = drone.getState().getMode();
	}
	
	public void setCompound(Compound compound) {
		pCompound = compound;
		drone.notifyDroneEvent(DroneEventsType.PERIMETER_RECEIVED);
	}
	
	public void setPosition(Coordinate position) {
		pLastPosition = position;
		
		if (pEnforce) {
			if (pLastPosition != null && !pCompound.isContained(position)) {
				drone.notifyDroneEvent(DroneEventsType.LEFT_PERIMETER);
				if (!pAlertOnly && drone.getState().isFlying()) {
					drone.notifyDroneEvent(DroneEventsType.ENFORCING_PERIMETER);
					try {
						if (!pEnforcePermeterRunning) {
							pMode = drone.getState().getMode();							
							loggerDisplayerSvc.logError("Changing flight from " + pMode.getName() + " to " + ApmModes.ROTOR_GUIDED.getName() + " (Enforcing perimeter)");
							drone.getGuidedPoint().forcedGuidedCoordinate(getClosestPointOnPerimeterBorder());
							pEnforcePermeterRunning = true;
						}
					} catch (Exception e) {
						logger.LogErrorMessege(e.toString());
						dialogManagerSvc.showErrorMessageDialog("Error occur while changing flight mode", e);
					}
				}
			}
			else {
				pLastPositionInPerimeter = pLastPosition;
				if (pEnforcePermeterRunning) {
					loggerDisplayerSvc.logError("Changing flight from " + ApmModes.ROTOR_GUIDED.getName() + " back to " + pMode.getName());
					drone.getState().changeFlightMode(pMode);
					pEnforcePermeterRunning = false;
				}
				
			}
		}
	}
	
	public void setEnforce(boolean enforce) {
		if (enforce)
			loggerDisplayerSvc.logGeneral("Enable Perimeter enforcement");
		else
			loggerDisplayerSvc.logGeneral("Disable Perimeter enforcement");
		pEnforce = enforce;
	}
	
	public void setAlertOnly(boolean alert) {
		if (alert)
			loggerDisplayerSvc.logGeneral("Enable perimeter alert only");
		else
			loggerDisplayerSvc.logGeneral("Disable perimeter alert only");
		pAlertOnly = alert;
	}
	
	public Coordinate getClosestPointOnPerimeterBorder() {
		if (pLastPositionInPerimeter == null) {
			loggerDisplayerSvc.logGeneral("Last position not exist, return closest corner in it");
			return getClosestCornerInPoligon();
		}
		
		return pLastPositionInPerimeter;
	}
	
	public Coordinate getClosestCornerInPoligon() {
		return pCompound.getClosestPointOnEdge(pLastPosition);
	}

	public boolean isAlertOnly() {
		return pAlertOnly;
	}

	public boolean isEnforce() {
		return pEnforce;
	}
}
