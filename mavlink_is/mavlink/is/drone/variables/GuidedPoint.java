package mavlink.is.drone.variables;

import org.springframework.stereotype.Component;

import gui.is.Coordinate;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.protocol.msgbuilder.MavLinkModes;
import mavlink.is.protocol.msgbuilder.MavLinkTakeoff;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.units.Altitude;

@Component("guidedPoint")
public class GuidedPoint extends DroneVariable implements OnDroneListener {


	private static final long serialVersionUID = 9146528390763622987L;
	private GuidedStates state = GuidedStates.UNINITIALIZED;
	private Coord2D coord = new Coord2D(0, 0);
	private Altitude altitude = new Altitude(0.0);

    private Runnable mPostInitializationTask;

	private enum GuidedStates {
		UNINITIALIZED, IDLE, ACTIVE
	}

	public void init() {
		drone.addDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			if (isGuidedMode(drone)) {
				initialize();
			} else {
				disable();
			}
			break;

		case DISCONNECTED:
		case HEARTBEAT_TIMEOUT:
			disable();

        default:
			break;
		}
	}

    public static boolean isGuidedMode(Drone drone){
        final int droneType = drone.getType();
        final ApmModes droneMode = drone.getState().getMode();

        if(Type.isCopter(droneType)){
            return droneMode == ApmModes.ROTOR_GUIDED;
        }

        if(Type.isPlane(droneType)){
            return droneMode == ApmModes.FIXED_WING_GUIDED;
        }

        return false;
    }

	public void pauseAtCurrentLocation() {
		if (state !=GuidedStates.ACTIVE) {
			changeToGuidedMode(drone);
		}else{
			newGuidedCoord(drone.getGps().getPosition());
		}
	}

    public static void changeToGuidedMode(Drone drone){
        final State droneState = drone.getState();
        final int droneType = drone.getType();
        if(Type.isCopter(droneType)){
            droneState.changeFlightMode(ApmModes.ROTOR_GUIDED);
        }
        else if(Type.isPlane(droneType)){
            //You have to send a guided point to the plane in order to trigger guided mode.
            forceSendGuidedPoint(drone, drone.getGps().getPosition(),
                    getDroneAltConstrained(drone));
        }
    }

	public void doGuidedTakeoff(Altitude alt) {
		doGuidedTakeoff(null, alt);
	}
	
	public void doGuidedTakeoff(Coordinate target_coord, Altitude alt) {
        if(Type.isCopter(drone.getType())) {
            coord = drone.getGps().getPosition();
            altitude.set(alt.valueInMeters());
            state = GuidedStates.IDLE;
            changeToGuidedMode(drone);
            if (target_coord != null)
            	MavLinkTakeoff.sendTakeoff(drone, target_coord, alt);
            else
            MavLinkTakeoff.sendTakeoff(drone, alt);
            drone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
        }
	}

	public void newGuidedCoord(Coord2D coord) {
		changeCoord(coord);
	}

	public void changeGuidedAltitude(double alt) {
		changeAlt(alt);
	}

	public void forcedGuidedCoordinate(final Coord2D coord) throws Exception {
		if ((drone.getGps().getFixTypeNumeric() != GPS.LOCK_3D)) {
			throw new Exception("Bad GPS for guided");
		}

        if(isInitialized()) {
            changeCoord(coord);
        }
        else{
            mPostInitializationTask = new Runnable() {
                @Override
                public void run() {
                    changeCoord(coord);
                }
            };

            changeToGuidedMode(drone);
        }
	}

	private void initialize() {
		if (state == GuidedStates.UNINITIALIZED) {
			coord = drone.getGps().getPosition();
			altitude.set(getDroneAltConstrained(drone));
			state = GuidedStates.IDLE;
			drone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
		}

        if(mPostInitializationTask != null){
            mPostInitializationTask.run();
            mPostInitializationTask = null;
        }
	}

	private void disable() {
		state = GuidedStates.UNINITIALIZED;
		drone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
	}

    private void changeAlt(double alt) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/

            case ACTIVE:
                altitude.set(Math.max(alt, getMinAltitude(drone)));
                sendGuidedPoint();
                break;
        }
    }

	private void changeCoord(Coord2D coord) {
		switch (state) {
		case UNINITIALIZED:
			break;

		case IDLE:
			state = GuidedStates.ACTIVE;
            /** FALL THROUGH **/
		case ACTIVE:
			this.coord = coord;
			sendGuidedPoint();
			break;
		}
	}

	private void sendGuidedPoint() {
		if (state == GuidedStates.ACTIVE) {
            forceSendGuidedPoint(drone, coord, altitude.valueInMeters());
		}
	}

    public static void forceSendGuidedPoint(Drone drone, Coord2D coord, double altitudeInMeters){
        drone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
        MavLinkModes.setGuidedMode(drone, coord.getLat(), coord.getLng(), altitudeInMeters);
    }

	private static double getDroneAltConstrained(Drone drone) {
		double alt = Math.floor(drone.getAltitude().getAltitude());
		return Math.max(alt, getMinAltitude(drone));
	}

	public Coord2D getCoord() {
		return coord;
	}

	public Altitude getAltitude() {
		return this.altitude;
	}

	public boolean isActive() {
		return (state == GuidedStates.ACTIVE);
	}
	
	public boolean isIdle() {
		return (state == GuidedStates.IDLE);
	}

	public boolean isInitialized() {
		return !(state == GuidedStates.UNINITIALIZED);
	}

    public static float getMinAltitude(Drone drone){
        final int droneType = drone.getType();
        if(Type.isCopter(droneType)){
            return 2f;
        }
        else if(Type.isPlane(droneType)){
            return 15f;
        }
        else{
            return 0f;
        }
    }

    public void newGuidedVelocity( double xVel, double yVel, double zVel){
			MavLinkModes.sendGuidedVelocity(drone,xVel,yVel,zVel);
	}

}
