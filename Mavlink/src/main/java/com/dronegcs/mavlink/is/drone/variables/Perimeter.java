package is.drone.variables;

import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import com.dronegcs.gcsis.logger.Logger;
import is.drone.DroneVariable;
import is.drone.DroneInterfaces.DroneEventsType;
import is.protocol.msg_metadata.ApmModes;
import geoTools.Coordinate;

@ComponentScan("tools.com.dronegcs.gcsis.logger")
@Component
public class Perimeter extends DroneVariable {

	private Compound pCompound;
	private boolean pEnforce;
	private boolean pAlertOnly;
	private Coordinate pLastPosition = null;
	private Coordinate pLastPositionInPerimeter = null;
	private ApmModes pMode;
	private boolean pEnforcePermeterRunning = false;

	@Autowired @NotNull(message = "Internal Error: Failed to get com.dronegcs.gcsis.logger")
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
							logger.LogErrorMessege("Changing flight from " + pMode.getName() + " to " + ApmModes.ROTOR_GUIDED.getName() + " (Enforcing perimeter)");
							drone.getGuidedPoint().forcedGuidedCoordinate(getClosestPointOnPerimeterBorder());
							pEnforcePermeterRunning = true;
						}
					} catch (Exception e) {
						logger.LogErrorMessege(e.toString());
						logger.LogErrorMessege("Error occur while changing flight mode; " + e.getMessage());
					}
				}
			}
			else {
				pLastPositionInPerimeter = pLastPosition;
				if (pEnforcePermeterRunning) {
					logger.LogErrorMessege("Changing flight from " + ApmModes.ROTOR_GUIDED.getName() + " back to " + pMode.getName());
					drone.getState().changeFlightMode(pMode);
					pEnforcePermeterRunning = false;
				}
				
			}
		}
	}
	
	public void setEnforce(boolean enforce) {
		if (enforce)
			logger.LogGeneralMessege("Enable Perimeter enforcement");
		else
			logger.LogGeneralMessege("Disable Perimeter enforcement");
		pEnforce = enforce;
	}
	
	public void setAlertOnly(boolean alert) {
		if (alert)
			logger.LogGeneralMessege("Enable perimeter alert only");
		else
			logger.LogGeneralMessege("Disable perimeter alert only");
		pAlertOnly = alert;
	}
	
	public Coordinate getClosestPointOnPerimeterBorder() {
		if (pLastPositionInPerimeter == null) {
			logger.LogGeneralMessege("Last position not exist, return closest corner in it");
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
