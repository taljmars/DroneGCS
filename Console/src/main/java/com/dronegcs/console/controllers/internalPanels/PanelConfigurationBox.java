package com.dronegcs.console.controllers.internalPanels;

import java.net.URL;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import com.dronegcs.console.controllers.internalFrames.InternalFrameMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.gui.core.mapViewer.MapViewerSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.dronegcs.console_plugin.services.EventPublisherSvc;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;

@Component
public class PanelConfigurationBox extends Pane implements Initializable {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get GUI event publisher")
	protected EventPublisherSvc eventPublisherSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;

	@Autowired @NotNull(message = "Internal Error: Missing application Context")
	private ApplicationContext applicationContext;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	@NotNull @FXML private CheckBox cbActiveGeofencePerimeterAlertOnly;
	@NotNull @FXML private Button btnUpdateDevice;
	@NotNull @FXML private TextField txtDeviceId;
	@NotNull @FXML private ComboBox<String> cmbMapIconFontSize;
	
	private static int called = 0;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
	}
	
	@Override 
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		cmbMapIconFontSize.setValue(MapViewerSettings.getMarkersFontSize());
		
        cbActiveGeofencePerimeterAlertOnly.setOnAction( e -> drone.getPerimeter().setAlertOnly(cbActiveGeofencePerimeterAlertOnly.isSelected() ? true : false));
        btnUpdateDevice.setOnAction( e -> eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.CAMERA_DEVICEID,  Integer.parseInt(txtDeviceId.getText())  )));
		cmbMapIconFontSize.setOnAction( e -> {
				MapViewerSettings.setMarkersFontSize(cmbMapIconFontSize.getValue());
				OperationalViewTree operationalViewTree = applicationContext.getBean(OperationalViewTree.class);
				operationalViewTree.regenerateTree();
		});
	}
}
