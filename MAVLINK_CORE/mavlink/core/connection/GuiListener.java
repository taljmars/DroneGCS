package mavlink.core.connection;

import mavlink.is.connection.MavLinkConnectionListener;
import mavlink.is.protocol.msg_metadata.MAVLinkMessage;

public class GuiListener implements MavLinkConnectionListener {

	@Override
	public void onConnect() {
	}

	@Override
	public void onReceiveMessage(MAVLinkMessage msg) {
		// TODO Auto-generated method stub
		/*
		switch (msg.msgid) {
			case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
				Dashboard.window.SetHeartBeat(true);
				return;
		
			case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:
				msg_param_value m_value = (msg_param_value) msg;
				int cnt = m_value.param_count;
				Dashboard.window.setProgressBar(0, cnt);
				return;
				
			case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
				msg_nav_controller_output m_nav_value = (msg_nav_controller_output) msg;
				Dashboard.window.SetBearing(1.0 * m_nav_value.nav_bearing);
				Dashboard.window.SetDistanceToWaypoint(m_nav_value.wp_dist);
				return;
				
			case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
				Dashboard.window.SetPosition(new Coord2D(((msg_global_position_int) msg).lat / 1E7,
						((msg_global_position_int) msg).lon / 1E7));
				return;
		
			case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
				ByteBuffer bf = msg.pack().payload.payload;
				try {
					Dashboard.addIncommingMessegeToDisplay(new String(bf.array(), "ASCII"));
				} catch (UnsupportedEncodingException e) {
				// 	TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
				
			case msg_fence_status.MAVLINK_MSG_ID_FENCE_STATUS:
				ByteBuffer bf1 = msg.pack().payload.payload;
				try {
					Dashboard.addIncommingMessegeToDisplay(new String(bf1.array(), "ASCII"));
					Dashboard.addIncommingMessegeToDisplay("Fence Status: " + msg.toString());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
				
			case msg_fence_fetch_point.MAVLINK_MSG_ID_FENCE_FETCH_POINT:
				ByteBuffer bf11 = msg.pack().payload.payload;
				try {
					Dashboard.addIncommingMessegeToDisplay(new String(bf11.array(), "ASCII"));
					Dashboard.addIncommingMessegeToDisplay("Fence FEtch Point: " + msg.toString());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			case msg_fence_point.MAVLINK_MSG_ID_FENCE_POINT:
				ByteBuffer bf111 = msg.pack().payload.payload;
				try {
					Dashboard.addIncommingMessegeToDisplay(new String(bf111.array(), "ASCII"));
					Dashboard.addIncommingMessegeToDisplay("Fence Point: " + msg.toString());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
				
		}
		*/
	}

	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onComError(String errMsg) {
		// TODO Auto-generated method stub
		
	}

}
