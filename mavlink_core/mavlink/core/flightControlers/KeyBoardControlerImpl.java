package mavlink.core.flightControlers;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.JFileChooser;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import gui.is.KeyBoardControler;
import gui.is.services.DialogManagerSvc;
import gui.is.services.LoggerDisplayerSvc;
import javafx.scene.input.KeyEvent;
import mavlink.is.drone.Drone;
import mavlink.is.protocol.msgbuilder.MavLinkRC;
import tools.comm.SerialConnection;
import tools.logger.Logger;

@ComponentScan("tools.logger")
@ComponentScan("tools.comm.internal")
@ComponentScan("mavlink.core.drone")
@ComponentScan("gui.is.services")
@Component("keyBoardControler")
public class KeyBoardControlerImpl implements KeyBoardControler {
	
	private static boolean param_loaded = false;
	private static Thread KeyboardStabilizer = null;
	
	@Resource(name = "twoWaySerialComm")
	@NotNull(message = "Internal Error: Failed to get serial communication")
	private SerialConnection serialConnection;
	
	@Resource(name = "drone")
	@NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Resource(name = "loggerDisplayerSvc")
	@NotNull(message = "Internal Error: Failed to get logger")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "dialogManagerSvc")
	@NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	@Resource(name = "logger")
	@NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;
	
	
	public KeyBoardControlerImpl() {
		
	}
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		LoadParams();
		
		KeyboardStabilizer = new Thread(new Runnable() {
			
			@Override
			public void run() {
				logger.LogGeneralMessege(this.getClass().getName() + " Stabilizer Thread started");
				while (true) {
					try {
						//Thread.sleep(1000);
						Thread.sleep(100);
						Update();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		KeyboardStabilizer.start();
	}
	
	public boolean bActive = false;
	private boolean onHold = false;
	public void Activate() {bActive = true;}
	public void Deactivate() {bActive = false;}
	
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
	
	private String settingsFilePath = "C:\\quad_setup_arducopter.txt";
	private Path fFilePath = null;
	private int paramAmount = 0;
	private final static Charset ENCODING = StandardCharsets.UTF_16;

	@SuppressWarnings("resource")
	private void LoadParams() {
		logger.LogGeneralMessege("Loading flight controler configuration");
		paramAmount = 0;
		param_loaded = false;
		fFilePath = Paths.get(settingsFilePath);
		if (fFilePath.toFile().exists() == false) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			fileChooser.setDialogTitle("Choose Configuration File");
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
			    File selectedFile = fileChooser.getSelectedFile();
			    settingsFilePath = selectedFile.getAbsolutePath();
			    LoadParams();
			    return;
			}
			else {
				logger.LogErrorMessege("Failed to read parameters, invalid line");
				logger.close();
		    	System.err.println(getClass().getName() + " Failed to read parameters, invalid line");
		    	dialogManagerSvc.showAlertMessageDialog("Configuration file must be supply, please resolve issue and try later");
				System.exit(-1);
			}
		}
		try (Scanner scanner =  new Scanner(fFilePath, ENCODING.name())) {
			while (scanner.hasNextLine()) {
				Scanner lineScanner = new Scanner(scanner.nextLine());
				lineScanner.useDelimiter("=");
			    if (lineScanner.hasNext()){
			      //assumes the line has a certain structure
			      String name = lineScanner.next();
			      String value = lineScanner.hasNext() ? lineScanner.next() : "";
			      
			      if (name.equals("_MIN_PWM_RANGE")) {
			    	  _MIN_PWM_RANGE = Integer.parseInt(value);
			    	  logger.LogGeneralMessege("_MIN_PWM_RANGE=" + _MIN_PWM_RANGE);
			    	  paramAmount++;
			      }
			      if (name.equals("_MAX_PWM_RANGE")) {
			    	  _MAX_PWM_RANGE = Integer.parseInt(value);
			    	  logger.LogGeneralMessege("_MAX_PWM_RANGE=" + _MAX_PWM_RANGE);
			    	  paramAmount++;
			      }
			      if (name.equals("_MIN_PWM_ANGLE")) {
			    	  _MIN_PWM_ANGLE = Integer.parseInt(value);
			    	  logger.LogGeneralMessege("_MIN_PWM_ANGLE=" + _MIN_PWM_ANGLE);
			    	  paramAmount++;
			      }
			      if (name.equals("_MAX_PWM_ANGLE")) {
			    	  _MAX_PWM_ANGLE = Integer.parseInt(value);
			    	  logger.LogGeneralMessege("_MAX_PWM_ANGLE=" + _MAX_PWM_ANGLE);
			    	  paramAmount++;
			      }
			      if (name.equals("_TRIM_ANGLE")) {
			    	  _TRIM_ANGLE = Integer.parseInt(value);
			    	  _TRIM_ANGLE_PITCH = _TRIM_ANGLE_ROLL = _TRIM_ANGLE_YAW = _TRIM_ANGLE;
			    	  logger.LogGeneralMessege("_TRIM_ANGLE=" + _TRIM_ANGLE);
			    	  paramAmount++;
			      }
			      if (name.equals("_PITCH_STEP")) {
			    	  _PITCH_STEP = Integer.parseInt(value);
			    	  logger.LogGeneralMessege("_PITCH_STEP=" + _PITCH_STEP);
			    	  paramAmount++;
			      }
			      if (name.equals("_ROLL_STEP")) {
			    	  _ROLL_STEP = Integer.parseInt(value);
			    	  logger.LogGeneralMessege("_ROLL_STEP=" + _ROLL_STEP);
			    	  paramAmount++;
			      }
			      if (name.equals("_YAW_STEP")) {
			    	  _YAW_STEP = Integer.parseInt(value);
			    	  logger.LogGeneralMessege("_YAW_STEP=" + _YAW_STEP);
			    	  paramAmount++;
			      }
			      if (name.equals("_THR_STEP")) {
			    	  _THR_STEP = Integer.parseInt(value);
			    	  logger.LogGeneralMessege("_THR_STEP=" + _THR_STEP);
			    	  paramAmount++;
			      }
			      if (name.equals("_INIT_THR")) {
				   	  _INIT_THR = Integer.parseInt(value);
				   	  logger.LogGeneralMessege("_INIT_THR=" + _INIT_THR);
				   	  paramAmount++;
				  }
			      
			  	
			      System.out.println("Param: '" + name + "', Value: '" + value + "'");
			    }
			    else {
			    	logger.LogErrorMessege("Failed to read parameters, invalid line");
			    	logger.close();
			    	System.err.println(getClass().getName() + " Failed to read parameters, invalid line");
			    	dialogManagerSvc.showAlertMessageDialog("Failed to read parameters, invalid line");
					System.exit(-1);
			    }
			}
			if (paramAmount != 10) {
				logger.LogErrorMessege("Missing parameter: Only " + paramAmount + " parameters were loaded");
				logger.close();
				if (paramAmount == 0) {
					System.err.println("Parameters haven't been found.\nVerify you've open a configuration file.");
					dialogManagerSvc.showAlertMessageDialog("Parameters haven't been found.\nVerify you've open a configuration file.");
				}
				else {
					System.err.println("Missing parameter: Only " + paramAmount + " parameters were loaded\nVerify you've open a configuration file.");
					dialogManagerSvc.showAlertMessageDialog("Missing parameter: Only " + paramAmount + " parameters were loaded"
												+ "\nVerify you've open a configuration file.");
				}
				System.exit(-1);
			}
			
			logger.LogGeneralMessege("All parameter loaded, configuration was successfully loaded");
		}
		catch (Exception e) {
			logger.LogErrorMessege("Unexpected Error:");
			logger.LogErrorMessege(e.getMessage());
			logger.close();
			dialogManagerSvc.showErrorMessageDialog(getClass().getName() + " Unexpected Error", e);
			System.exit(-1);
		}
		
		param_loaded = true;
		Reset();
	}
	
	static int _MIN_PWM_RANGE = 0;
	static int _MAX_PWM_RANGE = 0;
	static int _MIN_PWM_ANGLE = 0;
	static int _MAX_PWM_ANGLE = 0;
	
	static int _TRIM_ANGLE = 0;
	
	static int _TRIM_ANGLE_PITCH = 0;
	static int _PITCH_STEP = 0;
	
	static int _TRIM_ANGLE_ROLL = 0;
	static int _ROLL_STEP = 0;
	
	static int _TRIM_ANGLE_YAW = 0;
	static int _YAW_STEP = 0;
	
	static int _THR_STEP = 0;
	static int _INIT_THR = 0;
	
	private static int constrain(int val, int min, int max){
		if (val < min) {
			return min;
		}
		
		if (val > max){
			return max;
		}
		
		return val;
	}
	
	//RCValues set
	static int RC_Min_Thr = 0;
	//static int RC_Initial_Thr = 0;
	static int RC_Thr = _INIT_THR;
	static int RC_Yaw = 0;
	static int RC_Pitch = 0;
	static int RC_Roll = 0;
	
	public void ResetRCSet() {
		RC_Thr = _INIT_THR;
		RC_Yaw = _TRIM_ANGLE_YAW;
		RC_Pitch = _TRIM_ANGLE_PITCH;
		RC_Roll = _TRIM_ANGLE_ROLL;
	}
	
	public void ReduceRCSet() {		
		if (LastContolKeyTS == 0)
			return;
		
		long CurrentTS = (new Date()).getTime();
		
		long gap = CurrentTS - LastContolKeyTS;
		if (gap < 500 && gap > 0)
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
	
	static int last_RC_Roll = _TRIM_ANGLE_ROLL; 
	static int last_RC_Pitch = _TRIM_ANGLE_PITCH;
	static int last_RC_Thr = 0;
	static int last_RC_Yaw = _TRIM_ANGLE_YAW;
	
	public String toString() {
		return last_RC_Roll + " " + last_RC_Pitch + " " + last_RC_Thr + " " + last_RC_Yaw;
	}
	
	public String GetRCSet() {
		//Roll, Pitch, Throttle, Yaw
		
		if (RC_Roll == last_RC_Roll && RC_Pitch == last_RC_Pitch && RC_Yaw == last_RC_Yaw && RC_Thr == last_RC_Thr){
			return "";
		}
		
		last_RC_Roll = RC_Roll;
		last_RC_Thr = RC_Thr;
		last_RC_Pitch = RC_Pitch;
		last_RC_Yaw = RC_Yaw;
		
		return RC_Roll + "," + RC_Pitch + "," + RC_Thr + "," + RC_Yaw;
	}
	
	static long LastContolKeyTS = 0;
	@SuppressWarnings("incomplete-switch")
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
		}
		
		String val = GetRCSet();
		
		if (! val.isEmpty()) {
			serialConnection.write(val);
			System.out.println("Sending '" + val + "'");
			int[] rcOutputs = {RC_Roll, RC_Pitch, RC_Thr, RC_Yaw, 0, 0, 0, 0};
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
			int[] rcOutputs = {RC_Roll, RC_Pitch, RC_Thr, RC_Yaw, 0, 0, 0, 0};
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
