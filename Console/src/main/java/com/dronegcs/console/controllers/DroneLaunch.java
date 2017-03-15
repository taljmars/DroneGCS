package com.dronegcs.console.controllers;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import com.dronegcs.gcsis.environment.Environment;
import javafx.application.Application;
import javafx.stage.Stage;

public class DroneLaunch extends Application {
	
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
				System.exit(-1);
			}

			Environment environment = AppConfig.context.getBean(Environment.class);
			environment.setBaseRunningDirectoryByClass(this.getClass());
			System.err.println("Setting Running base directory as '" + environment.getRunningEnvBaseDirectory() + "'");

			GuiAppConfig guiAppConfig = AppConfig.context.getBean(GuiAppConfig.class);

			guiAppConfig.setPrimaryStage(primaryStage);
			guiAppConfig.showMainScreen();

		}
		catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public static void main(String[] args) {
        launch(args);
    }

}
