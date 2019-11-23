package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.variables.Perimeter;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class GeoFence extends Pane implements Initializable {

	@Autowired @NotNull(message = "Internal Error: Failed to get GUI event publisher")
	protected ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;

	@Autowired @NotNull(message = "Internal Error: Missing application Context")
	private ApplicationContext applicationContext;
	
	@Autowired
	private RuntimeValidator runtimeValidator;

	@NotNull @FXML public RadioButton geoOff;
	@NotNull @FXML public RadioButton geoAlert;
	@NotNull @FXML public RadioButton geoEnforce;

	private static int called = 0;
	@PostConstruct
	private void init() {
		Assert.isTrue(++called == 1, "Not a Singleton");
	}
	
	@Override 
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		ToggleGroup toggleGroup = new ToggleGroup();
		toggleGroup.getToggles().addAll(geoAlert, geoEnforce, geoOff);

		switch (drone.getPerimeter().getPerimeterMode()) {
			case OFF:
				geoOff.setSelected(true);
				break;
			case ALERT:
				geoAlert.setSelected(true);
				break;
			case ENFORCE:
				geoEnforce.setSelected(true);
				break;
		}
	}

	public void setGeoFenceOff(ActionEvent actionEvent) {
		drone.getPerimeter().setPerimeterMode(Perimeter.PerimeterMode.OFF);
	}

	public void setGeoFenceAlert(ActionEvent actionEvent) {
		drone.getPerimeter().setPerimeterMode(Perimeter.PerimeterMode.ALERT);
	}

	public void setGeoFenceEnforce(ActionEvent actionEvent) {
		drone.getPerimeter().setPerimeterMode(Perimeter.PerimeterMode.ENFORCE);
	}
}
