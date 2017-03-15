package com.dronegcs.mavlink.core.flightControllers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import com.dronegcs.gcsis.environment.Environment;
import com.dronegcs.gcsis.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
public class KeyBoardConfigurationParser {
	
	@Autowired @NotNull( message = "Internal Error: Failed to get com.dronegcs.gcsis.logger" )
	private Logger logger;

	@Autowired @NotNull( message = "Internal Error: Failed to get com.dronegcs.gcsis.environment" )
	Environment environment;
	
	private final String settingsFileName = "quad_setup_arducopter.txt";
	private Path fFilePath = null;
	private int paramAmount = 0;
	private final static Charset ENCODING = StandardCharsets.UTF_16;

	private static String CONF_FILE_DELIMETER = "=";
	
	private final static String _STABILIZER_CYCLE_KEY = "_STABILIZER_CYCLE";
	private final static int _STABILIZER_CYCLE_DEFAULT = 300;
	private int _STABILIZER_CYCLE = 0;
	
	private final static String _MIN_PWM_RANGE_KEY = "_MIN_PWM_RANGE";
	private final static int _MIN_PWM_RANGE_DEFAULT = 1000;
	private int _MIN_PWM_RANGE = 0;
	
	private final static String _MAX_PWM_RANGE_KEY = "_MAX_PWM_RANGE";
	private final static int _MAX_PWM_RANGE_DEFAULT = 2200;
	private int _MAX_PWM_RANGE = 0;
	
	private final static String _MIN_PWM_ANGLE_KEY = "_MIN_PWM_ANGLE";
	private final static int _MIN_PWM_ANGLE_DEFAULT = 1100;
	private int _MIN_PWM_ANGLE = 0;
	
	private final static String _MAX_PWM_ANGLE_KEY = "_MAX_PWM_ANGLE";
	private final static int _MAX_PWM_ANGLE_DEFAULT = 1900;
	private int _MAX_PWM_ANGLE = 0;
	
	private final static String _TRIM_ANGLE_KEY = "_TRIM_ANGLE";
	private final static int _TRIM_ANGLE_DEFAULT = 1500;
	private int _TRIM_ANGLE = 0;
	
	private final static String _PITCH_STEP_KEY = "_PITCH_STEP";
	private final static int _PITCH_STEP_DEFAULT = 10;
	private int _PITCH_STEP = 0;
	private int _TRIM_ANGLE_PITCH = 0;
	
	private final static String _ROLL_STEP_KEY = "_ROLL_STEP";
	private final static int _ROLL_STEP_DEFAULT = 10;
	private int _ROLL_STEP = 0;
	private int _TRIM_ANGLE_ROLL = 0;
	
	private final static String _YAW_STEP_KEY = "_YAW_STEP";
	private final static int _YAW_STEP_DEFAULT = 50;
	private int _YAW_STEP = 0;
	private int _TRIM_ANGLE_YAW = 0;
	
	private final static String _THR_STEP_KEY = "_THR_STEP";
	private final static int _THR_STEP_DEFAULT = 25;
	private int _THR_STEP = 0;
	
