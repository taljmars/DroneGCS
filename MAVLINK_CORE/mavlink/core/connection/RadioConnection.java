package mavlink.core.connection;

import java.io.File;
import java.io.IOException;

import mavlink.is.connection.MavLinkConnection;
import mavlink.is.connection.MavLinkConnectionTypes;
import communication_device.TwoWaySerialComm;

/**
 * Provides support for mavlink connection via udp.
 */
public class RadioConnection extends MavLinkConnection {

	private TwoWaySerialComm socket = null;

	private void getRadioStream() throws IOException {
		socket = TwoWaySerialComm.get();
	}

	@Override
	public final void closeConnection() throws IOException {
		
	}

	@Override
	public final void openConnection() throws IOException {
		System.err.println("openConnection");
		getRadioStream();
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
		addMavLinkConnectionListener("Drone", new DroneUpdateListener());
		addMavLinkConnectionListener("GUI", new GuiListener());
	}

	@Override
	public final int getConnectionType() {
		return MavLinkConnectionTypes.MAVLINK_CONNECTION_RADIO;
	}

	protected int loadServerPort(){return 1;}

	/*@Override
	protected Logger initLogger() {
		// TODO Auto-generated method stub
		return null;
	}*/

	@Override
	protected File getTempTLogFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void commitTempTLogFile(File tlogFile) {
		// TODO Auto-generated method stub
		
	}
}
