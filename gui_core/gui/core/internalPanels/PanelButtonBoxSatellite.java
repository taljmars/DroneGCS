package gui.core.internalPanels;

import java.util.List;

import gui.core.operations.OpArmQuad;
import gui.core.operations.OpChangeFlightControllerQuad;
import gui.core.operations.OpStartMissionQuad;
import gui.core.operations.OpTakeoffQuad;
import gui.is.events.GuiEvent;
import gui.is.events.GuiEvent.COMMAND;
import gui.is.services.DialogManagerSvc;
import gui.is.services.EventPublisherSvc;
import gui.is.services.LoggerDisplayerSvc;
import gui.is.services.TextNotificationPublisherSvc;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.TilePane;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import tools.comm.SerialConnection;
import tools.logger.Logger;
import tools.pair.Pair;
import mavlink.core.connection.helper.GCSLocationData;
import mavlink.core.flightControlers.FlightControler;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.drone.DroneInterfaces.OnParameterManagerListener;
import mavlink.is.drone.parameters.Parameter;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.protocol.msgbuilder.MavLinkArm;
import mavlink.is.protocol.msgbuilder.MavLinkModes;

@ComponentScan("mavlink.core.drone")
@ComponentScan("gui.core.operations.internal")
@ComponentScan("mavlink.core.drone")
@ComponentScan("gui.is.services")
@ComponentScan("gui.core.operations")
@Component("buttonBoxSatellite")
public class PanelButtonBoxSatellite extends TilePane implements OnDroneListener, OnParameterManagerListener, EventHandler<ActionEvent> {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -2419085692095415348L;
	
	private Button btnConnect;
	private Button btnExit;
	private Button btnSyncDrone;
	private Button btnFly;
	private ToggleButton btnArm;
	private Button btnLandRTL;
	private Button btnTakeoff;
	private Button btnGCSShow;
	private Button btnHoldPosition;
	private ToggleButton btnStartMission;
	private ToggleButton btnStartPerimeter;
	
	private boolean connected = false;
	private boolean takeOffThreadRunning = false;
    private ToggleButton btnFollowBeaconStart;
    private Thread FollowBeaconStartThread = null;
    private Button btnFollowBeaconShow;

	@Resource(name = "drone")
	@NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Resource(name = "textNotificationPublisherSvc")
	@NotNull(message = "Internal Error: Failed to get text publisher")
	private TextNotificationPublisherSvc textNotificationPublisherSvc;
	
	@Resource(name = "eventPublisherSvc")
	@NotNull(message = "Internal Error: Failed to get GUI event publisher")
	protected EventPublisherSvc eventPublisherSvc;
	
	@Resource(name = "opArmQuad")
	private OpArmQuad opArmQuad;
	
	@Resource(name = "opTakeoffQuad")
	private OpTakeoffQuad opTakeoffQuad;
	
	@Resource(name = "opStartMissionQuad")
	private OpStartMissionQuad opStartMissionQuad;
	
	@Resource(name = "opChangeFlightControllerQuad")
	private OpChangeFlightControllerQuad opChangeFlightControllerQuad;
	
	@Resource(name = "twoWaySerialComm")
	@NotNull(message = "Internal Error: Failed to get serial communication")
	private SerialConnection serialConnection;
	
	@Resource(name = "logger")
	@NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;
	
