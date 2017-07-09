package com.dronegcs.console_plugin.flightControllers;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

import java.lang.reflect.InvocationTargetException;

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

	/**
	 * parse keyboard settings from file
	 */
	void parse() throws Exception;

	/**
	 * dump keyboard settings to a file
	 */
	void dump() throws Exception;

	/**
	 * Setting default values section
	 */
	Integer getStabilizeCycle();
	void setStabilizeCycle(Integer integer);

	Integer getTrimAngle();
	void setTrimAngle(Integer integer);

	Integer getMinPwmRange();
	void setMinPwmRange(Integer integer);

	Integer getMaxPwmRange();
	void setMaxPwmRange(Integer integer);

	Integer getMinPwmAngle();
	void setMinPwmAngle(Integer integer);

	Integer getMaxPwmAngle();
	void setMaxPwmAngle(Integer integer);

	Integer getRollStep();
	void setRollStep(Integer integer);

	Integer getPitchStep();
	void setPitchStep(Integer integer);

	Integer getThrustStep();
	void setThrustStep(Integer integer);

	Integer getYawStep();
	void setYawStep(Integer integer);

	Integer getInitialThrust();
	void setInitialThrust(Integer integer);

	Integer getStabilizeFactor();
	void setStabilizeFactor(Integer val);
}
