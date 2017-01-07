package controllers.internalPanels;

import gui.is.events.GuiEvent;
import gui.services.DialogManagerSvc;
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
import validations.RuntimeValidator;

import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@ComponentScan("gui.core.internalFrames")
@Component("toolbarSatellite")
public class PanelToolBarSatellite extends FlowPane implements Initializable {
	
	@FXML private Button btnMap;
	@FXML private Button btnActualPWM;
	@FXML private Button btnBattery;
	@FXML private Button btnSignal;
	@FXML private Button btnHeightAndSpeed;
	@FXML private Button btnCamera;
		
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		if (!runtimeValidator.validate(this))
			throw new RuntimeException("Validation failed");
		else
			System.err.println("Validation Succeeded for instance of " + getClass());
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		btnMap.setUserData("internalFrameMap");
		btnActualPWM.setUserData("/views/internalFrames/InternalFrameActualPWMView.fxml");
		btnSignal.setUserData("/views/internalFrames/InternalFrameSignalsView.fxml");
		btnHeightAndSpeed.setUserData("/views/internalFrames/InternalFrameHeightAndSpeedView.fxml");
		btnBattery.setUserData("/views/internalFrames/InternalFrameBatteryView.fxml");
		btnCamera.setUserData("/views/internalFrames/InternalFrameVideoView.fxml");
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
	public void onApplicationEvent(GuiEvent command) {
		switch (command.getCommand()) {
		case EXIT:
			break;
		}
	}
}
