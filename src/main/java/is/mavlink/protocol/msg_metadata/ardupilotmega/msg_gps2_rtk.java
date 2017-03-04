// MESSAGE GPS2_RTK PACKING
package is.mavlink.protocol.msg_metadata.ardupilotmega;

import is.mavlink.protocol.msg_metadata.MAVLinkMessage;
import is.mavlink.protocol.msg_metadata.MAVLinkPacket;
import is.mavlink.protocol.msg_metadata.MAVLinkPayload;

/**
* RTK GPS data. Gives information on the relative baseline calculation the GPS is reporting
*/
public class msg_gps2_rtk extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_GPS2_RTK = 128;
	public static final int MAVLINK_MSG_LENGTH = 35;
	private static final long serialVersionUID = MAVLINK_MSG_ID_GPS2_RTK;
	

 	/**
	* Time since boot of last baseline message received in ms.
	*/
	public int time_last_baseline_ms; 
 	/**
	* GPS Time of Week of last baseline
	*/
	public int tow; 
 	/**
	* Current baseline in ECEF x or NED north component in mm.
	*/
	public int baseline_a_mm; 
 	/**
	* Current baseline in ECEF y or NED east component in mm.
	*/
	public int baseline_b_mm; 
 	/**
	* Current baseline in ECEF z or NED down component in mm.
	*/
	public int baseline_c_mm; 
 	/**
	* Current estimate of baseline accuracy.
	*/
	public int accuracy; 
 	/**
	* Current number of integer ambiguity hypotheses.
	*/
	public int iar_num_hypotheses; 
 	/**
	* GPS Week Number of last baseline
	*/
	public short wn; 
 	/**
	* Identification of connected RTK receiver.
	*/
	public byte rtk_receiver_id; 
 	/**
	* GPS-specific health report for RTK data.
	*/
	public byte rtk_health; 
 	/**
	* Rate of baseline messages being received by GPS, in HZ
	*/
	public byte rtk_rate; 
 	/**
	* Current number of sats used for RTK calculation.
	*/
	public byte nsats; 
 	/**
	* Coordinate system of baseline. 0 == ECEF, 1 == NED
	*/
	public byte baseline_coords_type; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * 
	 * @return mavlink packet
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_GPS2_RTK;
		packet.payload.putInt(time_last_baseline_ms);
		packet.payload.putInt(tow);
		packet.payload.putInt(baseline_a_mm);
		packet.payload.putInt(baseline_b_mm);
		packet.payload.putInt(baseline_c_mm);
		packet.payload.putInt(accuracy);
		packet.payload.putInt(iar_num_hypotheses);
		packet.payload.putShort(wn);
		packet.payload.putByte(rtk_receiver_id);
		packet.payload.putByte(rtk_health);
		packet.payload.putByte(rtk_rate);
		packet.payload.putByte(nsats);
		packet.payload.putByte(baseline_coords_type);
		return packet;		
	}

    /**
     * Decode a gps2_rtk message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    time_last_baseline_ms = payload.getInt();
	    tow = payload.getInt();
	    baseline_a_mm = payload.getInt();
	    baseline_b_mm = payload.getInt();
	    baseline_c_mm = payload.getInt();
	    accuracy = payload.getInt();
	    iar_num_hypotheses = payload.getInt();
	    wn = payload.getShort();
	    rtk_receiver_id = payload.getByte();
	    rtk_health = payload.getByte();
	    rtk_rate = payload.getByte();
	    nsats = payload.getByte();
	    baseline_coords_type = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_gps2_rtk(){
    	msgid = MAVLINK_MSG_ID_GPS2_RTK;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     * @param mavLinkPacket - mavlink packet
     */
    public msg_gps2_rtk(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GPS2_RTK;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "GPS2_RTK");
        //Log.d("MAVLINK_MSG_ID_GPS2_RTK", toString());
    }
    
                          
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_GPS2_RTK -"+" time_last_baseline_ms:"+time_last_baseline_ms+" tow:"+tow+" baseline_a_mm:"+baseline_a_mm+" baseline_b_mm:"+baseline_b_mm+" baseline_c_mm:"+baseline_c_mm+" accuracy:"+accuracy+" iar_num_hypotheses:"+iar_num_hypotheses+" wn:"+wn+" rtk_receiver_id:"+rtk_receiver_id+" rtk_health:"+rtk_health+" rtk_rate:"+rtk_rate+" nsats:"+nsats+" baseline_coords_type:"+baseline_coords_type+"";
    }
}
