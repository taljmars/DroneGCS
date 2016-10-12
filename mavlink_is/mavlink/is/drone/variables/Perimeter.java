package mavlink.is.drone.variables;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import gui.is.Coordinate;
import gui.is.interfaces.ICoordinate;
import gui.is.interfaces.MapPolygon;
import gui.is.services.LoggerDisplayerSvc;
import logger.Logger;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.geoTools.GeoTools;

@Component("perimeter")
public class Perimeter  extends DroneVariable implements Serializable {

	private static final long serialVersionUID = -7429107483849276132L;
	private MapPolygon pPolygon;
	private boolean pEnforce;
	private boolean pAlertOnly;
	private Coord2D pLastPosition = null;
	private Coord2D pLastPositionInPerimeter = null;
	private ApmModes pMode;
	private boolean pEnforcePermeterRunning = false;
	
	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	static int called;
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		pEnforce = false;
		pAlertOnly = false;
		pPolygon = null;
		pMode = drone.getState().getMode();
	}
	
	public void setPolygon(MapPolygon perimeterPoly) {
		pPolygon = perimeterPoly;
		drone.notifyDroneEvent(DroneEventsType.PERIMETER_RECEIVED);
	}
	
	public void setPosition(Coord2D position) {
		pLastPosition = position;
		
		if (pEnforce) {
			if (!isContained()) {
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
						Logger.LogErrorMessege(e.toString());
						e.printStackTrace();
					}
				}
			}
			else {
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
	
	public boolean isContained() {
		if (pPolygon == null || pLastPosition == null)
			return true;
		
		boolean res = pPolygon.contains(pLastPosition.convertToCoordinate());
		if (res) {
			pLastPositionInPerimeter = pLastPosition;
		}
		return res;
	}
	
	public Coord2D getClosestPointOnPerimeterBorder() {
		if (pLastPositionInPerimeter == null) {
			loggerDisplayerSvc.logGeneral("Last position not exist, return closest corner in it");
			return getClosestCornerInPoligon();
		}
		
		return pLastPositionInPerimeter;
	}
	
	public Coord2D getClosestCornerInPoligon() {
		Coordinate closestPoint = new Coordinate(0,0);
		double min_dist = Integer.MAX_VALUE;
		
		List<? extends ICoordinate> lst = pPolygon.getPoints();
		for (int i = 0 ; i < lst.size() ; i++) {
			Coordinate corner = (Coordinate) lst.get(i);
			
			double val = GeoTools.getDistance(pLastPosition, corner.ConvertToCoord2D()).valueInMeters();
			if (val < min_dist) {
				min_dist = val;
				closestPoint = corner;
			}
		}
		
		return closestPoint.ConvertToCoord2D();
	}

	public boolean isAlertOnly() {
		return pAlertOnly;
	}

	public boolean isEnforce() {
		return pEnforce;
	}
}
