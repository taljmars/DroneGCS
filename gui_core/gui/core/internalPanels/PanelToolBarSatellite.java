package gui.core.internalPanels;

import gui.is.events.GuiEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.stage.Screen;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.protocol.msg_metadata.ApmModes;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@ComponentScan("gui.core.internalFrames")
@Component("toolbarSatellite")
public class PanelToolBarSatellite extends FlowPane implements OnDroneListener, EventHandler<ActionEvent> {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 763689884103713162L;
	
	private Button btnStartCamera;
	private Button btnSetMode;
	private Button btnMap;
	private Button btnActualPWM;
	private Button btnBattery;
	private Button btnSignal;
	private Button btnHeightAndSpeed;
	private ComboBox<ApmModes> flightModesCombo;
	private TextField lblCriticalMsg;
	
	private Process cameraExternalProcess;

	@Resource(name = "drone")
	public Drone drone;

	public PanelToolBarSatellite() {	
		btnMap = CreateDragableButton(this.getClass().getResource("/guiImages/map.png"), "Map");
        getChildren().add(btnMap);
        
        btnActualPWM = CreateDragableButton(this.getClass().getResource("/guiImages/motor.png"), "Actual PWM");
        getChildren().add(btnActualPWM);
        
        btnSignal = CreateDragableButton(this.getClass().getResource("/guiImages/signal.png"), "Signals");
        getChildren().add(btnSignal);
        
        btnHeightAndSpeed = CreateDragableButton(this.getClass().getResource("/guiImages/hieght.png"), "Height And Speed");
        getChildren().add(btnHeightAndSpeed);
        
        btnBattery = CreateDragableButton(this.getClass().getResource("/guiImages/battery.png"), "Battery");
        getChildren().add(btnBattery);
        
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
        flightModesCombo.setPrefHeight(30 + 8);
        pnlMode.getChildren().add(flightModesCombo);
        btnSetMode = CreateImageButton(this.getClass().getResource("/guiImages/UpdateQuad.png"), "Set Mode");
        btnSetMode.setOnAction(this);
        pnlMode.getChildren().add(btnSetMode);
        
        btnStartCamera = CreateImageButton(this.getClass().getResource("/guiImages/Camera.png"), "Start Camera");
        btnStartCamera.setOnAction(this);
        pnlMode.getChildren().add(btnStartCamera);
        
        getChildren().add(pnlMode);
        
        lblCriticalMsg = new TextField("");
        lblCriticalMsg.setPrefWidth(Screen.getPrimary().getBounds().getWidth() * 0.3);
        lblCriticalMsg.setEditable(false);
        getChildren().add(lblCriticalMsg);
	}
	
	private Button CreateImageButton(URL url, String userDate) {
		Button button = new Button();
		Image img = new Image(url.toString());
		ImageView iview = new ImageView(img);
		iview.setFitHeight(30);
		iview.setFitWidth(30);
		button.setGraphic(iview);
		return button;
	}

	private Button CreateDragableButton(URL url, String userDate) {
		Button button = CreateImageButton(url, userDate);
		button.setUserData(userDate);
		
		ImageView iview = (ImageView) button.getGraphic();
        
		button.setOnDragDetected( (event) -> {
        	/* drag was detected, start a drag-and-drop gesture*/
        	/* allow any transfer mode */
        	Dragboard db = button.startDragAndDrop(TransferMode.ANY);
        	
        	ColorAdjust blackout = new ColorAdjust();
            blackout.setSaturation(0.5);
            iview.setEffect(blackout);
        	        
        	/* Put a string on a dragboard */
        	ClipboardContent content = new ClipboardContent();
        	content.putString(button.getUserData().toString());
        	db.setContent(content);
        	event.consume();
        });
		
		button.setOnDragDone( (event) -> {
			ColorAdjust blackout = new ColorAdjust();
            blackout.setContrast(0);
            iview.setEffect(blackout);
		});
		
		return button;
	}
	
	@PostConstruct
	public void init() {
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
		if (e.getSource() == btnSetMode) {
			drone.getState().changeFlightMode((ApmModes) flightModesCombo.getValue());
			return;
		}
		
		if (e.getSource() == btnStartCamera) {
			try {
				cameraExternalProcess = Runtime.getRuntime().exec("\"C:/Program Files (x86)/Samsung/SideSync4/SideSync.exe\"");
				cameraExternalProcess.waitFor();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(GuiEvent command) {
		switch (command.getCommand()) {
		case EXIT:
			if (cameraExternalProcess != null) 
				cameraExternalProcess.destroy();;
			break;
		}
	}

}
