package com.dronegcs.console.controllers;

import com.db.persistence.scheme.*;
import com.dronegcs.console.flightControllers.KeyBoardController;
import com.dronegcs.console_plugin.event_logger.EventLogManager;
import com.dronegcs.console_plugin.remote_services_wrappers.LoginSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.RegistrationSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.RestClientHelper;
import com.dronegcs.console_plugin.services.GlobalStatusSvc;
import com.generic_tools.devices.SerialConnection;
import com.generic_tools.environment.Environment;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class DroneLaunch extends AbstractJavaFxApplicationSupport implements DroneLaunchPreloader.LoginLoader {

    private final Logger LOGGER = LoggerFactory.getLogger(DroneLaunch.class);

	protected static final String STYLE_FILE = "/com/dronegcs/console/application.css";

	@Autowired
	private ActiveUserProfile activeUserProfile;

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

	@Autowired
	private LoginSvcRemoteWrapper loginSvcRemoteWrapper;

	@Autowired
	private RegistrationSvcRemoteWrapper registrationSvcRemoteWrapper;

	@Autowired
	private RestClientHelper restClientHelper;

	@Autowired
	private EventLogManager eventLogManager;

	private Stage mainStage;

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
			mainStage = primaryStage;
			guiAppConfig.setPrimaryStage(mainStage);
//			guiAppConfig.showMainScreen();
//			showMainScreen();

			//Bind to window resizing
			mainStage.widthProperty().addListener((obs, oldVal, newVal) -> GUISettings._WIDTH.set(((Double) newVal).intValue()));
			mainStage.heightProperty().addListener((obs, oldVal, newVal) -> GUISettings._HEIGHT.set(((Double) newVal).intValue()));
		}
		catch (Throwable e) {
			LOGGER.error("Terminating launch", e);
			System.exit(-1);
		}
    }

    @Override
    public LoginResponse handleLogin(String userName, String password, String server, int port) {
		LoginRequest loginRestRequest = new LoginRequest();
		loginRestRequest.setUserName(userName);
		loginRestRequest.setApplicationName("DroneGCS GUI");
		loginRestRequest.setTimeout(100);
		LoginResponse loginRestResponse = loginSvcRemoteWrapper.login(loginRestRequest, password, server, port);
        if (loginRestResponse.getReturnCode().equals(LoginLogoutStatus.OK)){
			restClientHelper.setToken(loginRestResponse.getToken());
			activeUserProfile.setMode(ActiveUserProfile.Mode.ONLINE);
			activeUserProfile.setUsername(userName);
            showMainScreen();

            // Logged in, sync all recent events
			eventLogManager.SyncTracker();
            return loginRestResponse;
        }

        return loginRestResponse;
	}

	@Override
	public RegistrationResponse handleRegisterNewUser(String userName, String password, String serverIp, int serverPort) {
		RegistrationRequest registrationRequest = new RegistrationRequest();
		registrationRequest.setUserName(userName);
		registrationRequest.setPassword(password);
		return registrationSvcRemoteWrapper.registerNewUser(registrationRequest, serverIp, serverPort);
	}

    @Override
    public void handleOffline(String username) {
        restClientHelper.setToken(null);
		activeUserProfile.setMode(ActiveUserProfile.Mode.OFFLINE);
		activeUserProfile.setUsername(username);
        showMainScreen();
    }

    private void showMainScreen() {
		Parent root = (Parent) guiAppConfig.load("/com/dronegcs/console/views/DashboardView2.fxml");
//        root.setStyle("-fx-background-color: whitesmoke;");
		root.getStylesheets().add(STYLE_FILE);
//		Scene scene = new Scene(root, WIDTH, HEIGHT);
		Scene scene = new Scene(root);
		//scene.getStylesheets().add("talma.css");
		scene.setOnKeyPressed(keyBoardController);
//		mainStage.setResizable(false);
		mainStage.setScene(scene);
		mainStage.setMaximized(true);
		mainStage.show();

		mainStage.setOnCloseRequest(guiAppConfig);

		mainStage.setX(0);
		mainStage.setY(0);
		mainStage.setWidth(Screen.getPrimary().getVisualBounds().getMaxX());
		mainStage.setHeight(Screen.getPrimary().getVisualBounds().getMaxY());
	}

    public static void main(String[] args) {
		System.setProperty("LOGS.DIR", args[0]);
		System.setProperty("CONF.DIR", args[1]);
		System.out.println("Logs directory set to: " + System.getProperty("LOGS.DIR"));
		System.out.println("Configuration directory set to: " + System.getProperty("CONF.DIR"));
		launchApp(DroneLaunch.class, args);
    }

}
