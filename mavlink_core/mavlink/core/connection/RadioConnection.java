package mavlink.core.connection;

import gui.is.services.LoggerDisplayerSvc;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import tools.antenna_device.TwoWaySerialComm;
import mavlink.is.connection.MavLinkConnection;
import mavlink.is.connection.MavLinkConnectionTypes;
import mavlink.is.drone.Drone;

/**
 * Provides support for mavlink connection via udp.
 */
@ComponentScan("communication_device")
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
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}

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
				socket.getOutputStream().write(buffer);
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
