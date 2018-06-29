package com.dronegcs.console.flightControllers;

import com.generic_tools.environment.Environment;
import com.generic_tools.logger.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Component
public class KeyBoardConfigurationParser {

	private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(KeyBoardConfigurationParser.class);
	
	@Autowired @NotNull( message = "Internal Error: Failed to get logger" )
	private Logger logger;

	@Autowired @NotNull( message = "Internal Error: Failed to get environment" )
	private Environment environment;
	
	private final String settingsFileName = "quad_setup_arducopter.txt";
	private Path fFileFullPath = null;
	private int paramAmount = 0;
	private final static Charset ENCODING = StandardCharsets.UTF_16;

	private static String CONF_FILE_DELIMETER = "=";

	static int called;
	@PostConstruct
	public void init() {
		Assert.isTrue(++called == 1, "Not a Singleton");
	}

	@SuppressWarnings("resource")
	public KeyBoardRcValues parse() {
		logger.LogGeneralMessege("Loading flight controller configuration");
		LOGGER.debug("Loading flight controller configuration");
		paramAmount = 0;
		KeyBoardRcValues keyBoardRcValues = new KeyBoardRcValues();
		try {
			//fFileFullPath = Paths.get(Environment.getRunningEnvConfDirectory() + Environment.DIR_SEPERATOR + settingsFileName);
			fFileFullPath = Paths.get(System.getProperty("CONF.DIR") + Environment.DIR_SEPERATOR + settingsFileName);
			if ( ! fFileFullPath.toFile().exists()) {
				logger.LogErrorMessege("Configuration file wasn't found, start generating default conf file");
				LOGGER.error("Configuration file wasn't found, start generating default conf file");
				buildConfigurationFile(fFileFullPath, KeyBoardRcValues.generateDefault());
			}

			Field[] fields = KeyBoardRcValues.class.getDeclaredFields();
			Map<String, Field> fieldMap = new HashMap<>();
			for (Field field : fields) fieldMap.put(field.getName(), field);
		
			Scanner scanner =  new Scanner(fFileFullPath, ENCODING.name());
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.startsWith("#")) {
					LOGGER.debug("Commented line '{}'", line);
					continue;
				}
				if (!line.contains(CONF_FILE_DELIMETER)) {
					LOGGER.debug("Irrelevant line found '{}'", line);
					continue;
				}
				Scanner lineScanner = new Scanner(line);
				lineScanner.useDelimiter(CONF_FILE_DELIMETER);
			    if (lineScanner.hasNext()) {
			    	// assumes the line has a certain structure
			    	String name = lineScanner.next();
			    	String value = lineScanner.hasNext() ? lineScanner.next() : "";

					if (!fieldMap.containsKey(name)) {
						LOGGER.error("A Key was found in configuration file but doesn't exist in the class itself");
						throw new Exception("Failed to find field named '" + name + "'");
					}

					KeyBoardRcValues.execSetter(keyBoardRcValues, fieldMap.get(name), Integer.parseInt(value));
					Object val = KeyBoardRcValues.execGetter(keyBoardRcValues, fieldMap.get(name));
					logger.LogGeneralMessege(name + CONF_FILE_DELIMETER + val);
					paramAmount++;

					LOGGER.debug("Param: {}, Value: {}", name, value);
					lineScanner.close();
			    }
			    else {
					lineScanner.close();
					throw new Exception("Failed to read parameters, invalid line");
			    }
			}
			if (!keyBoardRcValues.isInitialized()) {
				logger.LogErrorMessege("Missing parameter: Only " + paramAmount + " parameters were loaded");
				logger.close();
				if (paramAmount == 0)
					throw new Exception("Parameters haven't been found.\nVerify configuration file validity.");

				throw new Exception("Missing parameter: Only " + paramAmount + " parameters were loaded" + "\nVerify configuration file validity.");
			}
			
			logger.LogGeneralMessege("All parameter loaded, configuration was successfully loaded");
			scanner.close();
			return keyBoardRcValues;
		}
		catch (IOException e) {
			LOGGER.error("Configuration file is missing", e);
			logger.LogAlertMessage("Configuration file is missing, failed to build a default one", e);
			logger.close();
			System.exit(-1);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			LOGGER.error("Reflection error, failed to handle configuration file", e);
			logger.LogAlertMessage("Failed to handle configuration file", e);
			logger.close();
			System.exit(-1);
		} catch (Exception e) {
			LOGGER.error(e.getMessage() ,e);
			logger.LogErrorMessege(e.getMessage());
			logger.close();
			System.exit(-1);
		}
		return null;
	}

	public void dump(KeyBoardRcValues keyBoardRcValues) throws Exception {
		try {
			fFileFullPath = Paths.get(System.getProperty("CONF.DIR") + Environment.DIR_SEPERATOR + settingsFileName);
			buildConfigurationFile(fFileFullPath, keyBoardRcValues);
		}
		catch (InvocationTargetException | FileNotFoundException | NoSuchMethodException | IllegalAccessException | UnsupportedEncodingException e) {
			LOGGER.error("Failed to write file", e);
			throw new Exception("Failed to write file");
		}
	}
	
	private void buildConfigurationFile(Path fileFullPath, KeyBoardRcValues keyBoardRcValues) throws FileNotFoundException, UnsupportedEncodingException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		LOGGER.debug("Building default configuration file: " + fFileFullPath.toString());

		File directoryOfFile = fileFullPath.getParent().toFile();
		if (!directoryOfFile.exists()) {
			LOGGER.warn("Creating sub directories: " + directoryOfFile);
			directoryOfFile.mkdirs();
		}
		
		PrintWriter printWriter = new PrintWriter(fileFullPath.toString(), ENCODING.name());
		Field[] fields = KeyBoardRcValues.class.getDeclaredFields();

		for (Field field : fields) {
			if (!KeyBoardRcValues.isSerializable(field))
				continue;

			Object val = KeyBoardRcValues.execGetter(keyBoardRcValues, field);
			printWriter.println(field.getName() + CONF_FILE_DELIMETER + val);
		}

		printWriter.close();
	}

}
