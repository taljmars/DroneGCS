package com.dronegcs.console.controllers;

import com.dronegcs.console_plugin.exceptions.ClientPluginException;
import com.dronegcs.console_plugin.services.GlobalStatusSvc;
import com.generic_tools.environment.Environment;
import javafx.application.Application;
import javafx.stage.Stage;

import java.net.URISyntaxException;
import java.nio.file.Paths;

public class DroneLaunch extends Application {
	
	@Override
    public void start(Stage primaryStage) {
		try {
			GlobalStatusSvc globalStatus= AppConfig.context.getBean(GlobalStatusSvc.class);

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

			Environment environment = AppConfig.context.getBean(Environment.class);
			environment.setBaseRunningDirectoryByClass(this.getClass());
			System.err.println("Setting Running base directory as '" + environment.getRunningEnvBaseDirectory() + "'");

			GuiAppConfig guiAppConfig = AppConfig.context.getBean(GuiAppConfig.class);

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
        launch(args);
    }

}
