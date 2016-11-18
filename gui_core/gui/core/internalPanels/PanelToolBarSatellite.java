package gui.core.internalPanels;

import gui.core.internalFrames.InternalFrameActualPWM;
import gui.core.internalFrames.InternalFrameBattery;
import gui.core.internalFrames.InternalFrameHeightAndSpeed;
import gui.core.internalFrames.InternalFrameMap;
import gui.core.internalFrames.InternalFrameSignals;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;

import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.protocol.msg_metadata.ApmModes;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@ComponentScan("gui.core.internalFrames")
@Component("toolbarSatellite")
public class PanelToolBarSatellite extends FlowPane implements OnDroneListener, EventHandler<ActionEvent> {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 763689884103713162L;
	
	private Button btnSetMode;
	private Button btnMap;
	private Button btnActualPWM;
	private Button btnBattery;
	private Button btnSignal;
	private Button btnHeightAndSpeed;
	private ComboBox<ApmModes> flightModesCombo;
	private TextField lblCriticalMsg;

	@Resource(name = "frameContainer")
	private Pane frameContainer;
	
	@Resource(name = "internalFrameMap")
	private InternalFrameMap internalFrameMap;
	
	@Resource(name = "internalFrameActualPWM")
	private InternalFrameActualPWM internalFrameActualPWM;
	
	@Resource(name = "internalFrameSignals")
	private InternalFrameSignals internalFrameSignals;
	
	@Resource(name = "internalFrameHeightAndSpeed")
	private InternalFrameHeightAndSpeed internalFrameHeightAndSpeed;
	
	@Resource(name = "internalFrameBattery")
	private InternalFrameBattery internalFrameBattery;
	
	@Resource(name = "drone")
	public Drone drone;

	public PanelToolBarSatellite() {		
		btnMap = new Button("Map");
        getChildren().add(btnMap);
        btnMap.setOnAction(this);
        
        btnActualPWM = new Button("Actual PWM");
        getChildren().add(btnActualPWM);
        btnActualPWM.setOnAction(this);
        
        btnSignal = new Button("Signals");
        getChildren().add(btnSignal);
        btnSignal.setOnAction(this);
        
        btnHeightAndSpeed = new Button("Height And Speed");
        getChildren().add(btnHeightAndSpeed);
        btnHeightAndSpeed.setOnAction(this);
        
        btnBattery = new Button("Battery");
        getChildren().add(btnBattery);
        btnBattery.setOnAction(this);
        
        //btnMap.setSelected(true);
        
        FlowPane pnlMode = new FlowPane();
		
        Vector<ApmModes> flightModes = new Vector<ApmModes>();
        flightModes.add(ApmModes.ROTOR_STABILIZE);
        flightModes.add(ApmModes.ROTOR_ACRO);
        flightModes.add(ApmModes.ROTOR_ALT_HOLD);
        flightModes.add(ApmModes.ROTOR_AUTO);
        flightModes.add(ApmModes.ROTOR_GUIDED);
        flightModes.add(ApmModes.ROTOR_LOITER);
        flightModes.add(ApmModes.ROTOR_RTL);
        flightModes.add(ApmModes.ROTOR_CIRCLE);
        flightModes.add(ApmModes.ROTOR_LAND);
        flightModes.add(ApmModes.ROTOR_TOY);
        flightModes.add(ApmModes.ROTOR_SPORT);
        flightModes.add(ApmModes.ROTOR_AUTOTUNE);
        flightModes.add(ApmModes.ROTOR_POSHOLD);
        
        flightModesCombo = new ComboBox<ApmModes>();
        flightModesCombo.getItems().addAll(new Vector<ApmModes>(flightModes));
        pnlMode.getChildren().add(flightModesCombo);
        btnSetMode = new Button("Set Mode");
        btnSetMode.setOnAction(this);
        pnlMode.getChildren().add(btnSetMode);
        getChildren().add(pnlMode);
        
        lblCriticalMsg = new TextField("");
        lblCriticalMsg.setPrefWidth(Screen.getPrimary().getBounds().getWidth() * 0.3);
        lblCriticalMsg.setEditable(false);
        getChildren().add(lblCriticalMsg);
	}
	
	@PostConstruct
	public void init() {
		btnMap.fire();
        drone.addDroneListener(this);
	}

	public void ClearNotification() {
		lblCriticalMsg.setVisible(false);
	}

	public void SetNotification(String notification) {
		lblCriticalMsg.setVisible(true);
		if (lblCriticalMsg.getStyle() == "-fx-control-inner-background: orange;") {
			lblCriticalMsg.setStyle("-fx-control-inner-background: orange;");
		}
		else {
			lblCriticalMsg.setStyle("-fx-control-inner-background: orange;");
		}
		lblCriticalMsg.setText(notification);
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			Platform.runLater( () -> flightModesCombo.setValue(drone.getState().getMode()));
			return;
		}
	}

	@Override
	public void handle(ActionEvent e) {
		if (e.getSource() == btnMap) {
			Platform.runLater(() -> {
				frameContainer.getChildren().clear();
		        frameContainer.getChildren().add(internalFrameMap);
		        internalFrameMap.setPrefHeight(frameContainer.getHeight());
		        internalFrameMap.setPrefWidth(frameContainer.getWidth());
		        internalFrameMap.refreshGui();
			});
			return;
		}

		if (e.getSource() == btnActualPWM) {
			Platform.runLater(() -> {
				frameContainer.getChildren().clear();
		        frameContainer.getChildren().add(internalFrameActualPWM);
		        internalFrameActualPWM.setPrefHeight(frameContainer.getHeight());
		        internalFrameActualPWM.setPrefWidth(frameContainer.getWidth());
			});
			return;
		}
		
		if (e.getSource() == btnSignal) {
			Platform.runLater(() -> {
				frameContainer.getChildren().clear();
		        frameContainer.getChildren().add(internalFrameSignals);
		        internalFrameSignals.setPrefHeight(frameContainer.getHeight());
		        internalFrameSignals.setPrefWidth(frameContainer.getWidth());
			});
			return;
		}
		
		if (e.getSource() == btnHeightAndSpeed) {
			Platform.runLater(() -> {
				frameContainer.getChildren().clear();
		        frameContainer.getChildren().add(internalFrameHeightAndSpeed);
		        internalFrameHeightAndSpeed.setPrefHeight(frameContainer.getHeight());
		        internalFrameHeightAndSpeed.setPrefWidth(frameContainer.getWidth());
			});
			return;
		}
		
		if (e.getSource() == btnBattery) {
			Platform.runLater(() -> {
				frameContainer.getChildren().clear();
		        frameContainer.getChildren().add(internalFrameBattery);
		        internalFrameBattery.setPrefHeight(frameContainer.getHeight());
		        internalFrameBattery.setPrefWidth(frameContainer.getWidth());
			});
			return;
		}
		
		if (e.getSource() == btnSetMode) {
			drone.getState().changeFlightMode((ApmModes) flightModesCombo.getValue());
			return;
		}
	}

}
