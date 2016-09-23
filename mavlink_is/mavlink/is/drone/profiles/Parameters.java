package mavlink.is.drone.profiles;

import gui.is.services.LoggerDisplayerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.Handler;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.drone.parameters.Parameter;
import mavlink.is.protocol.msg_metadata.MAVLinkMessage;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_param_value;
import mavlink.is.protocol.msgbuilder.MavLinkParameters;

/**
 * Class to manage the communication of parameters to the MAV.
 * 
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 * 
 */
public class Parameters extends DroneVariable implements OnDroneListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2394143828053379768L;

	private static final int TIMEOUT = 2000; //TALMA original is 1000;

	private int expectedParams;

	private final HashMap<Integer, Parameter> parameters = new HashMap<Integer, Parameter>();

	private DroneInterfaces.OnParameterManagerListener parameterListener;

	public Handler watchdog;
	public Runnable watchdogCallback = () -> onParameterStreamStopped();

	public final ArrayList<Parameter> parameterList = new ArrayList<Parameter>();

	public Parameters(Drone myDrone, Handler handler) {
		super(myDrone);
		this.watchdog = handler;
		myDrone.addDroneListener(this);
	}

	public void refreshParameters() {
		parameters.clear();
        parameterList.clear();
        
        myDrone.notifyDroneEvent(DroneEventsType.PARAMETERS_DOWNLOAD_START);

		if (parameterListener != null)
			parameterListener.onBeginReceivingParameters();
		
		MavLinkParameters.requestParametersList(myDrone);
		resetWatchdog();
	}

    public List<Parameter> getParametersList(){
        return parameterList;
    }
    
    public int getLoadedDownloadedParameters() {
    	return parameters.size();
    }

	/**
	 * Try to process a Mavlink message if it is a parameter related message
	 * 
	 * @param msg
	 *            Mavlink message to process
	 * @return Returns true if the message has been processed
	 */
	public boolean processMessage(MAVLinkMessage msg) {
		if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
			processReceivedParam((msg_param_value) msg);
			return true;
		}
		return false;
	}

	private void processReceivedParam(msg_param_value m_value) {
		// collect params in parameter list
		Parameter param = new Parameter(m_value);
		//System.err.println("Param Name: " + param.name + "id " + parameters.size() + " total " + m_value.param_count);
		parameters.put((int) m_value.param_index, param);

		expectedParams = m_value.param_count;

		// update listener
		if (parameterListener != null)
			parameterListener.onParameterReceived(param, m_value.param_index, m_value.param_count);
		
		// Are all parameters here? Notify the listener with the parameters
		if (parameters.size() >= m_value.param_count) {
            parameterList.clear();
			for (int key : parameters.keySet()) {
				parameterList.add(parameters.get(key));
			}
			killWatchdog();
			LoggerDisplayerManager.addGeneralMessegeToDisplay("Parameters finished!");
			myDrone.notifyDroneEvent(DroneEventsType.PARAMETERS_DOWNLOADED_FINISH);

			if (parameterListener != null) {
				parameterListener.onEndReceivingParameters(parameterList);
			}
		} else {
			resetWatchdog();
		}
		myDrone.notifyDroneEvent(DroneEventsType.PARAMETER);
	}

	private void reRequestMissingParams(int howManyParams) {
		for (int i = 0; i < howManyParams; i++) {
			if (!parameters.containsKey(i)) {
				MavLinkParameters.readParameter(myDrone, i);
			}
		}
	}

	public void sendParameter(Parameter parameter) {
		MavLinkParameters.sendParameter(myDrone, parameter);
	}

	public void ReadParameter(String name) {
		MavLinkParameters.readParameter(myDrone, name);
	}

	public Parameter getParameter(String name) {
		for (int key : parameters.keySet()) {
			if (parameters.get(key).name.equalsIgnoreCase(name))
				return parameters.get(key);
		}
		return null;
	}

	public Parameter getLastParameter() {
		if (parameters.size() > 0)
			return parameters.get(parameters.size() - 1);

		return null;
	}

	private void onParameterStreamStopped() {
		reRequestMissingParams(expectedParams);
		resetWatchdog();
	}

	private void resetWatchdog() {
		watchdog.removeCallbacks(watchdogCallback);
		watchdog.postDelayed(watchdogCallback, TIMEOUT);
	}

	private void killWatchdog() {
		watchdog.removeCallbacks(watchdogCallback);
	}

	static int i = 0 ;
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case HEARTBEAT_FIRST:
			if (!drone.getState().isFlying()) {
				System.out.println(getClass().getName() + " First HB Packet");
				refreshParameters();
			}
			break;
		case DISCONNECTED:
		case HEARTBEAT_TIMEOUT:
			killWatchdog();
			break;
		default:
			break;

		}
	}
	
	public int getExpectedParameterAmount() {
		return expectedParams;
	}

	public void setParameterListener(DroneInterfaces.OnParameterManagerListener parameterListener) {
		this.parameterListener = parameterListener;
	}
}