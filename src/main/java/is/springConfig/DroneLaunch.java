package is.springConfig;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import gui.controllers.dashboard.Dashboard;
import is.devices.KeyBoardController;
import is.logger.Logger;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tools.os_utilities.Environment;

public class DroneLaunch extends Application {
	
	@Override
    public void start(Stage primaryStage) {
		try {
			if (AppConfig.DEBUG_SYMBOL.equals(System.getenv(AppConfig.ENV_SYMBOL)))
				AppConfig.DebugMode = true;

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

			Environment.setBaseRunningDirectoryByClass(this.getClass());
			System.err.println("Setting Running base directory as '" + Environment.getRunningEnvBaseDirectory() + "'");

	        Dashboard dashboard = AppConfig.context.getBean(Dashboard.class);
	        //DroneDbCrudSvc droneDbCrudSvc = (DroneDbCrudSvc) AppConfig.context.getBean("droneDbCrudSvc");
	        //System.out.println(droneDbCrudSvc.CheckConnection() + " ASASDASDASDASD");
	        dashboard.setViewManager(primaryStage);
	        Parent root = (Parent) AppConfig.loader.load("/views/DashboardView.fxml");
			root.setStyle("-fx-background-color: whitesmoke;");
			Scene scene = new Scene(root, 800, 650);
			scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
	        KeyBoardController keyboardController = AppConfig.context.getBean(KeyBoardController.class);
	        scene.setOnKeyPressed(keyboardController);
	        primaryStage.setResizable(false);
	        primaryStage.setScene(scene);
	        primaryStage.setMaximized(true);
	        primaryStage.show();
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
