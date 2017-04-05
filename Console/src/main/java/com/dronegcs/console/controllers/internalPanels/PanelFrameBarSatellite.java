package com.dronegcs.console.controllers.internalPanels;

import com.dronegcs.console_plugin.services.DialogManagerSvc;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.generic_tools.validations.RuntimeValidator;
import java.net.URL;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.generic_tools.validations.ValidatorResponse;

@Component
public class PanelFrameBarSatellite extends FlowPane implements Initializable {	
	
	private static String INTERNAL_FRAME_PATH = "/com/dronegcs/console/views/internalFrames/";
	
	@NotNull @FXML private Button btnMap;
	private static String MAP_VIEW = "InternalFrameMapAndTreeView.fxml";
	
	@NotNull @FXML private Button btnActualPWM;
	private static String ACTUAL_PWM_VIEW = "InternalFrameActualPWMView.fxml";

	@NotNull @FXML private Button btnBattery;
	private static String BATTERY_VIEW = "InternalFrameBatteryView.fxml";
	
	@NotNull @FXML private Button btnSignal;
	private static String SIGNALS_VIEW = "InternalFrameSignalsView.fxml";
	
	@NotNull @FXML private Button btnHeightAndSpeed;
	private static String HEIGHT_SPEED_VIEW = "InternalFrameHeightAndSpeedView.fxml";
	
	@NotNull @FXML private Button btnCamera;
	private static String CAMERA_VIEW = "InternalFrameVideoView.fxml";
		
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		updateFrameMapPath();

		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());
	}
	
	private void updateFrameMapPath() {
		btnMap.setUserData(INTERNAL_FRAME_PATH + MAP_VIEW);
		btnActualPWM.setUserData(INTERNAL_FRAME_PATH + ACTUAL_PWM_VIEW);
		btnSignal.setUserData(INTERNAL_FRAME_PATH + SIGNALS_VIEW);
		btnHeightAndSpeed.setUserData(INTERNAL_FRAME_PATH  + HEIGHT_SPEED_VIEW);
		btnBattery.setUserData(INTERNAL_FRAME_PATH + BATTERY_VIEW);
		btnCamera.setUserData(INTERNAL_FRAME_PATH + CAMERA_VIEW);
	}
	
	@FXML
	private void ToolbarOnDragDone(DragEvent event) {
		Button button = (Button) event.getSource();
		ColorAdjust blackout = new ColorAdjust();
        blackout.setContrast(0);
        button.setEffect(blackout);
	}
	
	@FXML
	private void ToolbarOnDragEvent(MouseEvent event) {
		Button button = (Button) event.getSource();
		
    	/* drag was detected, start a drag-and-drop gesture*/
    	/* allow any transfer mode */
    	Dragboard db = button.startDragAndDrop(TransferMode.ANY);
    	
    	ColorAdjust blackout = new ColorAdjust();
        blackout.setSaturation(0.5);
        button.setEffect(blackout);
    	        
    	/* Put a string on a dragboard */
    	ClipboardContent content = new ClipboardContent();
    	content.putString((String) button.getUserData());
    	db.setContent(content);
    	event.consume();
	}
	
	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(QuadGuiEvent command) {
		switch (command.getCommand()) {
			case EXIT:
				break;			
		}
	}
}
