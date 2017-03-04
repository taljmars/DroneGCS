package is.mavlink.connection;

import is.mavlink.protocol.msg_metadata.MAVLinkMessage;

/**
 * Provides updates about the mavlink connection.
 */
public interface MavLinkConnectionListener {

	/**
	 * Called when the mavlink connection is established.
	 */
	public void onConnect();

	/**
	 * Called when data is received via the mavlink connection.
	 * 
	 * @param msg
	 *            received data
	 */
	public void onReceiveMessage(MAVLinkMessage msg);

	/**
	 * Called when the mavlink connection is disconnected.
	 */
	public void onDisconnect();

	/**
	 * Provides information about communication error.
	 * 
	 * @param errMsg
	 *            error information
	 */
	public void onComError(String errMsg);

}
