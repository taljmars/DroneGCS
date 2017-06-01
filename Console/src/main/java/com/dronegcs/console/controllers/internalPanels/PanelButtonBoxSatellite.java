package com.dronegcs.console.controllers.internalPanels;

import com.dronegcs.console_plugin.operations.*;
import com.dronegcs.console_plugin.services.DialogManagerSvc;
import com.dronegcs.console_plugin.services.EventPublisherSvc;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.services.TextNotificationPublisherSvc;
import com.dronegcs.mavlink.core.connection.helper.GCSLocationData;
import com.dronegcs.mavlink.core.connection.helper.GCSLocationDataFactory;
import com.dronegcs.mavlink.core.flightControllers.FlightController;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnDroneListener;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnParameterManagerListener;
import com.dronegcs.mavlink.is.drone.parameters.Parameter;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmModes;
import com.dronegcs.mavlink.is.protocol.msgbuilder.MavLinkArm;
import com.dronegcs.mavlink.is.protocol.msgbuilder.MavLinkModes;
import com.generic_tools.devices.SerialConnection;
import com.generic_tools.logger.Logger;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.util.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import static com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_TYPE.MAV_TYPE_QUADROTOR;

@Component
public class PanelButtonBoxSatellite extends TilePane implements OnDroneListener, OnParameterManagerListener, Initializable {
	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PanelButtonBoxSatellite.class);
	
	@NotNull @FXML private Button btnConnect;
	@NotNull @FXML private Button btnSyncDrone;
	@NotNull @FXML private Button btnController;
	@NotNull @FXML private ToggleButton btnArm;
	@NotNull @FXML private Button btnLandRTL;
	@NotNull @FXML private Button btnTakeoff;
	@NotNull @FXML private Button btnGCSShow;
	@NotNull @FXML private Button btnHoldPosition;
	@NotNull @FXML private ToggleButton btnStartMission;
	@NotNull @FXML private ToggleButton btnStartPerimeter;
	@NotNull @FXML private Button btnFollowBeaconShow;
	@NotNull @FXML private ToggleButton btnFollowBeaconStart;
	@NotNull @FXML private Button btnExit;
	
	@NotNull @FXML private ComboBox<ApmModes> flightModesCombo;
	@NotNull @FXML private Button btnSetMode;

	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get text publisher")
	private TextNotificationPublisherSvc textNotificationPublisherSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get GUI event publisher")
	private EventPublisherSvc eventPublisherSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get quad armer")
	private OpArmQuad opArmQuad;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get GCS terminator")
	private OpGCSTerminationHandler opGCSTerminationHandler;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get takeoff handler")
	private OpTakeoffQuad opTakeoffQuad;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get droneMission handler")
	private OpStartMissionQuad opStartMissionQuad;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get flight controller service")
	private OpChangeFlightControllerQuad opChangeFlightControllerQuad;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get serial communication")
	private SerialConnection serialConnection;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger")
	private Logger logger;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;

	@Autowired @NotNull(message = "Internal Error: Failed to get gcs beacon factory")
	private GCSLocationDataFactory gcsLocationDataFactory;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	private boolean connected = false;
	private boolean takeOffThreadRunning = false;
    private Thread FollowBeaconStartThread = null;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
		
		drone.addDroneListener(this);
		drone.getParameters().addParameterListener(this);
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		setButtonControl(false);
		
		Vector<ApmModes> flightModes = new Vector<ApmModes>();
		flightModes.addAll(FXCollections.observableArrayList( ApmModes.getModeList(MAV_TYPE_QUADROTOR)));
		flightModesCombo.getItems().addAll(flightModes);

		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());
	}
	
	@FXML
	public void ButtonConnectOnAction(ActionEvent actionEvent) {
    	try {
			if (connected) {
				loggerDisplayerSvc.logGeneral("Close Connection");
				drone.getMavClient().disconnect();
			}

			Object[] ports = serialConnection.listPorts();
			if (ports.length == 0) {
				dialogManagerSvc.showAlertMessageDialog("Failed to find ports");
				return;
			}
			Pair<Object, Object> res = dialogManagerSvc.showMuliComboBoxMessageDialog("Select port: ", ports, ports[0] ,"Select baud rate: ",serialConnection.baudList(), serialConnection.getDefaultBaud());
			if (res != null) {
				String port_name = (String) res.getKey();
				Integer baud = (Integer) res.getValue();
				serialConnection.setPortName(port_name);
				serialConnection.setBaud(baud);

				loggerDisplayerSvc.logGeneral("Open Connection");
				drone.getMavClient().connect();
			}
		}
		catch (Throwable t) {
			logger.LogErrorMessege("An error occur during interface collection " + t.getMessage());
			dialogManagerSvc.showAlertMessageDialog("Failed to find ports");
		}
	}
	
	@FXML
	public void ButtonSyncOnAction(ActionEvent actionEvent) {
		LOGGER.info("Sync");
    	loggerDisplayerSvc.logGeneral("Syncing Drone parameters");
		drone.getParameters().refreshParameters();
	}
	
	@FXML
	@SuppressWarnings("deprecation")
	public void ButtonControllerOnAction(ActionEvent actionEvent) {
		try {
    		String btnControllerText = btnController.getText();
    		String imagePath = ((ImageView) (btnController.getGraphic())).getImage().impl_getUrl();
    		opChangeFlightControllerQuad.setNext(null);
    		String[] options = {FlightController.KEYBOARD.name(), FlightController.REMOTE.name()};
        	int n = dialogManagerSvc.showOptionsDialog("Choose Controller", "", null,options, options[1]);
        	if (n == 0) {
        		opChangeFlightControllerQuad.setFlightMode(FlightController.KEYBOARD);
        		btnControllerText = FlightController.KEYBOARD.name();
        		imagePath = "/com/dronegcs/console/guiImages/Keyboard.png";
        	}
        	if (n == 1) {
        		opChangeFlightControllerQuad.setFlightMode(FlightController.REMOTE);
        		btnControllerText = FlightController.REMOTE.name();
        		imagePath = "/com/dronegcs/console/guiImages/Remote.png";
        		
        	}
			if (opChangeFlightControllerQuad.go()) {
				loggerDisplayerSvc.logGeneral("Start Fly '" + options[n] + "'");
				btnController.setText(btnControllerText);
				SetImageButton(btnController, this.getClass().getResource(imagePath), btnControllerText);
			}
		} 
		catch (Exception e1) {
			logger.LogErrorMessege(e1.getMessage());
		}
	}
	
	@FXML
	public void ButtonArmDisarmOnAction(ActionEvent actionEvent) {
		if (btnArm.isSelected()) {
    		loggerDisplayerSvc.logOutgoing("arm");
    		MavLinkArm.sendArmMessage(drone, true);
    	}
    	else {
    		// Not selected
    		if (drone.getState().isFlying()) {
    			dialogManagerSvc.showAlertMessageDialog("Drone is flying, dis-arming motor is dangerous");
            	if (!TryLand())
            		btnArm.setSelected(true);
    		}
    		else {
    			loggerDisplayerSvc.logOutgoing("disarm");
    			MavLinkArm.sendArmMessage(drone, false);
    		}
    	}
	}
	
	@FXML
	public void ButtonLandRTLOnAction(ActionEvent actionEvent) {
		TryLand();
	}
	
	@FXML
	public void ButtonTakeOffOnAction(ActionEvent actionEvent) {
		if (takeOffThreadRunning) {
    		dialogManagerSvc.showAlertMessageDialog("MavlinkTakeoff procedure was already started");
    		return;
    	}

    	takeOffThreadRunning = true;
		    		
		logger.LogGeneralMessege("MavlinkTakeoff thread Stated!");
	    if (!drone.getState().isArmed()) {
	    	if (dialogManagerSvc.showAlertMessageDialog("Quad will automatically be armed")) {
	    		LOGGER.info("User notified quad was armed");
	    	}
	    }
					
		String val = dialogManagerSvc.showInputDialog("Choose altitude", "", null, null, "5");
		if (val == null) {
			LOGGER.info(getClass().getName() + " MavlinkTakeoff canceled");
			takeOffThreadRunning = false;
				logger.LogGeneralMessege("MavlinkTakeoff thread Done!");
			return;
		}
    
		Task<Void> task = new Task<Void>() {
    		@Override protected Void call() throws Exception {
        		try {
        			double real_value = Double.parseDouble(val);
        			opTakeoffQuad.setTargetHeight(real_value);
					opArmQuad.setNext(opTakeoffQuad);
					opTakeoffQuad.setNext(null);
					opArmQuad.go();
					takeOffThreadRunning = false;
	   				logger.LogGeneralMessege("MavlinkTakeoff thread Done!");
        		}
        		catch (Exception exp) {
        			exp.printStackTrace();
        			Platform.runLater( () -> dialogManagerSvc.showErrorMessageDialog("Failed to get required height for value '" + val + "'\n", exp));
        		}
    			return null;
				}
		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();

	}
	
	@FXML
	public void ButtonShowBeaconOnAction(ActionEvent actionEvent) {
		Task<Void> task = new Task<Void>() {
			@Override protected Void call() throws Exception {	       				
				drone.getBeacon().syncBeacon();
				return null;
			}
		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
	}
	
	@FXML
	public void ButtonFollowBeaconOnAction(ActionEvent actionEvent) {
		if (btnFollowBeaconStart.isSelected()) {
    		if (!drone.getState().isArmed()) {
    			if (dialogManagerSvc.showAlertMessageDialog("Quad will automatically be armed")) {
    				LOGGER.info("User was notified about arming quad");
    			}
    		}
        		
    		if (drone.getState().isFlying()) {
    			drone.getFollow().toggleFollowMeState();
    			return;
    		}
        		
    		String val = dialogManagerSvc.showInputDialog("Choose altitude", "", null, null, "5");
    		if (val == null) {
    			loggerDisplayerSvc.logError("MavlinkTakeoff canceled");
    			btnFollowBeaconStart.setSelected(false);				        			
    			return;
    		}
        		
    		Task<Void> task = new Task<Void>() {
    			@Override protected Void call() throws Exception {	        		
        		try {
        			double real_value = Double.parseDouble((String) val);
        			opArmQuad.setNext(opTakeoffQuad);
        			opTakeoffQuad.setTargetHeight(real_value);
        			opTakeoffQuad.setNext(null);
        			
					if (!opArmQuad.go()) {
						btnFollowBeaconStart.setSelected(false);
						return null;
					}
        			
        			drone.getFollow().toggleFollowMeState();
        		}
        		catch (InterruptedException e) {
        			Platform.runLater( () -> dialogManagerSvc.showAlertMessageDialog("Beacon lock operation was canceled"));
        		}
        		catch (Exception e) {
        			Platform.runLater( () -> dialogManagerSvc.showErrorMessageDialog("Failed to get required height for value '" + val + "'", e));
        		}
				return null;
			}
			};
			FollowBeaconStartThread = new Thread(task);
			FollowBeaconStartThread.setDaemon(true);
			FollowBeaconStartThread.start();
		}
		else {
			// Not selected
			drone.getFollow().toggleFollowMeState();
			loggerDisplayerSvc.logError("Lock On Beacon is not selected");
    		if (FollowBeaconStartThread != null)
    			FollowBeaconStartThread.interrupt();
    		
    		FollowBeaconStartThread = null;
    	}
	}
	
	@FXML
	public void ButtonShowGCSOnAction(ActionEvent actionEvent) {
		Task<Void> task = new Task<Void>() {
			@Override protected Void call() throws Exception {
				GCSLocationData gcsLocation = gcsLocationDataFactory.fetch();
				if (gcsLocation == null) {
					loggerDisplayerSvc.logError("Failed to get beacon point from the web");
					return null;
				}
				drone.getGCS().setPosition(gcsLocation.getCoordinate());
				drone.getGCS().UpdateAll();
				return null;
			}
		};
		
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
	}
	
	@FXML
	public void ButtonHoldPositionOnAction(ActionEvent actionEvent) {
		TryPoshold();
	}
	
	@FXML
	public void ButtonStartMissionOnAction(ActionEvent actionEvent) {
		Platform.runLater( () -> {
			if (btnStartMission.isSelected()) {
        		try {
        			opStartMissionQuad.setDroneMission(drone.getDroneMission());
        			opStartMissionQuad.setNext(null);
					if (!opStartMissionQuad.go())
						btnStartMission.setSelected(false);
				} catch (Exception e1) {
					dialogManagerSvc.showErrorMessageDialog("Failed to start droneMission, please resolve issue and try again", e1);
					btnStartMission.setSelected(false);
				}
        	}
        	else {
        		TryPoshold();
        	}
		});
	}
	
	@FXML
	public void ButtonStartPerimeterOnAction(ActionEvent actionEvent) {
		drone.getPerimeter().setEnforce(btnStartPerimeter.isSelected());
	}
	
	@FXML
	public void ButtonExitOnAction(ActionEvent actionEvent) {
		try {
			opGCSTerminationHandler.go();
		} 
		catch (InterruptedException ex) {
			loggerDisplayerSvc.logError("Failed to terminate GCS");
			ex.printStackTrace();
		}
	}
	
	@FXML
	public void ButtonSetModeOnAction(ActionEvent actionEvent) {
		if (flightModesCombo.getValue() == null)
			dialogManagerSvc.showAlertMessageDialog("Flight mode must be set");
		else
			drone.getState().changeFlightMode((ApmModes) flightModesCombo.getValue());
			drone.notifyDroneEvent(DroneEventsType.MODE);
		return;
	}

	private boolean TryLand() {
		boolean result = true;
		String[] options = {"MavlinkLand", "RTL", "Cancel"};
		int n = dialogManagerSvc.showOptionsDialog("Choose MavlinkLand Option", "", null, options, drone.getGps().isPositionValid() ? options[1] : options[0]);
		if (n == 0) {
			MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_LAND);
			loggerDisplayerSvc.logGeneral("Landing");
			textNotificationPublisherSvc.publish("Landing");
		}
		else if(n == 1) {
			MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_RTL);
			loggerDisplayerSvc.logGeneral("Comming back to lunch position");
			textNotificationPublisherSvc.publish("Return To Lunch");
		}
		else
			result = false;
		
		return result;
	}

	private void TryPoshold() {
		if (drone.getGps().isPositionValid()) {
			drone.getState().changeFlightMode(ApmModes.ROTOR_POSHOLD);
			loggerDisplayerSvc.logGeneral("Flight Mode set to 'Position Hold' - GPS");
		}
		else {
			drone.getState().changeFlightMode(ApmModes.ROTOR_ALT_HOLD);
			loggerDisplayerSvc.logGeneral("Flight Mode set to 'Altitude Hold' - Barometer");
		}
	}
	
	private void setButtonControl(boolean val) {
		btnArm.setDisable(!val);
		btnHoldPosition.setDisable(!val);
		btnController.setDisable(!val);
		btnLandRTL.setDisable(!val);
		btnTakeoff.setDisable(!val);
		btnStartMission.setDisable(!val);
		btnStartPerimeter.setDisable(!val);
		btnFollowBeaconShow.setDisable(!val);
		btnFollowBeaconStart.setDisable(!val);
		btnGCSShow.setDisable(!val);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		Platform.runLater( () -> {
			switch (event) {
				case ARMING:
					boolean motorArmed = drone.getState().isArmed();
					btnArm.setSelected(motorArmed);
					btnHoldPosition.setDisable(!motorArmed);
					return;
				case FOLLOW_STOP:
					btnFollowBeaconStart.setSelected(false);
					return;
				case MODE:
					btnStartMission.setSelected(drone.getState().getMode().equals(ApmModes.ROTOR_AUTO));
					Platform.runLater( () -> flightModesCombo.setValue(drone.getState().getMode()));
					return;
				case CONNECTED:
					loggerDisplayerSvc.logGeneral("Connected");
					connected = true;
					btnConnect.setText("Disconnect");
					SetImageButton(btnConnect, this.getClass().getResource("/com/dronegcs/console/guiImages/Connected.png"), "Disconnect");
					btnSyncDrone.setDisable(!connected);
					return;
				case DISCONNECTED:
					loggerDisplayerSvc.logGeneral("Disonnected");
					connected = false;
					btnConnect.setText("Connect");
					SetImageButton(btnConnect, this.getClass().getResource("/com/dronegcs/console/guiImages/Disconnected.png"), "Connect");
					btnSyncDrone.setDisable(!connected);
					setButtonControl(connected);
					return;
			}
		});
	}

	@Override
	public void onBeginReceivingParameters() {
		LOGGER.info(getClass() + " Start receiving parameters");
	}

	@Override
	public void onParameterReceived(Parameter parameter, int index, int count) {
		int prc = drone.getParameters().getPrecentageComplete();
		if (prc > 95)
			setButtonControl(true);
	}

	@Override
	public void onEndReceivingParameters(List<Parameter> parameter) {
		LOGGER.info(getClass() + " Finish receiving parameters");
		setButtonControl(true);
	}
	
	private void SetImageButton(Button button, URL url, String userDate) {
		button.setText(userDate);
		Image img = new Image(url.toString());
		ImageView iview = new ImageView(img);
		iview.setFitHeight(20);
		iview.setFitWidth(20);
		button.setGraphic(iview);
		button.setUserData(userDate);
	}
}
