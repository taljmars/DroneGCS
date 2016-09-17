package mavlink.is.protocol.msgbuilder;

import mavlink.is.protocol.msg_metadata.MAVLinkMessage;
import mavlink.is.protocol.msg_metadata.MAVLinkPacket;

public class MAVLinkStreams {

	public interface MAVLinkOutputStream {

		void sendMavPacket(MAVLinkPacket pack);

		boolean isConnected();

		void toggleConnectionState();

        void openConnection();

        void closeConnection();

	}

	public interface MavlinkInputStream {
		public void notifyConnected();

		public void notifyDisconnected();

		public void notifyReceivedData(MAVLinkMessage m);
	}
}
