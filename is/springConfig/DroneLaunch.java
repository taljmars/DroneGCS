package springConfig;

import java.net.URISyntaxException;

import controllers.dashboard.Dashboard;
import devices.KeyBoardControler;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tools.os_utilities.Environment;

public class DroneLaunch extends Application {
	//private static final AppConfig loader = new AppConfig();
	
	@Override
    public void start(Stage primaryStage) {
		try {
			if (AppConfig.DEBUG_SYMBOL.equals(System.getenv(AppConfig.ENV_SYMBOL)))
				AppConfig.DebugMode = true;
			
			Environment.setBaseRunningDirectoryByClass(this.getClass());
			System.err.println("Setting Running base directory as '" + Environment.getRunningEnvBaseDirectory() + "'");
			
	        Dashboard dashboard = (Dashboard) AppConfig.context.getBean("dashboard");
	        dashboard.setViewManager(primaryStage);
	        Parent root = (Parent) AppConfig.loader.load("/views/DashboardView.fxml");
			root.setStyle("-fx-background-color: whitesmoke;");
			Scene scene = new Scene(root, 800, 650);
			scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
	        KeyBoardControler keyboardControler = (KeyBoardControler) AppConfig.context.getBean("keyBoardControler");
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
