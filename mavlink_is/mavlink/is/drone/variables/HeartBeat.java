package mavlink.is.drone.variables;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.Handler;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_heartbeat;

@Component("heartbeat")
public class HeartBeat extends DroneVariable implements OnDroneListener {

	private static final long serialVersionUID = -1666657044309252476L;
	private static final long HEARTBEAT_NORMAL_TIMEOUT = 5000; //ms
	private static final long HEARTBEAT_LOST_TIMEOUT = 15000; //ms
    private static final long HEARTBEAT_IMU_CALIBRATION_TIMEOUT = 35000; //ms

	public static final int INVALID_MAVLINK_VERSION = -1;

	public HeartbeatState heartbeatState = HeartbeatState.FIRST_HEARTBEAT;
	public int droneID = 1;

	/**
	 * Stores the version of the mavlink protocol.
	 */
	private byte mMavlinkVersion = INVALID_MAVLINK_VERSION;

	public enum HeartbeatState {
		FIRST_HEARTBEAT, LOST_HEARTBEAT, NORMAL_HEARTBEAT, IMU_CALIBRATION
	}

	@Resource(name = "handler")
	private Handler handler;
	
	public final Runnable watchdogCallback = new Runnable() {
		@Override
		public void run() {
			onHeartbeatTimeout();
		}
	};
	
	public void init() {
		drone.addDroneListener(this);
	}

	/**
	 * @return the version of the mavlink protocol.
	 */
	public byte getMavlinkVersion() {
		return mMavlinkVersion;
	}

	@SuppressWarnings("incomplete-switch")
	public void onHeartbeat(msg_heartbeat msg) {
		droneID = msg.sysid;			
		mMavlinkVersion = msg.mavlink_version;

		System.out.println(getClass().getName() + " Currnet Status: " + heartbeatState);
		
		switch (heartbeatState) {
		case FIRST_HEARTBEAT:
			drone.notifyDroneEvent(DroneEventsType.HEARTBEAT_FIRST);
			break;
		case LOST_HEARTBEAT:
			drone.notifyDroneEvent(DroneEventsType.HEARTBEAT_RESTORED);
			break;
		}

		heartbeatState = HeartbeatState.NORMAL_HEARTBEAT;
		
		restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
	}

	public boolean isConnectionAlive() {
		//return heartbeatState != HeartbeatState.LOST_HEARTBEAT;
		return heartbeatState != HeartbeatState.LOST_HEARTBEAT && heartbeatState != HeartbeatState.FIRST_HEARTBEAT;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
            case CALIBRATION_IMU:
                //Set the heartbeat in imu calibration mode.
                heartbeatState = HeartbeatState.IMU_CALIBRATION;
                restartWatchdog(HEARTBEAT_IMU_CALIBRATION_TIMEOUT);
                break;

            case CONNECTED:
			notifyConnected();
			break;

		case DISCONNECTED:
			notifyDisconnected();
			break;

            default:
			break;
		}
	}

	private void notifyConnected() {
		System.err.println("HB Notification");
		restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
	}

	private void notifyDisconnected() {
		handler.removeCallbacks(watchdogCallback);
		heartbeatState = HeartbeatState.FIRST_HEARTBEAT;
		mMavlinkVersion = INVALID_MAVLINK_VERSION;
	}

	private void onHeartbeatTimeout() {
        switch(heartbeatState){
            case IMU_CALIBRATION:
                restartWatchdog(HEARTBEAT_IMU_CALIBRATION_TIMEOUT);
                drone.notifyDroneEvent(DroneEventsType.CALIBRATION_TIMEOUT);
                break;

            default:
                heartbeatState = HeartbeatState.LOST_HEARTBEAT;
                restartWatchdog(HEARTBEAT_LOST_TIMEOUT);
                drone.notifyDroneEvent(DroneEventsType.HEARTBEAT_TIMEOUT);
                break;
        }
	}

	private void restartWatchdog(long timeout) {
		// re-start watchdog
		handler.removeCallbacks(watchdogCallback);
		handler.postDelayed(watchdogCallback, timeout);
	}
}
