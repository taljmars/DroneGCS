package desktop.logic;

import java.io.IOException;

import mavlink.core.connection.RadioConnection;
import mavlink.core.drone.MyDroneImpl;
import mavlink.core.drone.DroneInterfaces.Handler;
import mavlink.core.gcs.follow.Follow;
import mavlink.is.model.Drone;
import mavlink.is.protocol.msg_metadata.MAVLinkMessage;
import mavlink.is.protocol.msg_metadata.MAVLinkPacket;
import mavlink.is.protocol.msgbuilder.MavLinkMsgHandler;
import mavlink.is.protocol.msgparser.Parser;
import desktop.location.FakeLocation;

public class MyLogic implements Runnable {
	public Drone drone;
	public Follow follow;
	private MavLinkMsgHandler mavlinkHandler;
	RadioConnection radConn = null;
	

	public MyLogic() {
		radConn = new RadioConnection();
		radConn.connect();
		
		Handler handler = new desktop.logic.Handler();
		drone = new MyDroneImpl(radConn, new Clock(), handler,FakeFactory.fakePreferences());
		mavlinkHandler = new MavLinkMsgHandler(drone);
		follow = new Follow(drone, handler, new FakeLocation());
	}

	@Override
	public void run() {
		try {
			final Parser parser = new Parser();
			parser.stats.mavlinkResetStats();
			
			final byte[] readBuffer = new byte[8192*1024];
			
			while (true) {

				//final byte[] readBuffer = new byte[4096];
				
				
				if (radConn == null || readBuffer == null)
					continue;
				
				System.out.println("SIZE " + readBuffer.length);
				
				int bufferSize = radConn.readDataBlock(readBuffer);		
				if (bufferSize < 1) {
					continue;
				}

				for (int i = 0; i < bufferSize; i++) {
					MAVLinkPacket mavPacket = parser.mavlink_parse_char(readBuffer[i] & 0x00ff);
					if (mavPacket != null) {
						MAVLinkMessage msg = mavPacket.unpack();
						mavlinkHandler.receiveData(msg);
					}
				}
				
				readBuffer[0] = '\0';
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}