	private final static String _INIT_THR_KEY = "_INIT_THR";
	private final static int _INIT_THR_DEFAULT = 1100;
	private int _INIT_THR = 0;
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}

	@SuppressWarnings("resource")
	public void LoadParams() {
		logger.LogGeneralMessege("Loading flight controller configuration");
		paramAmount = 0;
		try {
			//fFilePath = Paths.get(Environment.getRunningEnvConfDirectory() + Environment.DIR_SEPERATOR + settingsFileName);
			fFilePath = Paths.get(environment.getRunningEnvConfDirectory() + Environment.DIR_SEPERATOR + settingsFileName);
			if (fFilePath.toFile().exists() == false) {
				logger.LogErrorMessege("Configuration file wasn't found, start generating default conf file");
				buildConfigurationFile(fFilePath);
			}
		
			Scanner scanner =  new Scanner(fFilePath, ENCODING.name());
			while (scanner.hasNextLine()) {
				Scanner lineScanner = new Scanner(scanner.nextLine());
				lineScanner.useDelimiter(CONF_FILE_DELIMETER);
			    if (lineScanner.hasNext()) {
			    	// assumes the line has a certain structure
			    	String name = lineScanner.next();
			    	String value = lineScanner.hasNext() ? lineScanner.next() : "";
			      
			    	if (name.equals(_MIN_PWM_RANGE_KEY)) {
			    		_MIN_PWM_RANGE = Integer.parseInt(value);
			    		logger.LogGeneralMessege(_MIN_PWM_RANGE_KEY + CONF_FILE_DELIMETER + _MIN_PWM_RANGE);
			    		paramAmount++;
			    	}
			    	if (name.equals(_MAX_PWM_RANGE_KEY)) {
			    		_MAX_PWM_RANGE = Integer.parseInt(value);
			    		logger.LogGeneralMessege(_MAX_PWM_RANGE_KEY + CONF_FILE_DELIMETER + _MAX_PWM_RANGE);
			    		paramAmount++;
			    	}
			    	if (name.equals(_MIN_PWM_ANGLE_KEY)) {
			    		_MIN_PWM_ANGLE = Integer.parseInt(value);
			    		logger.LogGeneralMessege(_MIN_PWM_ANGLE_KEY + CONF_FILE_DELIMETER + _MIN_PWM_ANGLE);
			    		paramAmount++;
			    	}
			    	if (name.equals(_MAX_PWM_ANGLE_KEY)) {
			    		_MAX_PWM_ANGLE = Integer.parseInt(value);
			    		logger.LogGeneralMessege(_MAX_PWM_ANGLE_KEY + CONF_FILE_DELIMETER + _MAX_PWM_ANGLE);
			    		paramAmount++;
			    	}
			    	if (name.equals(_TRIM_ANGLE_KEY)) {
			    		_TRIM_ANGLE = Integer.parseInt(value);
			    		_TRIM_ANGLE_PITCH = _TRIM_ANGLE_ROLL = _TRIM_ANGLE_YAW = _TRIM_ANGLE;
			    		logger.LogGeneralMessege(_TRIM_ANGLE_KEY + CONF_FILE_DELIMETER + _TRIM_ANGLE);
			    		paramAmount++;
			    	}
			    	if (name.equals(_PITCH_STEP_KEY)) {
			    		_PITCH_STEP = Integer.parseInt(value);
			    		logger.LogGeneralMessege(_PITCH_STEP_KEY + CONF_FILE_DELIMETER + _PITCH_STEP);
			    		paramAmount++;
			    	}
			    	if (name.equals(_ROLL_STEP_KEY)) {
			    		_ROLL_STEP = Integer.parseInt(value);
			    		logger.LogGeneralMessege(_ROLL_STEP_KEY + CONF_FILE_DELIMETER + _ROLL_STEP);
			    		paramAmount++;
			    	}
			    	if (name.equals(_YAW_STEP_KEY)) {
			    		_YAW_STEP = Integer.parseInt(value);
			    		logger.LogGeneralMessege(_YAW_STEP_KEY + CONF_FILE_DELIMETER + _YAW_STEP);
			    		paramAmount++;
			    	}
			    	if (name.equals(_THR_STEP_KEY)) {
			    		_THR_STEP = Integer.parseInt(value);
			    		logger.LogGeneralMessege(_THR_STEP_KEY + CONF_FILE_DELIMETER + _THR_STEP);
			    		paramAmount++;
			    	}
			    	if (name.equals(_INIT_THR_KEY)) {
			    		_INIT_THR = Integer.parseInt(value);
			    		logger.LogGeneralMessege(_INIT_THR_KEY + CONF_FILE_DELIMETER + _INIT_THR);
			    		paramAmount++;
			    	}
			    	
			    	if (name.equals(_STABILIZER_CYCLE_KEY)) {
			    		_STABILIZER_CYCLE = Integer.parseInt(value);
			    		logger.LogGeneralMessege(_STABILIZER_CYCLE_KEY + CONF_FILE_DELIMETER + _STABILIZER_CYCLE);
			    		paramAmount++;
			    	}
			    	
			    	System.out.println("Param: '" + name + "', Value: '" + value + "'");
			    }
			    else {
			    	logger.LogErrorMessege("Failed to read parameters, invalid line");
			    	logger.close();
			    	System.err.println(getClass().getName() + " Failed to read parameters, invalid line");
					logger.LogAlertMessage("Failed to read parameters, invalid line");
					System.exit(-1);
			    }
			}
			if (paramAmount != 11) {
				logger.LogErrorMessege("Missing parameter: Only " + paramAmount + " parameters were loaded");
				logger.close();
				if (paramAmount == 0) {
					System.err.println("Parameters haven't been found.\nVerify configuration file validity.");
					logger.LogAlertMessage("Parameters haven't been found.\nVerify configuration file validity.");
				}
				else {
					System.err.println("Missing parameter: Only " + paramAmount + " parameters were loaded\nVerify configuration file validity.");
					logger.LogAlertMessage("Missing parameter: Only " + paramAmount + " parameters were loaded"
												+ "\nVerify configuration file validity.");
				}
				System.exit(-1);
			}
			
			logger.LogGeneralMessege("All parameter loaded, configuration was successfully loaded");
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.LogAlertMessage("Configuration file is missing, failed to build a default one", e);
			logger.close();
			System.exit(-1);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			logger.LogAlertMessage("Failed to file running com.dronegcs.gcsis.environment", e);
			logger.close();
			System.exit(-1);
		}
	}
	
	private void buildConfigurationFile(Path path) throws FileNotFoundException, UnsupportedEncodingException {
		System.out.println("Building default configuration file: " + fFilePath.toString());
		
		PrintWriter printWriter = new PrintWriter(path.toString(), ENCODING.name());
		printWriter.println(_STABILIZER_CYCLE_KEY 	+ CONF_FILE_DELIMETER + _STABILIZER_CYCLE_DEFAULT);
		printWriter.println(_MIN_PWM_RANGE_KEY 		+ CONF_FILE_DELIMETER + _MIN_PWM_RANGE_DEFAULT);
		printWriter.println(_MAX_PWM_RANGE_KEY 		+ CONF_FILE_DELIMETER + _MAX_PWM_RANGE_DEFAULT);
		printWriter.println(_MIN_PWM_ANGLE_KEY 		+ CONF_FILE_DELIMETER + _MIN_PWM_ANGLE_DEFAULT);
		printWriter.println(_MAX_PWM_ANGLE_KEY 		+ CONF_FILE_DELIMETER + _MAX_PWM_ANGLE_DEFAULT);
		printWriter.println(_TRIM_ANGLE_KEY 		+ CONF_FILE_DELIMETER + _TRIM_ANGLE_DEFAULT);
		printWriter.println(_PITCH_STEP_KEY 		+ CONF_FILE_DELIMETER + _PITCH_STEP_DEFAULT);
		printWriter.println(_ROLL_STEP_KEY 			+ CONF_FILE_DELIMETER + _ROLL_STEP_DEFAULT);
		printWriter.println(_YAW_STEP_KEY 			+ CONF_FILE_DELIMETER + _YAW_STEP_DEFAULT);
		printWriter.println(_THR_STEP_KEY 			+ CONF_FILE_DELIMETER + _THR_STEP_DEFAULT);
		printWriter.println(_INIT_THR_KEY 			+ CONF_FILE_DELIMETER + _INIT_THR_DEFAULT);
		printWriter.close();
	}

	public int getTrimAngleRoll() {
		return _TRIM_ANGLE_ROLL;
	}
	
	public int getRollStep() {
		return _ROLL_STEP;
	}

	public int getStabilizerCycle() {
		return _STABILIZER_CYCLE;
	}

	public int getMinPwmRange() {
		return _MIN_PWM_RANGE;
	}

	public int getMaxPwmRange() {
		return _MAX_PWM_ANGLE;
	}

	public int getMinPwmAngle() {
		return _MIN_PWM_ANGLE;
	}
	
	public int getMaxPwmAngle() {
		return _MAX_PWM_ANGLE;
	}

	public int getTrimAngle() {
		return _TRIM_ANGLE;
	}

	public int getPitchStep() {
		return _PITCH_STEP;
	}

	public int getTrimAnglePitch() {
		return _TRIM_ANGLE_PITCH;
	}

	public int getYawStep() {
		return _YAW_STEP;
	}
	
	public int getTrimAngleYaw() {
		return _TRIM_ANGLE_YAW;
	}

	public int getThrustStep() {
		return _THR_STEP;
	}

	public int getInitThrust() {
		return _INIT_THR;
	}

}
