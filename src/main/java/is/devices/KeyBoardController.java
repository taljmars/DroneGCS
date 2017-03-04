package is.devices;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public interface KeyBoardController extends EventHandler<KeyEvent> {

	/**
	 * Release the keyboard control for a time, this is usually being set when the user
	 * would like to use the keyboard without effecting the flight.
	 * Note: 'Active' function will not gain the control as long as hold/release take place.
	 */
	void HoldIfNeeded();

	/**
	 * Gain the keyboard control for a time, this is usually being set when the user
	 * would like to use the keyboard without effecting the flight.
	 * Note: 'Active' function will not gain the control as long as hold/release take place.
	 */
	void ReleaseIfNeeded();

    /**
	 * Starting the keyboard controller
	 */
	void Activate();

	/**
	 * This function use to set the base thrust for the keyboard flight control
	 * This value can be change during the flight.
	 *  
	 * @param eAvg - average thrust to be set
	 */
	void SetThrust(int eAvg);

	/**
	 * Stopping the keyboard controller
	 */
	void Deactivate();

}
