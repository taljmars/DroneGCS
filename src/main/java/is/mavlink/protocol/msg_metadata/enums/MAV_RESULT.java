/** result from a mavlink command
*/
package main.java.is.mavlink.protocol.msg_metadata.enums;

public class MAV_RESULT {
	public static final int MAV_RESULT_ACCEPTED = 0; /* Command ACCEPTED and EXECUTED | */
	public static final int MAV_RESULT_TEMPORARILY_REJECTED = 1; /* Command TEMPORARY REJECTED/DENIED | */
	public static final int MAV_RESULT_DENIED = 2; /* Command PERMANENTLY DENIED | */
	public static final int MAV_RESULT_UNSUPPORTED = 3; /* Command UNKNOWN/UNSUPPORTED | */
	public static final int MAV_RESULT_FAILED = 4; /* Command executed, but failed | */
	public static final int MAV_RESULT_ENUM_END = 5; /*  | */
}
