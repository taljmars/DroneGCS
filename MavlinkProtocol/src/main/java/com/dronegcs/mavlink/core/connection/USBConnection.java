package com.dronegcs.mavlink.core.connection;

import com.dronegcs.mavlink.is.connection.MavLinkConnection;
import com.dronegcs.mavlink.is.connection.MavLinkConnectionTypes;
import com.generic_tools.devices.SerialConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Provides support for com.dronegcs.mavlink.is.mavlink connection via udp.
 */
@Component
public class USBConnection extends MavLinkConnection {

	@Autowired
	private SerialConnection serialConnection;
	
	@Autowired
	private DroneUpdateListener droneUpdateListener;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
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
