package mavlink.core.connection;

import gui.is.services.LoggerDisplayerSvc;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import mavlink.is.connection.MavLinkConnection;
import mavlink.is.connection.MavLinkConnectionTypes;
import mavlink.is.drone.Drone;
import communication_device.TwoWaySerialComm;

/**
 * Provides support for mavlink connection via udp.
 */
@Component("radioConnection")
public class RadioConnection extends MavLinkConnection {

	@Resource(name = "twoWaySerialComm")
	private TwoWaySerialComm socket;
	
	@Resource(name = "droneUpdateListener")
	private DroneUpdateListener droneUpdateListener;
	
	@Resource(name = "drone")
	private Drone drone;
	
	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;

	@Override
	public final void closeConnection() throws IOException {
	}

	@Override
	public final void openConnection() throws IOException {
		System.err.println("openConnection");
		socket.connect();
	}

	@Override
	public final void sendBuffer(byte[] buffer) throws IOException {
		try {
			if (socket != null) { // We can't send to our sister until they
				// have connected to us
				socket.out.write(buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public final int readDataBlock(byte[] readData) throws IOException {
		if (socket == null)
			return -1;
		return socket.read(readData, readData.length);
	}

	@Override
	public final void loadPreferences() {
		addMavLinkConnectionListener("Drone", droneUpdateListener);
	}

	@Override
	public final int getConnectionType() {
		return MavLinkConnectionTypes.MAVLINK_CONNECTION_RADIO;
	}

	protected int loadServerPort(){return 1;}


	@Override
	protected File getTempTLogFile() {
		return null;
	}

	@Override
	protected void commitTempTLogFile(File tlogFile) {
		
	}
}
