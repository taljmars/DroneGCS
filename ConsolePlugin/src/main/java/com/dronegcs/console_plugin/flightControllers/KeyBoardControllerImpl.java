package com.dronegcs.console_plugin.flightControllers;

import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.protocol.msgbuilder.MavLinkRC;
import com.generic_tools.devices.SerialConnection;
import com.generic_tools.logger.Logger;
import javafx.scene.input.KeyEvent;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class KeyBoardControllerImpl implements KeyBoardController, Runnable {

	private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(KeyBoardControllerImpl.class);
	
	@Autowired @NotNull(message = "Internal Error: Failed to keyboard parser")
	private KeyBoardConfigurationParser keyBoardConfigurationParser;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get serial communication")
	private SerialConnection serialConnection;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;
	
	private boolean param_loaded = false;
	private Thread KeyboardStabilizer = null;
	
	/* Variables that should be initialized using using parser */
//	private int _STABILIZER_CYCLE;
//
//	private int _MIN_PWM_RANGE = 0;
//	private int _MAX_PWM_RANGE = 0;
//
//	private int _MIN_PWM_ANGLE = 0;
//	private int _MAX_PWM_ANGLE = 0;
//
//	private int _TRIM_ANGLE = 0;
//
//	private int _PITCH_STEP = 0;
	private AtomicInteger _TRIM_ANGLE_PITCH = new AtomicInteger(0);
//
//	private int _ROLL_STEP = 0;
	private AtomicInteger _TRIM_ANGLE_ROLL = new AtomicInteger(0);
//
//	private int _YAW_STEP = 0;
	private AtomicInteger _TRIM_ANGLE_YAW = new AtomicInteger(0);
//
//	private int _THR_STEP = 0;
//	private AtomicInteger _INIT_THR;
//
	private int _CAMERA_PITCH = 1500;
	private int _CAMERA_ROLL = 1500;
	
	/* RC values that should be sent */
	private AtomicInteger RC_Thr = new AtomicInteger(0);
	private AtomicInteger RC_Yaw = new AtomicInteger(0);
	private AtomicInteger RC_Pitch = new AtomicInteger(0);
	private AtomicInteger RC_Roll = new AtomicInteger(0);
	
	/* RC values that currently being used */
	private AtomicInteger last_RC_Roll = new AtomicInteger(0);
	private AtomicInteger last_RC_Pitch = new AtomicInteger(0);
	private AtomicInteger last_RC_Thr = new AtomicInteger(0);
	private AtomicInteger last_RC_Yaw = new AtomicInteger(0);
	
	/* Keyboard status holders */
	public AtomicBoolean bActive = new AtomicBoolean(false);
	private AtomicBoolean onHold = new AtomicBoolean(false);

	private KeyBoardRcValues keyBoardRcValues;

	private static int called;
	@PostConstruct
	public void init() {

		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");

		keyBoardRcValues = keyBoardConfigurationParser.parse();

		_TRIM_ANGLE_PITCH.set(keyBoardRcValues.get_TRIM_ANGLE_PITCH());
		_TRIM_ANGLE_ROLL.set(keyBoardRcValues.get_TRIM_ANGLE_ROLL());
		_TRIM_ANGLE_YAW.set(keyBoardRcValues.get_TRIM_ANGLE_YAW());

		Reset();

		KeyboardStabilizer = new Thread(this);
		KeyboardStabilizer.start();
	}
	
	@Override
	public void run() {
		logger.LogGeneralMessege("Stabilizer Thread started");
		LOGGER.debug("Stabilizer Thread started");
		while (true) {
			try {
				//Thread.sleep(1000);
				Thread.sleep(keyBoardRcValues.get_STABILIZER_CYCLE());
				Update();
			} 
			catch (InterruptedException e) {
				LOGGER.error("Keyboard monitor terminated", e);
			}
		}
	}
	
	public void Activate() {
		bActive.set(true);
	}
	
	public void Deactivate() {
		bActive.set(false);
	}

	@Override
	public void parse() throws Exception {
		try {
			KeyBoardRcValues newConf = keyBoardConfigurationParser.parse();
			if (keyBoardRcValues == null) {
				keyBoardRcValues = newConf;
				return;
			}
			Field[] fields = KeyBoardRcValues.class.getDeclaredFields();
			for (Field field : fields) {
				if (!KeyBoardRcValues.isSerializable(field))
					continue;
				Object val = KeyBoardRcValues.execGetter(newConf, field);
				KeyBoardRcValues.execSetter(keyBoardRcValues, field, val);
			}
		}
		catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new Exception("Failed to parse file");
		}
	}

	@Override
	public void dump() throws Exception {
		keyBoardConfigurationParser.dump(keyBoardRcValues);
	}

	public void HoldIfNeeded() {
		if (!bActive.get())
			return;
			
		onHold.set(true);
		bActive.set(false);
	}
	public void ReleaseIfNeeded() {
		if (!onHold.get())
			return;
			
		onHold.set(false);
		bActive.set(true);
	}
	
	private static int constrain(int val, int min, int max){
		if (val < min) return min;
		if (val > max) return max;
		return val;
	}
	
	public void ResetRCSet() {
		RC_Thr.set(keyBoardRcValues.get_INIT_THR());
		RC_Yaw.set(keyBoardRcValues.get_TRIM_ANGLE_YAW());
		RC_Pitch.set(keyBoardRcValues.get_TRIM_ANGLE_PITCH());
		RC_Roll.set(keyBoardRcValues.get_TRIM_ANGLE_ROLL());
	}
	
	private void ReduceRCSet() {		
		if (LastContolKeyTS == 0)
			return;
		
		long CurrentTS = (new Date()).getTime();
		
		long gap = CurrentTS - LastContolKeyTS;
		if (gap < keyBoardRcValues.get_STABILIZER_CYCLE() && gap > 0)
			return;
		
		// Roll, Pitch, Throttle, Yaw
		// For roll: right is positive, left is negative
		// For pitch: down is positive, up is negative
		// For Throttle: up is higher, down is lower (with min value of 1000)
		// For Yaw: right is positive, left is negative (no decay, and with some hexa values)
		
		RC_Yaw.set(_TRIM_ANGLE_YAW.get() + ((RC_Yaw.get() - _TRIM_ANGLE_YAW.get())/10));
		RC_Yaw.set(constrain(RC_Yaw.get(), keyBoardRcValues.get_MIN_PWM_ANGLE(), keyBoardRcValues.get_MAX_PWM_ANGLE()));

		RC_Pitch.set(_TRIM_ANGLE_PITCH.get() + ((RC_Pitch.get() - _TRIM_ANGLE_PITCH.get())/2));
		RC_Pitch.set(constrain(RC_Pitch.get(), keyBoardRcValues.get_MIN_PWM_ANGLE(), keyBoardRcValues.get_MAX_PWM_ANGLE()));
		
		RC_Roll.set(_TRIM_ANGLE_ROLL.get() + ((RC_Roll.get() - _TRIM_ANGLE_ROLL.get())/2));
		RC_Roll.set(constrain(RC_Roll.get(), keyBoardRcValues.get_MIN_PWM_ANGLE(), keyBoardRcValues.get_MAX_PWM_ANGLE()));
		
		LastContolKeyTS = CurrentTS;
	}
	
	
	public String toString() {
		return last_RC_Roll + " " + last_RC_Pitch + " " + last_RC_Thr + " " + last_RC_Yaw;
	}
	
	public String GetRCSet() {
		//Roll, Pitch, Throttle, Yaw
		
		if (RC_Roll == last_RC_Roll && RC_Pitch == last_RC_Pitch && RC_Yaw == last_RC_Yaw && RC_Thr == last_RC_Thr &&
			_CAMERA_PITCH == 1500 && _CAMERA_ROLL == 1500){
			return "";
		}
		
		last_RC_Roll = RC_Roll;
		last_RC_Thr = RC_Thr;
		last_RC_Pitch = RC_Pitch;
		last_RC_Yaw = RC_Yaw;
		
		return RC_Roll + "," + RC_Pitch + "," + RC_Thr + "," + RC_Yaw + ",0,0," + _CAMERA_PITCH + "," + _CAMERA_ROLL;
	}
	
	static long LastContolKeyTS = 0;
	private void UpdateRCSet(KeyEvent event) {
		LOGGER.debug("Updating RC Set");
		if (!bActive.get())
			return;
		
		if (event == null)
			return;
		
		switch( event.getCode() ) { 
			// For pitch: down is positive, up is negative
		    case UP:
		    	if (RC_Pitch.get() > keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_PITCH_STEP())
		    		RC_Pitch.set(RC_Pitch.get() - (keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_PITCH_STEP()));
		    	else
		    		RC_Pitch.set(RC_Pitch.get() - keyBoardRcValues.get_PITCH_STEP());
		    	RC_Pitch.set(constrain(RC_Pitch.get(), keyBoardRcValues.get_MIN_PWM_ANGLE(), keyBoardRcValues.get_MAX_PWM_ANGLE()));
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		        break;
		    case DOWN:
		    	if (RC_Pitch.get() < -keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_PITCH_STEP())
		    		RC_Pitch.set(RC_Pitch.get() + (keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_PITCH_STEP()));
		    	else
		    		RC_Pitch.set(RC_Pitch.get() + keyBoardRcValues.get_PITCH_STEP());
		    	RC_Pitch.set(constrain(RC_Pitch.get(), keyBoardRcValues.get_MIN_PWM_ANGLE(), keyBoardRcValues.get_MAX_PWM_ANGLE()));
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		        break;
		        
			// For roll: right is positive, left is negative
		    case LEFT:
		    	if (RC_Roll.get() > keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_ROLL_STEP())
		    		RC_Roll.set(RC_Roll.get() - (keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_ROLL_STEP()));
		    	else
		    		RC_Roll.set(RC_Roll.get() - keyBoardRcValues.get_ROLL_STEP());
		    	RC_Roll.set(constrain(RC_Roll.get(), keyBoardRcValues.get_MIN_PWM_ANGLE(), keyBoardRcValues.get_MAX_PWM_ANGLE()));
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		        break;
		    case RIGHT:
		    	if (RC_Roll.get() < -keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_ROLL_STEP())
		    		RC_Roll.set(RC_Roll.get() + (keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_ROLL_STEP()));
		    	else
		    		RC_Roll.set(RC_Roll.get() + keyBoardRcValues.get_ROLL_STEP());
		    	RC_Roll.set(constrain(RC_Roll.get(), keyBoardRcValues.get_MIN_PWM_ANGLE(), keyBoardRcValues.get_MAX_PWM_ANGLE()));
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		        break;
		        
			// For Throttle: up is higher, down is lower (with min value of 1000)
		    case W:
		    	RC_Thr.set(RC_Thr.get() + keyBoardRcValues.get_THR_STEP());
		    	RC_Thr.set(constrain(RC_Thr.get(), keyBoardRcValues.get_MIN_PWM_RANGE(), keyBoardRcValues.get_MAX_PWM_RANGE()));
		    	event.consume();
		    	break;
		    case S:
		    	RC_Thr.set(RC_Thr.get() - keyBoardRcValues.get_THR_STEP());
		    	RC_Thr.set(constrain(RC_Thr.get(), keyBoardRcValues.get_MIN_PWM_RANGE(), keyBoardRcValues.get_MAX_PWM_RANGE()));
		    	event.consume();
		    	break;
		        
			// For Yaw: right is positive, left is negative (no decay, and with some hexa values)
		    case D:
		    	if (RC_Yaw.get() < -keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_YAW_STEP())
		    		RC_Yaw.set(RC_Yaw.get() + (keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_YAW_STEP()));
		    	else
		    		RC_Yaw.set(RC_Yaw.get() + keyBoardRcValues.get_YAW_STEP());
		    	RC_Yaw.set(constrain(RC_Yaw.get(), keyBoardRcValues.get_MIN_PWM_ANGLE(), keyBoardRcValues.get_MAX_PWM_ANGLE()));
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		    	break;
		    case A:
		    	if (RC_Yaw.get() > keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_YAW_STEP())
		    		RC_Yaw.set(RC_Yaw.get() - (keyBoardRcValues.get_STABILIZER_FACTOR() * keyBoardRcValues.get_YAW_STEP()));
		    	else
		    		RC_Yaw.set(RC_Yaw.get() - keyBoardRcValues.get_YAW_STEP());
		    	RC_Yaw.set(constrain(RC_Yaw.get(), keyBoardRcValues.get_MIN_PWM_ANGLE(), keyBoardRcValues.get_MAX_PWM_ANGLE()));
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		    	break;
		    case BACK_SPACE:
		    	_TRIM_ANGLE_ROLL = RC_Roll;
		    	_TRIM_ANGLE_PITCH = RC_Pitch;
		    	_TRIM_ANGLE_YAW = RC_Yaw;
				logger.LogGeneralMessege("Calibrating New Center of Keyboard Control");
		    	event.consume();
		    	break;
		    case SPACE:
		    	_TRIM_ANGLE_ROLL.set(keyBoardRcValues.get_TRIM_ANGLE());
		    	_TRIM_ANGLE_PITCH.set(keyBoardRcValues.get_TRIM_ANGLE());
		    	_TRIM_ANGLE_YAW.set(keyBoardRcValues.get_TRIM_ANGLE());
		    	logger.LogGeneralMessege("Resetting Center of Keyboard Control");
		    	event.consume();
		    	break;
		    case COMMA:
		    	_CAMERA_PITCH -= 10;
		    	break;
		    case PERIOD:
		    	_CAMERA_PITCH += 10;
		    	break;
		    default:
		    	LOGGER.error("Key Value: {}", event);
		}
		
		String val = GetRCSet();
		
		if (! val.isEmpty()) {
			serialConnection.write(val);
			LOGGER.debug("Sending '{}'", val);
			int[] rcOutputs = {RC_Roll.get(), RC_Pitch.get(), RC_Thr.get(), RC_Yaw.get(), 0, 0, _CAMERA_PITCH, _CAMERA_ROLL};
			MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
		}
	}
	
	public void Update() {
		if (!bActive.get())
			return;
			
		ReduceRCSet();
		
		String val = GetRCSet();
		
		if (param_loaded && ! val.isEmpty()) {
			serialConnection.write(val);
			LOGGER.debug("Sending '{}'", val);
			int[] rcOutputs = {RC_Roll.get(), RC_Pitch.get(), RC_Thr.get(), RC_Yaw.get(), 0, 0, _CAMERA_PITCH, _CAMERA_ROLL};
			MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
		}
	}

	public void Reset() {
		LOGGER.debug("Resetting RC Set");
		ResetRCSet();
	}

	public void SetThrust(int parseInt) {
		RC_Thr.set(parseInt);
	}

	@Override
	public void handle(KeyEvent arg0) {
		UpdateRCSet(arg0);
	}

	/////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	///////////  Setting atomic values via external API  ////////////

	@Override
	public Integer getStabilizeCycle() {
		return keyBoardRcValues.get_STABILIZER_CYCLE();
	}

	@Override
	public void setStabilizeCycle(Integer integer) {
		keyBoardRcValues.set_STABILIZER_CYCLE(integer);
	}

	@Override
	public Integer getTrimAngle() {
		return keyBoardRcValues.get_TRIM_ANGLE();
	}

	@Override
	public void setTrimAngle(Integer integer) {
		keyBoardRcValues.set_TRIM_ANGLE(integer);
	}

	@Override
	public Integer getMinPwmRange() {
		return keyBoardRcValues.get_MIN_PWM_RANGE();
	}

	@Override
	public void setMinPwmRange(Integer integer) {
		keyBoardRcValues.set_MIN_PWM_RANGE(integer);
	}

	@Override
	public Integer getMaxPwmRange() {
		return keyBoardRcValues.get_MAX_PWM_RANGE();
	}

	@Override
	public void setMaxPwmRange(Integer integer) {
		keyBoardRcValues.set_MAX_PWM_RANGE(integer);
	}

	@Override
	public Integer getMinPwmAngle() {
		return keyBoardRcValues.get_MIN_PWM_ANGLE();
	}

	@Override
	public void setMinPwmAngle(Integer integer) {
		keyBoardRcValues.set_MIN_PWM_ANGLE(integer);
	}

	@Override
	public Integer getMaxPwmAngle() {
		return keyBoardRcValues.get_MAX_PWM_ANGLE();
	}

	@Override
	public void setMaxPwmAngle(Integer integer) {
		keyBoardRcValues.set_MAX_PWM_ANGLE(integer);
	}

	@Override
	public Integer getRollStep() {
		return keyBoardRcValues.get_ROLL_STEP();
	}

	@Override
	public void setRollStep(Integer integer) {
		keyBoardRcValues.set_ROLL_STEP(integer);
	}

	@Override
	public Integer getPitchStep() {
		return keyBoardRcValues.get_PITCH_STEP();
	}

	@Override
	public void setPitchStep(Integer integer) {
		keyBoardRcValues.set_PITCH_STEP(integer);
	}

	@Override
	public Integer getThrustStep() {
		return keyBoardRcValues.get_THR_STEP();
	}

	@Override
	public void setThrustStep(Integer integer) {
		keyBoardRcValues.set_THR_STEP(integer);
	}

	@Override
	public Integer getYawStep() {
		return keyBoardRcValues.get_YAW_STEP();
	}

	@Override
	public void setYawStep(Integer integer) {
		keyBoardRcValues.set_YAW_STEP(integer);
	}

	@Override
	public Integer getInitialThrust() {
		return keyBoardRcValues.get_INIT_THR();
	}

	@Override
	public void setInitialThrust(Integer integer) {
		keyBoardRcValues.set_INIT_THR(integer);
	}

	@Override
	public Integer getStabilizeFactor() {
		return keyBoardRcValues.get_STABILIZER_FACTOR();
	}

	@Override
	public void setStabilizeFactor(Integer val) {
		keyBoardRcValues.set_STABILIZER_FACTOR(val);
	}


}
