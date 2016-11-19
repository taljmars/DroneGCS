package gui.core.Main;

import gui.core.dashboard.Dashboard;
import gui.core.springConfig.AppConfig;
import gui.is.KeyBoardControler;
import gui.is.services.DialogManagerSvc;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tools.validations.RuntimeValidator;

public class Main extends Application {
	
	public static void main(String[] args) {
        launch(args);
    }
	
	@Override
    public void start(Stage primaryStage) {
		if (AppConfig.DEBUG_SYMBOL.equals(System.getenv(AppConfig.ENV_SYMBOL)))
			AppConfig.DebugMode = true;
		
        Dashboard dashboard = (Dashboard) AppConfig.context.getBean("dashboard");
        DialogManagerSvc dialogManager = (DialogManagerSvc) AppConfig.context.getBean("dialogManagerSvc");
    	RuntimeValidator validator = (RuntimeValidator) AppConfig.context.getBean("validator");
    	
        dashboard.setViewManager(primaryStage);
        if (!validator.validate(dashboard)) {
			dialogManager.showAlertMessageDialog("Critical error occur, failed to find running path");
			return;
		}
        Scene scene = new Scene(dashboard, 300, 250);
        KeyBoardControler keyboardControler = (KeyBoardControler) AppConfig.context.getBean("keyBoardControler");
        scene.setOnKeyPressed(keyboardControler);       
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

}
