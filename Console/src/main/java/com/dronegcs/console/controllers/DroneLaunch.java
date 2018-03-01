package com.dronegcs.console.controllers;

import com.dronegcs.console.flightControllers.KeyBoardController;
import com.dronegcs.console_plugin.services.GlobalStatusSvc;
import com.generic_tools.devices.SerialConnection;
import com.generic_tools.environment.Environment;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication
public class DroneLaunch extends AbstractJavaFxApplicationSupport {

    private final Logger LOGGER = LoggerFactory.getLogger(DroneLaunch.class);

	protected static final String STYLE_FILE = "/com/dronegcs/console/application.css";
	private static final int WIDTH = 800;
	private static final int HEIGHT = 650;

    @Autowired
    private GlobalStatusSvc globalStatus;

    @Autowired
    private GuiAppConfig guiAppConfig;

    @Autowired
	private Environment environment;

	@Autowired
	private KeyBoardController keyBoardController;

	@Autowired
	private SerialConnection serialConnection;

	private Stage stage;

    @Override
    public void start(Stage primaryStage) {
		try {
			LOGGER.debug(StringUtils.repeat("-", 20));
			LOGGER.debug(StringUtils.repeat("-", 20));
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

			Thread a = new Thread(() -> {
				LOGGER.debug("Loading library for usb connection");
				serialConnection.listPorts();
			});
			a.start();

//			guiAppConfig.setPrimaryStage(primaryStage);
			stage = primaryStage;
			guiAppConfig.setPrimaryStage(stage);
//			guiAppConfig.showMainScreen();
			showMainScreen();
		}
		catch (Throwable e) {
			LOGGER.error("Terminating launch", e);
			System.exit(-1);
		}
    }

	private void showMainScreen() {
		Parent root = (Parent) guiAppConfig.load("/com/dronegcs/console/views/DashboardView.fxml");
//        root.setStyle("-fx-background-color: whitesmoke;");
		root.getStylesheets().add(STYLE_FILE);
		Scene scene = new Scene(root, WIDTH, HEIGHT);
		//scene.getStylesheets().add("talma.css");
		scene.setOnKeyPressed(keyBoardController);
		stage.setResizable(false);
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.show();
//		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//			@Override
//			public void handle(WindowEvent event) {
//				applicationContext.close();
//			}
//		});
		stage.setOnCloseRequest(guiAppConfig);
	}

    public static void main(String[] args) {
		System.setProperty("LOGS.DIR", args[0]);
		System.setProperty("CONF.DIR", args[1]);
		System.out.println("Logs directory set to: " + System.getProperty("LOGS.DIR"));
		System.out.println("Configuration directory set to: " + System.getProperty("CONF.DIR"));
		launchApp(DroneLaunch.class, args);
    }

}
