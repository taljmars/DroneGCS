package main.java.mavlink_core.mavlink.core.flightControllers;

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import devices.KeyBoardController;
import devices.SerialConnection;
import gui.services.DialogManagerSvc;
import gui.services.LoggerDisplayerSvc;
import javafx.scene.input.KeyEvent;
import logger.Logger;
import mavlink.drone.Drone;
import mavlink.protocol.msgbuilder.MavLinkRC;

@ComponentScan("logger")
@ComponentScan("devices")
@ComponentScan("mavlink.core.drone")
@ComponentScan("gui.services")
@Component("keyBoardController")
public class KeyBoardControllerImpl implements KeyBoardController, Runnable {
	
	@Autowired @NotNull(message = "Internal Error: Failed to keyboard parser")
	private KeyBoardConfigurationParser keyBoardConfigurationParser;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get serial communication")
	private SerialConnection serialConnection;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;
	
	private boolean param_loaded = false;
	private Thread KeyboardStabilizer = null;
	
	/* Variables that should be initialized using using parser */
	private int _STABILIZER_CYCLE;
	
	private int _MIN_PWM_RANGE = 0;
	private int _MAX_PWM_RANGE = 0;
	
	private int _MIN_PWM_ANGLE = 0;
	private int _MAX_PWM_ANGLE = 0;
	
	private int _TRIM_ANGLE = 0;
	
	private int _PITCH_STEP = 0;
	private int _TRIM_ANGLE_PITCH = 0;
	
	private int _ROLL_STEP = 0;
	private int _TRIM_ANGLE_ROLL = 0;
	
	private int _YAW_STEP = 0;
	private int _TRIM_ANGLE_YAW = 0;
	
	private int _THR_STEP = 0;
	private int _INIT_THR = 0;
	
	private int _CAMERA_PITCH = 1500;
	private int _CAMERA_ROLL = 1500;
	
	/* RC values that should be sent */
	private int RC_Thr = _INIT_THR;
	private int RC_Yaw = 0;
	private int RC_Pitch = 0;
	private int RC_Roll = 0;
	
	/* RC values that currently being used */
	private int last_RC_Roll = _TRIM_ANGLE_ROLL; 
	private int last_RC_Pitch = _TRIM_ANGLE_PITCH;
	private int last_RC_Thr = 0;
	private int last_RC_Yaw = _TRIM_ANGLE_YAW;
	
	/* Keyboard status holders */
	public boolean bActive = false;
	private boolean onHold = false;
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		_STABILIZER_CYCLE = keyBoardConfigurationParser.getStabilizerCycle();
		
		_MIN_PWM_RANGE = keyBoardConfigurationParser.getMinPwmRange();
		_MAX_PWM_RANGE = keyBoardConfigurationParser.getMaxPwmRange();
		
		_MIN_PWM_ANGLE = keyBoardConfigurationParser.getMinPwmAngle();
		_MAX_PWM_ANGLE = keyBoardConfigurationParser.getMaxPwmAngle();
		
		_TRIM_ANGLE = keyBoardConfigurationParser.getTrimAngle();
		
		_PITCH_STEP = keyBoardConfigurationParser.getPitchStep();
		_TRIM_ANGLE_PITCH = keyBoardConfigurationParser.getTrimAnglePitch();
		
		_ROLL_STEP = keyBoardConfigurationParser.getRollStep();
		_TRIM_ANGLE_ROLL = keyBoardConfigurationParser.getTrimAngleRoll();
		
		_YAW_STEP = keyBoardConfigurationParser.getYawStep();
		_TRIM_ANGLE_YAW = keyBoardConfigurationParser.getTrimAngleYaw();
		
		_THR_STEP = keyBoardConfigurationParser.getThrustStep();
		_INIT_THR = keyBoardConfigurationParser.getInitThrust();
		
		Reset();
		
