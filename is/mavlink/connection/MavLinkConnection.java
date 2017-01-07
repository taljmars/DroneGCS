package mavlink.connection;

import logger.Logger;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;

import gui.services.LoggerDisplayerSvc;
import mavlink.drone.Drone;
import mavlink.drone.DroneInterfaces.DroneEventsType;
import mavlink.protocol.msg_metadata.MAVLinkMessage;
import mavlink.protocol.msg_metadata.MAVLinkPacket;
import mavlink.protocol.msg_metadata.ardupilotmega.msg_heartbeat;
import mavlink.protocol.msgparser.Parser;

/**
 * Base for mavlink connection implementations.
 */
@ComponentScan("tools.logger")
public abstract class MavLinkConnection {

	@SuppressWarnings("unused")
	private static final String TAG = MavLinkConnection.class.getSimpleName();
	
	@Resource(name = "loggerDisplayerSvc")
	@NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "drone")
	@NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Resource(name = "logger")
	@NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;

	/*
	 * MavLink connection states
	 */
	private static final int MAVLINK_DISCONNECTED = 0;
	private static final int MAVLINK_CONNECTING = 1;
	private static final int MAVLINK_CONNECTED = 2;

	/**
	 * Size of the buffer used to read messages from the mavlink connection.
	 */
	private static final int READ_BUFFER_SIZE = 4096*4;

	/**
	 * Maximum possible sequence number for a packet.
	 */
	private static final int MAX_PACKET_SEQUENCE = 255;

	/**
	 * Set of listeners subscribed to this mavlink connection. We're using a
	 * ConcurrentSkipListSet because the object will be accessed from multiple
	 * threads concurrently.
	 */
	private final ConcurrentHashMap<String, MavLinkConnectionListener> mListeners = new ConcurrentHashMap<String, MavLinkConnectionListener>();

	/**
	 * Queue the set of packets to send via the mavlink connection. A thread
	 * will be blocking on it until there's element(s) available to send.
	 */
	private final LinkedBlockingQueue<MAVLinkPacket> mPacketsToSend = new LinkedBlockingQueue<MAVLinkPacket>();

	private final AtomicInteger mConnectionStatus = new AtomicInteger(MAVLINK_DISCONNECTED);

	/**
	 * Listen for incoming data on the mavlink connection.
	 */
	private final Runnable mConnectingTask = new Runnable() {

		@Override
		public void run() {
			System.out.println("Starting Connection Tasks Thread");
			Thread sendingThread = null; 

			// Load the connection specific preferences
			loadPreferences();

			try {
				// Open the connection
				if (!openConnection()) {
					loggerDisplayerSvc.logError("Failed to open connection");
					drone.notifyDroneEvent(DroneEventsType.DISCONNECTED);
					return;
				}
				
				mConnectionStatus.set(MAVLINK_CONNECTED);
				reportConnect();

				// Launch the 'Sending' thread
				sendingThread = new Thread(mSendingTask, "MavLinkConnection-Sending Thread");
				sendingThread.start();

				final Parser parser = new Parser();
				parser.stats.mavlinkResetStats();

				final byte[] readBuffer = new byte[READ_BUFFER_SIZE];

				drone.notifyDroneEvent(DroneEventsType.CONNECTED);
				
				while (mConnectionStatus.get() == MAVLINK_CONNECTED) {
					int bufferSize = readDataBlock(readBuffer);
					handleData(parser, bufferSize, readBuffer);
				}
			} 
			catch (IOException e) {
				// Ignore errors while shutting down
				if (mConnectionStatus.get() != MAVLINK_DISCONNECTED) {
					reportComError(e.getMessage());
				}
			}
			finally {
				if (sendingThread != null && sendingThread.isAlive()) {
					sendingThread.interrupt();
				}

				logger.LogDesignedMessege("Connection Thread finished");
				drone.notifyDroneEvent(DroneEventsType.DISCONNECTED);
				disconnect();
			}
		}

		private void handleData(Parser parser, int bufferSize, byte[] buffer) {
			if (bufferSize < 1) {
				return;
			}

			for (int i = 0; i < bufferSize; i++) {
				MAVLinkPacket receivedPacket = parser.mavlink_parse_char(buffer[i] & 0x00ff);
				if (receivedPacket != null) {
					MAVLinkMessage msg = receivedPacket.unpack();
					reportReceivedMessage(msg);
				}
			}
		}
	};

	/**
	 * Blocks until there's packet(s) to send, then dispatch them.
	 */
	private final Runnable mSendingTask = new Runnable() {
		@Override
		public void run() {
			System.out.println("Starting Sending Tasks Thread");
			int msgSeqNumber = 0;

			try {
				while (mConnectionStatus.get() == MAVLINK_CONNECTED) {
					final MAVLinkPacket packet = mPacketsToSend.take();
					if (packet.unpack().msgid != msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT) {
//						System.err.println("[SND] " + packet.unpack().toString());
						String log_entry = Logger.generateDesignedMessege(packet.unpack().toString(), Logger.Type.OUTGOING, false);
						logger.LogDesignedMessege(log_entry);
					}
					packet.seq = msgSeqNumber;
					byte[] buffer = packet.encodePacket();

					try {
						sendBuffer(buffer);
					} catch (IOException e) {
						reportComError(e.getMessage());
					}

					msgSeqNumber = (msgSeqNumber + 1) % (MAX_PACKET_SEQUENCE + 1);
				}
				loggerDisplayerSvc.logError("Mavlink was not connected");
			} catch (InterruptedException e) {
				logger.LogErrorMessege("Interrupted exception:");
				logger.LogErrorMessege(e.getMessage());
			} finally {
				logger.LogDesignedMessege("Sending Thread finished");
				disconnect();
			}
		}
	};
	
	private Thread mConnectingThread;
	
	/**
	 * Establish a mavlink connection. If the connection is successful, it will
	 * be reported through the MavLinkConnectionListener interface.
	 */
	public void connect() {
		if (mConnectionStatus.compareAndSet(MAVLINK_DISCONNECTED, MAVLINK_CONNECTING)) {
			mConnectingThread = new Thread(mConnectingTask, "MavLinkConnection-Connecting Thread");
			mConnectingThread.start();
		}
	}

	/**
	 * Disconnect a mavlink connection. If the operation is successful, it will
	 * be reported through the MavLinkConnectionListener interface.
	 */
	public void disconnect() {		
		if (mConnectionStatus.get() == MAVLINK_DISCONNECTED || mConnectingThread == null) {
			return;
		}

		try {
			mConnectionStatus.set(MAVLINK_DISCONNECTED);
			if (mConnectingThread.isAlive() && !mConnectingThread.isInterrupted()) {
				mConnectingThread.interrupt();
			}

			closeConnection();
			reportDisconnect();
		} catch (IOException e) {
			logger.LogErrorMessege(e.getMessage());
			reportComError(e.getMessage());
		}
	}

	private int getConnectionStatus() {
		return mConnectionStatus.get();
	}

	public void sendMavPacket(MAVLinkPacket packet) {
		if (!mPacketsToSend.offer(packet))
			loggerDisplayerSvc.logError("Unable to send mavlink packet. Packet queue is full!");
	}

	/**
	 * Adds a listener to the mavlink connection.
	 * 
	 * @param listener
	 * @param tag
	 *            Listener tag
	 */
	public void addMavLinkConnectionListener(String tag, MavLinkConnectionListener listener) {
		mListeners.put(tag, listener);

		if (getConnectionStatus() == MAVLINK_CONNECTED) {
			listener.onConnect();
		}
	}

	/**
	 * Removes the specified listener.
	 * 
	 * @param tag
	 *            Listener tag
	 */
	public void removeMavLinkConnectionListener(String tag) {
		mListeners.remove(tag);
	}

	protected abstract boolean openConnection() throws IOException;

	protected abstract int readDataBlock(byte[] buffer) throws IOException;

	protected abstract void sendBuffer(byte[] buffer) throws IOException;

	protected abstract boolean closeConnection() throws IOException;

	protected abstract void loadPreferences();

	/**
	 * @return The type of this mavlink connection.
	 */
	protected abstract int getConnectionType();

	/**
	 * Utility method to notify the mavlink listeners about communication
	 * errors.
	 * 
	 * @param errMsg
	 */
	private void reportComError(String errMsg) {
		if (mListeners.isEmpty())
			return;

		for (MavLinkConnectionListener listener : mListeners.values()) {
			listener.onComError(errMsg);
		}
	}

	/**
	 * Utility method to notify the mavlink listeners about a successful
	 * connection.
	 */
	private void reportConnect() {
		for (MavLinkConnectionListener listener : mListeners.values()) {
			listener.onConnect();
		}
	}

	/**
	 * Utility method to notify the mavlink listeners about a connection
	 * disconnect.
	 */
	private void reportDisconnect() {
		if (mListeners.isEmpty())
			return;

		for (MavLinkConnectionListener listener : mListeners.values()) {
			listener.onDisconnect();
		}
	}

	/**
	 * Utility method to notify the mavlink listeners about received messages.
	 * 
	 * @param msg
	 *            received mavlink message
	 */
	private void reportReceivedMessage(MAVLinkMessage msg) {
		if (mListeners.isEmpty())
			return;

		for (MavLinkConnectionListener listener : mListeners.values()) {
			listener.onReceiveMessage(msg);
		}
	}
	
	public boolean isConnected() {
		return mConnectionStatus.get() == MAVLINK_CONNECTED;
	}
}
