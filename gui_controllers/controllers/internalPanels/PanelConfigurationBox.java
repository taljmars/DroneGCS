package controllers.internalPanels;

import java.net.URL;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gui.events.QuadGuiEvent;
import gui.events.QuadGuiEvent.QUAD_GUI_COMMAND;
import gui.services.EventPublisherSvc;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import mavlink.drone.Drone;
import validations.RuntimeValidator;

@Component
public class PanelConfigurationBox extends Pane implements Initializable {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get GUI event publisher")
	protected EventPublisherSvc eventPublisherSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	@NotNull @FXML private CheckBox cbActiveGeofencePerimeterAlertOnly;
	@NotNull @FXML private ComboBox<Integer> cmbframeContainerCells;
	@NotNull @FXML private Button btnUpdateDevice;
	@NotNull @FXML private TextField txtDeviceId;
	
	private static int called = 0;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
	
	@Override 
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		if (!runtimeValidator.validate(this))
			throw new RuntimeException("Validation failed");
		
        cbActiveGeofencePerimeterAlertOnly.setOnAction( e -> drone.getPerimeter().setAlertOnly(cbActiveGeofencePerimeterAlertOnly.isSelected() ? true : false));
        cmbframeContainerCells.setOnAction( e -> eventPublisherSvc.publish(new QuadGuiEvent(QUAD_GUI_COMMAND.SPLIT_FRAMECONTAINER, cmbframeContainerCells.getValue())));
        btnUpdateDevice.setOnAction( e -> eventPublisherSvc.publish(new QuadGuiEvent(QUAD_GUI_COMMAND.CAMERA_DEVICEID,  Integer.parseInt(txtDeviceId.getText())  )));
	}
}