		KeyboardStabilizer = new Thread(this);
		KeyboardStabilizer.start();
	}
	
	@Override
	public void run() {
		logger.LogGeneralMessege(this.getClass().getName() + " Stabilizer Thread started");
		while (true) {
			try {
				//Thread.sleep(1000);
				Thread.sleep(_STABILIZER_CYCLE);
				Update();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void Activate() {
		bActive = true;
	}
	
	public void Deactivate() {
		bActive = false;
	}
	
	public void HoldIfNeeded() {
		if (!bActive)
			return;
			
		onHold = true;
		bActive = false;
	}
	public void ReleaseIfNeeded() {
		if (!onHold)
			return;
			
		onHold = false;
		bActive = true;
	}
	
	private static int constrain(int val, int min, int max){
		if (val < min) return min;
		if (val > max) return max;
		return val;
	}
	
	public void ResetRCSet() {
		RC_Thr = _INIT_THR;
		RC_Yaw = _TRIM_ANGLE_YAW;
		RC_Pitch = _TRIM_ANGLE_PITCH;
		RC_Roll = _TRIM_ANGLE_ROLL;
	}
	
	private void ReduceRCSet() {		
		if (LastContolKeyTS == 0)
			return;
		
		long CurrentTS = (new Date()).getTime();
		
		long gap = CurrentTS - LastContolKeyTS;
		if (gap < _STABILIZER_CYCLE && gap > 0)
			return;
		
		// Roll, Pitch, Throttle, Yaw
		// For roll: right is positive, left is negative
		// For pitch: down is positive, up is negative
		// For Throttle: up is higher, down is lower (with min value of 1000)
		// For Yaw: right is positive, left is negative (no decay, and with some hexa values)
		
		RC_Yaw = _TRIM_ANGLE_YAW + ((RC_Yaw - _TRIM_ANGLE_YAW)/10);
		RC_Yaw = constrain(RC_Yaw, _MIN_PWM_ANGLE, _MAX_PWM_ANGLE);
		
		RC_Pitch = _TRIM_ANGLE_PITCH + ((RC_Pitch - _TRIM_ANGLE_PITCH)/2);
		RC_Pitch = constrain(RC_Pitch, _MIN_PWM_ANGLE, _MAX_PWM_ANGLE);
		
		RC_Roll = _TRIM_ANGLE_ROLL + ((RC_Roll - _TRIM_ANGLE_ROLL)/2);
		RC_Roll = constrain(RC_Roll, _MIN_PWM_ANGLE, _MAX_PWM_ANGLE);
		
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
		System.out.println(getClass().getName() + " Updating RC Set");
		if (!bActive)
			return;
		
		if (event == null)
			return;
		
		switch( event.getCode() ) { 
			// For pitch: down is positive, up is negative
		    case UP:
		    	if (RC_Pitch > 3 * _PITCH_STEP)
		    		RC_Pitch-=(3 * _PITCH_STEP);
		    	else
		    		RC_Pitch-=_PITCH_STEP;
		    	RC_Pitch = constrain(RC_Pitch, _MIN_PWM_ANGLE, _MAX_PWM_ANGLE);
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		        break;
		    case DOWN:
		    	if (RC_Pitch < -3 * _PITCH_STEP)
		    		RC_Pitch+=(3 * _PITCH_STEP);
		    	else
		    		RC_Pitch+=_PITCH_STEP;
		    	RC_Pitch = constrain(RC_Pitch, _MIN_PWM_ANGLE, _MAX_PWM_ANGLE);
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		        break;
		        
			// For roll: right is positive, left is negative
		    case LEFT:
		    	if (RC_Roll > 3 * _ROLL_STEP)
		    		RC_Roll-=(3 * _ROLL_STEP);
		    	else
		    		RC_Roll-=_ROLL_STEP;
		    	RC_Roll = constrain(RC_Roll, _MIN_PWM_ANGLE, _MAX_PWM_ANGLE);
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		        break;
		    case RIGHT:
		    	if (RC_Roll < -3 * _ROLL_STEP)
		    		RC_Roll+=(3 * _ROLL_STEP);
		    	else
		    		RC_Roll+=_ROLL_STEP;
		    	RC_Roll = constrain(RC_Roll, _MIN_PWM_ANGLE, _MAX_PWM_ANGLE);
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		        break;
		        
			// For Throttle: up is higher, down is lower (with min value of 1000)
		    case W:
		    	RC_Thr += _THR_STEP;
		    	RC_Thr = constrain(RC_Thr, _MIN_PWM_RANGE, _MAX_PWM_RANGE);
		    	event.consume();
		    	break;
		    case S:
		    	RC_Thr -= _THR_STEP;
		    	RC_Thr = constrain(RC_Thr, _MIN_PWM_RANGE, _MAX_PWM_RANGE);
		    	event.consume();
		    	break;
		        
			// For Yaw: right is positive, left is negative (no decay, and with some hexa values)
		    case D:
		    	if (RC_Yaw < -3 * _YAW_STEP)
		    		RC_Yaw+=(3 * _YAW_STEP);
		    	else
		    		RC_Yaw+=_YAW_STEP;
		    	RC_Yaw = constrain(RC_Yaw, _MIN_PWM_ANGLE, _MAX_PWM_ANGLE);
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		    	break;
		    case A:
		    	if (RC_Yaw > 3 * _YAW_STEP)
		    		RC_Yaw-=(3 * _YAW_STEP);
		    	else
		    		RC_Yaw-=_YAW_STEP;
		    	RC_Yaw = constrain(RC_Yaw, _MIN_PWM_ANGLE, _MAX_PWM_ANGLE);
		    	
		    	LastContolKeyTS = (new Date()).getTime();
		    	event.consume();
		    	break;
		    case BACK_SPACE:
		    	_TRIM_ANGLE_ROLL = RC_Roll;
		    	_TRIM_ANGLE_PITCH = RC_Pitch;
		    	_TRIM_ANGLE_YAW = RC_Yaw;
		    	loggerDisplayerSvc.logGeneral("Calibrating New Center of Keyboard Control");
		    	event.consume();
		    	break;
		    case SPACE:
		    	_TRIM_ANGLE_ROLL = _TRIM_ANGLE;
		    	_TRIM_ANGLE_PITCH = _TRIM_ANGLE;
		    	_TRIM_ANGLE_YAW = _TRIM_ANGLE;
		    	loggerDisplayerSvc.logGeneral("Reseting Center of Keyboard Control");
		    	event.consume();
		    	break;
		    case COMMA:
		    	_CAMERA_PITCH -= 10;
		    	break;
		    case PERIOD:
		    	_CAMERA_PITCH += 10;
		    	break;
		    default:
		    	System.err.println("Key Value: " + event);
		}
		
		String val = GetRCSet();
		
		if (! val.isEmpty()) {
			serialConnection.write(val);
			System.out.println("Sending '" + val + "'");
			int[] rcOutputs = {RC_Roll, RC_Pitch, RC_Thr, RC_Yaw, 0, 0, _CAMERA_PITCH, _CAMERA_ROLL};
			MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
		}
	}
	
	public void Update() {
		if (!bActive)
			return;
			
		ReduceRCSet();
		
		String val = GetRCSet();
		
		if (param_loaded && ! val.isEmpty()) {
			serialConnection.write(val);
			System.out.println("Sending '" + val + "'");
			int[] rcOutputs = {RC_Roll, RC_Pitch, RC_Thr, RC_Yaw, 0, 0, _CAMERA_PITCH, _CAMERA_ROLL};
			MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
		}
	}

	public void Reset() {
		System.out.println("Reseting RC Set");
		ResetRCSet();
	}

	public void SetThrust(int parseInt) {
		RC_Thr = parseInt;
	}

	@Override
	public void handle(KeyEvent arg0) {
		UpdateRCSet(arg0);
	}	
}
