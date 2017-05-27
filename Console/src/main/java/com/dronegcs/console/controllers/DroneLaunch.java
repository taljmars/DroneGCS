package com.dronegcs.console.controllers;

import com.dronegcs.console_plugin.services.GlobalStatusSvc;
import com.generic_tools.environment.Environment;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication
public class DroneLaunch extends AbstractJavaFxApplicationSupport {
    private final static Logger LOGGER = LoggerFactory.getLogger(DroneLaunch.class);

    @Autowired
    private GlobalStatusSvc globalStatus;

    @Autowired
    private GuiAppConfig guiAppConfig;

    @Autowired
	private Environment environment;

    @Override
    public void start(Stage primaryStage) {
		try {
			//Validating serial device exists
			if (Paths.get("/dev/ttyACM0").toFile().exists() &&
					!Paths.get("/dev/ttyS85").toFile().exists() ) {
				System.err.println("************************************************************************");
				System.err.println("************************************************************************\n\n");
				System.err.println("Soft link to Arduino doesn't exist, please run the following command "
						+ "in order to set it right:\n'sudo ln -s /dev/ttyACM0 /dev/ttyS85' and run it again");
				System.err.println("************************************************************************");
				System.err.println("************************************************************************\n\n");
				//System.exit(-1);
			}
			else {
				globalStatus.setComponentStatus(GlobalStatusSvc.Component.ANTENNA, true);
			}

			guiAppConfig.setPrimaryStage(primaryStage);
			guiAppConfig.showMainScreen();

		}
		catch (Throwable e) {
			e.printStackTrace();
			System.err.println("Terminating launch, " + e.getMessage());
			System.exit(-1);
		}
    }

    public static void main(String[] args) {
    	launchApp(DroneLaunch.class, args);
    }

}