	@Resource(name = "loggerDisplayerSvc")
	@NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "dialogManagerSvc")
	@NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	public PanelButtonBoxSatellite () {		
		setVgap(2);
		setHgap(2);
		setPrefColumns(2);
		double preferedButtonWidth = 100;
		double preferedButtonHeight = 25;
		
		btnExit = new Button("Exit");
		btnExit.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnExit.setOnAction( e -> {
        	if (DialogManagerSvc.YES_OPTION == dialogManagerSvc.showConfirmDialog("Are you sure you wand to exit?", "")) {
        		System.out.println("Bye Bye");
        		logger.LogGeneralMessege("");
        		logger.LogGeneralMessege("Summary:");
        		logger.LogGeneralMessege("--------");
        		logger.LogGeneralMessege("Traveled distance: " + drone.getGps().getDistanceTraveled() + "m");
        		logger.LogGeneralMessege("Max Height: " + drone.getAltitude().getMaxAltitude() + "m");
        		logger.LogGeneralMessege("Max Speed: " + drone.getSpeed().getMaxAirSpeed().valueInMetersPerSecond() + "m/s (" + ((int) (drone.getSpeed().getMaxAirSpeed().valueInMetersPerSecond()*3.6)) + "km/h)");
        		logger.LogGeneralMessege("Flight time: " + drone.getState().getFlightTime() + "");
        		eventPublisherSvc.publish(new GuiEvent(COMMAND.EXIT, this));
				logger.close();
				System.exit(0);
        	}
		});
        getChildren().add(btnExit);
        
        btnConnect = new Button("Connect");
        btnConnect.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnConnect.setOnAction( e -> {
        	Object[] ports = serialConnection.listPorts();
        	if (ports.length == 0) {
        		dialogManagerSvc.showAlertMessageDialog("Failed to find ports");
        		return;
        	}
        	Pair<Object, Object> res = dialogManagerSvc.showMuliComboBoxMessageDialog("Select port: ", ports, ports[0] ,"Select baud rate: ",serialConnection.baudList(), serialConnection.getDefaultBaud());
        	if (res != null) {
        		String port_name = (String) res.first;
        		Integer baud = (Integer) res.second;
        		serialConnection.setPortName(port_name);
        		serialConnection.setBaud(baud);
        		
        		if (connected) {
            		loggerDisplayerSvc.logGeneral("Close Connection");
            		drone.getMavClient().disconnect();
            	}
            	else {
    		    	loggerDisplayerSvc.logGeneral("Open Connection");
    		    	drone.getMavClient().connect();
            	}
        	}
		});
        getChildren().add(btnConnect);
        
        btnSyncDrone = new Button("Sync Drone");
        btnSyncDrone.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnSyncDrone.setDisable(true);
        btnSyncDrone.setOnAction( e -> {
        	System.out.println("Sync");
        	loggerDisplayerSvc.logGeneral("Syncing Drone parameters");
    		drone.getParameters().refreshParameters();
        });
        getChildren().add(btnSyncDrone);
        
        btnFly = new Button("Controler: " + FlightControler.REMOTE.name());
        btnFly.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnFly.setOnAction( e -> {
        	try {
        		String btnFlyText = btnFly.getText();
        		opChangeFlightControllerQuad.setNext(null);
        		String[] options = {FlightControler.KEYBOARD.name(), FlightControler.REMOTE.name()};
            	int n = dialogManagerSvc.showYesNoOptionDialog("Choose Controler", "", null,options, options[1]);
            	if (n == 0) {
            		opChangeFlightControllerQuad.setFlightMode(FlightControler.KEYBOARD);
            		btnFlyText = "Controler: " + FlightControler.KEYBOARD.name();
            	}
            	if (n == 1) {
            		opChangeFlightControllerQuad.setFlightMode(FlightControler.REMOTE);
            		btnFlyText = "Controler: " + FlightControler.REMOTE.name();
            	}
				if (opChangeFlightControllerQuad.go()) {
					loggerDisplayerSvc.logGeneral("Start Fly '" + options[n] + "'");
					btnFly.setText(btnFlyText);
				}
				
			} catch (Exception e1) {
				logger.LogErrorMessege(e1.getMessage());
			}

        });
        getChildren().add(btnFly);        
        
        btnArm = new ToggleButton("Arm Motors");
        btnArm.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnArm.setOnAction( e -> {
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
        });
        getChildren().add(btnArm);
        
        btnLandRTL = new Button("Land/RTL");
        btnLandRTL.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnLandRTL.setOnAction( e -> TryLand());
        getChildren().add(btnLandRTL);
        
        btnTakeoff = new Button("Takeoff");
        btnTakeoff.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnTakeoff.setOnAction(this);
        getChildren().add(btnTakeoff);
        
        btnFollowBeaconShow = new Button("Show Beacon");
        btnFollowBeaconShow.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnFollowBeaconShow.setOnAction( e -> {        		
        	Task<Void> task = new Task<Void>() {
    			@Override protected Void call() throws Exception {	       				
					drone.getBeacon().syncBeacon();
					return null;
				}
			};
			Thread th = new Thread(task);
    		th.setDaemon(true);
    		th.start();
        });
        getChildren().add(btnFollowBeaconShow);
        
        btnFollowBeaconStart = new ToggleButton("Lock on Beacon");
        btnFollowBeaconStart.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnFollowBeaconStart.setOnAction(this);
        getChildren().add(btnFollowBeaconStart);
        
        btnGCSShow = new Button("Get GCS Position");
        btnGCSShow.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnGCSShow.setOnAction( e -> {
        	Task<Void> task = new Task<Void>() {
    			@Override protected Void call() throws Exception {
					GCSLocationData gcslocation = GCSLocationData.fetch();
					if (gcslocation == null) {
						loggerDisplayerSvc.logError("Failed to get beacon point from the web");
						return null;
					}
					drone.getGCS().setPosition(gcslocation.getCoordinate());
					drone.getGCS().UpdateAll();
					return null;
				}
			};
   		
			Thread th = new Thread(task);
			th.setDaemon(true);
			th.start();
        });
        getChildren().add(btnGCSShow);
        
        btnHoldPosition = new Button("Hold Position");
        btnHoldPosition.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnHoldPosition.setOnAction( e -> TryPoshold());
        btnHoldPosition.setDisable(true);
        getChildren().add(btnHoldPosition);
        
        btnStartMission = new ToggleButton("Start Mission");
        btnStartMission.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnStartMission.setOnAction(this);
        btnStartMission.setDisable(true);
        getChildren().add(btnStartMission);
        
        btnStartPerimeter = new ToggleButton("Start Perimeter");
        btnStartPerimeter.setPrefSize(preferedButtonWidth, preferedButtonHeight);
        btnStartPerimeter.setOnAction( e -> drone.getPerimeter().setEnforce(btnStartPerimeter.isSelected()));
        btnStartPerimeter.setDisable(true);
        getChildren().add(btnStartPerimeter);
        
	}
	
	@PostConstruct
	public void init() {
		setButtonControl(false);
		drone.addDroneListener(this);
		drone.getParameters().addParameterListener(this);
	}

	private boolean TryLand() {
		boolean result = true;
		String[] options = {"Land", "RTL", "Cancel"};
		int n = dialogManagerSvc.showYesNoOptionDialog("Choose Land Option", "", null, options, drone.getGps().isPositionValid() ? options[1] : options[0]);
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
		btnFly.setDisable(!val);
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
				return;
			case CONNECTED:
				loggerDisplayerSvc.logGeneral("Connected");
				connected = true;
				btnConnect.setText("Disconnect");
				btnSyncDrone.setDisable(!connected);
				return;
			case DISCONNECTED:
				loggerDisplayerSvc.logGeneral("Disonnected");
				connected = false;
				btnConnect.setText("Connect");
				btnSyncDrone.setDisable(!connected);
				setButtonControl(connected);
				return;
		}
		});
	}

	@Override
	public void onBeginReceivingParameters() {
		System.out.println(getClass() + " Start receiving parameters");
	}

	@Override
	public void onParameterReceived(Parameter parameter, int index, int count) {
		int prc = drone.getParameters().getPrecentageComplete();
		if (prc > 95)
			setButtonControl(true);
	}

	@Override
	public void onEndReceivingParameters(List<Parameter> parameter) {
		System.out.println(getClass() + " Finish receiving parameters");
		setButtonControl(true);
	}

	@Override
	public void handle(ActionEvent event) {
		
		// Handling takeoff request - it includes arming request as well
		if (event.getSource() == btnTakeoff) {
			
	    	if (takeOffThreadRunning) {
	    		dialogManagerSvc.showAlertMessageDialog("Takeoff procedure was already started");
	    		return;
	    	}

	    	takeOffThreadRunning = true;
			    		
			logger.LogGeneralMessege("Takeoff thread Stated!");
		    if (!drone.getState().isArmed()) {
		    	if (dialogManagerSvc.showAlertMessageDialog("Quad will automatically be armed")) {
		    		System.out.println("User notified quad was armed");
		    	}
		    }
						
    		String val = dialogManagerSvc.showInputDialog("Choose altitude", "", null, null, "5");
    		if (val == null) {
    			System.out.println(getClass().getName() + " Takeoff canceled");
    			takeOffThreadRunning = false;
   				logger.LogGeneralMessege("Takeoff thread Done!");
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
		   				logger.LogGeneralMessege("Takeoff thread Done!");
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
		
		if (event.getSource() == btnStartMission) {
			Platform.runLater( () -> {
				if (btnStartMission.isSelected()) {
	        		try {
	        			opStartMissionQuad.setMission(drone.getMission());
	        			opStartMissionQuad.setNext(null);
						if (!opStartMissionQuad.go())
							btnStartMission.setSelected(false);
					} catch (Exception e1) {
						dialogManagerSvc.showErrorMessageDialog("Failed to start mission, please resolve issue and try again", e1);
						btnStartMission.setSelected(false);
					}
	        	}
	        	else {
	        		TryPoshold();
	        	}
			});
		}
		
		
		if (event.getSource() == btnFollowBeaconStart) {
			if (btnFollowBeaconStart.isSelected()) {
        		if (!drone.getState().isArmed()) {
        			if (dialogManagerSvc.showAlertMessageDialog("Quad will automatically be armed")) {
        				System.out.println("User was notified about arming quad");
        			}
        		}
	        		
        		if (drone.getState().isFlying()) {
        			drone.getFollow().toggleFollowMeState();
        			return;
        		}
	        		
        		String val = dialogManagerSvc.showInputDialog("Choose altitude", "", null, null, "5");
        		if (val == null) {
        			loggerDisplayerSvc.logError("Takeoff canceled");
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
	}

}
