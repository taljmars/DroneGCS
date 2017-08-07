package com.dronegcs.console.controllers.internalPanels;

import com.dronegcs.console.controllers.internalPanels.internal.PerimeterTableProfile;
import com.dronegcs.console.controllers.internalPanels.internal.TableItemEntry;
import com.dronegcs.console.controllers.internalPanels.internal.MissionTableProfile;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class PanelTableBox extends Pane implements Initializable {

	@NotNull @FXML private TableView<TableItemEntry> table;
    
	@NotNull @FXML private TableColumn<TableItemEntry,Integer> order;
	@NotNull @FXML private TableColumn<TableItemEntry,String> type;
	@NotNull @FXML private TableColumn<TableItemEntry,Double> lat;
	@NotNull @FXML private TableColumn<TableItemEntry,Double> lon;
	@NotNull @FXML private TableColumn<TableItemEntry,Double> altitude;
	@NotNull @FXML private TableColumn<TableItemEntry,Double> delayOrTime;
	@NotNull @FXML private TableColumn<TableItemEntry,Double> radius;
	@NotNull @FXML private TableColumn<TableItemEntry,Integer> turns;
	@NotNull @FXML private TableColumn<TableItemEntry,String> up;
	@NotNull @FXML private TableColumn<TableItemEntry,String> down;
	@NotNull @FXML private TableColumn<TableItemEntry,String> remove;

	@Autowired
	private RuntimeValidator runtimeValidator;

	// Profiles //

	@Autowired @NotNull(message = "Internal Error: Failed to get mission box profile")
	private MissionTableProfile missionTableProfile;

	@Autowired @NotNull(message = "Internal Error: Failed to get perimeter box profile")
	private PerimeterTableProfile perimeterTableProfile;
	
	private static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		missionTableProfile.setBigTableView(this);
		perimeterTableProfile.setBigTableView(this);
	}


	public TableView<TableItemEntry> getTable() {
		return table;
	}

	public TableColumn<TableItemEntry, Integer> getOrder() {
		return order;
	}

	public TableColumn<TableItemEntry, String> getType() {
		return type;
	}

	public TableColumn<TableItemEntry, Double> getLat() {
		return lat;
	}

	public TableColumn<TableItemEntry, Double> getLon() {
		return lon;
	}

	public TableColumn<TableItemEntry, Double> getAltitude() {
		return altitude;
	}

	public TableColumn<TableItemEntry, Double> getDelayOrTime() {
		return delayOrTime;
	}

	public TableColumn<TableItemEntry, Double> getRadius() {
		return radius;
	}

	public TableColumn<TableItemEntry, Integer> getTurns() {
		return turns;
	}

	public TableColumn<TableItemEntry, String> getUp() {
		return up;
	}

	public TableColumn<TableItemEntry, String> getDown() {
		return down;
	}

	public TableColumn<TableItemEntry, String> getRemove() {
		return remove;
	}
}
