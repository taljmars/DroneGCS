package mavlink.core.connection;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import is.devices.SerialConnection;
import is.mavlink.connection.MavLinkConnection;
import is.mavlink.connection.MavLinkConnectionTypes;

/**
 * Provides support for mavlink connection via udp.
 */
@ComponentScan("tools.comm.internal")
@Component("usbConnection")
public class USBConnection extends MavLinkConnection {

	@Resource(name = "twoWaySerialComm")
	private SerialConnection serialConnection;
	
	@Resource(name = "droneUpdateListener")
	private DroneUpdateListener droneUpdateListener;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}

	@Override
	public boolean openConnection() throws IOException {
		System.err.println("openConnection");
		return serialConnection.connect();
	}
	
	@Override
	public boolean closeConnection() throws IOException {
		System.err.println(getClass().getName() + " closeConnection");
		return serialConnection.disconnect();
	}

	@Override
	public final void sendBuffer(byte[] buffer) throws IOException {
		try {
			if (serialConnection != null) { // We can't send to our sister until they
				// have connected to us
				serialConnection.write(buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public final int readDataBlock(byte[] readData) throws IOException {
		if (serialConnection == null)
			return -1;
		return serialConnection.read(readData, readData.length);
	}

	@Override
	public void loadPreferences() {
		addMavLinkConnectionListener("Drone", droneUpdateListener);
	}

	@Override
	public final int getConnectionType() {
		return MavLinkConnectionTypes.MAVLINK_CONNECTION_USB;
	}
}
