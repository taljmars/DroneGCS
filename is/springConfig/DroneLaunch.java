package springConfig;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import controllers.dashboard.Dashboard;
import devices.KeyBoardController;
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
				System.err.println("Soft link to Arduino doesn't exist, please run the following command "
						+ "in order to set it right:\n'sudo ln -s /dev/ttyACM0 /dev/ttyS85' and run it again");
			}
			
			Environment.setBaseRunningDirectoryByClass(this.getClass());
			System.err.println("Setting Running base directory as '" + Environment.getRunningEnvBaseDirectory() + "'");
			
	        Dashboard dashboard = (Dashboard) AppConfig.context.getBean("dashboard");
	        dashboard.setViewManager(primaryStage);
	        Parent root = (Parent) AppConfig.loader.load("/views/DashboardView.fxml");
			root.setStyle("-fx-background-color: whitesmoke;");
			Scene scene = new Scene(root, 800, 650);
			scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
	        KeyBoardController keyboardControler = (KeyBoardController) AppConfig.context.getBean("keyBoardController");
	        scene.setOnKeyPressed(keyboardControler);       
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
