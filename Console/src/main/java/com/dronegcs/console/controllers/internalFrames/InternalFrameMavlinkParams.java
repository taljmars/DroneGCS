package com.dronegcs.console.controllers.internalFrames;

import com.dronegcs.console.controllers.EditingCell;
import com.dronegcs.console.controllers.dashboard.FloatingNodeManager;
import com.dronegcs.console.controllers.internalFrames.internal.MavlinkParameters.ParamsTableEntry;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnDroneListener;
import com.dronegcs.mavlink.is.drone.parameters.Parameter;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.dronegcs.mavlink.is.drone.profiles.Parameters.UNINDEX_PARAM;

@Component
public class InternalFrameMavlinkParams extends Pane implements OnDroneListener, Initializable, DroneInterfaces.OnParameterManagerListener {

	private final static Logger LOGGER = LoggerFactory.getLogger(InternalFrameMavlinkParams.class);

	@Autowired
	@NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get floatingNodeManager")
	private FloatingNodeManager floatingNodeManager;


	@NotNull @FXML private TableView<ParamsTableEntry> table;

	@NotNull @FXML private TableColumn<ParamsTableEntry,Integer> id;
	@NotNull @FXML private TableColumn<ParamsTableEntry,String> name;
	@NotNull @FXML private TableColumn<ParamsTableEntry,Double> value;
	@NotNull @FXML private TableColumn<ParamsTableEntry,Integer> type;
	@NotNull @FXML private TableColumn<ParamsTableEntry,String> update;
	@NotNull @FXML private TableColumn<ParamsTableEntry,String> description;

	private ObservableList<ParamsTableEntry> data;

	@Autowired @NotNull( message="Internal Error: Failed to get drone" )
	private Drone drone;

	@Autowired @NotNull( message="Internal Error: Failed to get application context" )
	private ApplicationContext applicationContext;

	@Autowired
	private RuntimeValidator runtimeValidator;
	
	@NotNull @FXML private Pane root;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		table.setPrefWidth(root.getPrefWidth());
//		table.setPrefHeight(root.getPrefHeight());

		Callback<TableColumn<ParamsTableEntry, Double>, TableCell<ParamsTableEntry, Double>> cellFactory = new Callback<TableColumn<ParamsTableEntry, Double>, TableCell<ParamsTableEntry, Double>>() {
			public TableCell<ParamsTableEntry, Double> call(TableColumn<ParamsTableEntry, Double> p) {
				EditingCell editingCell = new EditingCell<ParamsTableEntry, Double>(new DoubleStringConverter());
				editingCell.setApplicationContext(applicationContext);
				return editingCell;
			}
		};

		table.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.isPopupTrigger() && floatingNodeManager.isEditing()) {
				root.getParent().setVisible(false);
			}
		});

		id.setCellValueFactory(new PropertyValueFactory<>("id"));
		name.setCellValueFactory(new PropertyValueFactory<>("name"));

		value.setCellValueFactory(new PropertyValueFactory<>("value"));
		value.setCellFactory(cellFactory);
		value.setEditable(true);
		value.setOnEditCommit( t -> {
			ParamsTableEntry entry = t.getTableView().getItems().get(t.getTablePosition().getRow());
			entry.setValue(String.valueOf(t.getNewValue()));
		});

		type.setCellValueFactory(new PropertyValueFactory<>("type"));

		update.setCellFactory( param -> {
			final TableCell<ParamsTableEntry, String> cell = new TableCell<ParamsTableEntry, String>() {
				final Button btn = new Button("Update");
				@Override
				public void updateItem( String item, boolean empty ) {
					super.updateItem( item, empty );
					setGraphic( null );
					setText( null );
					if ( !empty && getIndex() > 0 ) {
						btn.setOnAction( ( ActionEvent event ) -> {
							ParamsTableEntry entry = getTableView().getItems().get( getIndex() );
							Double value = Double.parseDouble(entry.getValue());
							if (value != null) {
								if (!drone.isConnectionAlive()) {
									LOGGER.debug("Connection is not a live, failed to update");
									loggerDisplayerSvc.logGeneral("Connection is not a live, reconnect before update");
									return;
								}
								Parameter parameter = new Parameter(entry.getName(), value, entry.getType(), "dummy", "Updated by GCS");
								drone.getParameters().sendParameter(parameter);
								LOGGER.debug("Update Parameter: {}", parameter);
								loggerDisplayerSvc.logGeneral("Update Parameter: " + parameter);
							}
						});
						setGraphic( btn );
					}
				}
			};
			return cell;
		});

		description.setCellValueFactory(new PropertyValueFactory<>("description"));

		loadTable();
	}
	
	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");

		drone.addDroneListener(this);
		drone.getParameters().addParameterListener(this);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(QuadGuiEvent command) {
		switch (command.getCommand()) {
		case EXIT:

			break;
		}
	}

	public void loadTable() {
		if (table == null) {
			LOGGER.debug("Table wasn't initialize yet");
			return;
		}
		List<Parameter> parametersList = drone.getParameters().getParametersList();
		if (parametersList.size() > 0) {
			data = FXCollections.observableArrayList();
			for (int i = 0; i < parametersList.size(); i++) {
				Parameter parameter = parametersList.get(i);
				ParamsTableEntry entry = new ParamsTableEntry(i, parameter.name, parameter.getValue(), parameter.type, parameter.getDescription());
				data.add(entry);
			}
			table.setItems(data);
			table.setEditable(true);
		}
	}

	@Override
	public void onBeginReceivingParameters() {}

	@Override
	public void onParameterReceived(Parameter parameter, int i, int i1) {
		if (i != UNINDEX_PARAM)
			return;

		LOGGER.debug("Received updated parameter {}", parameter);
		LOGGER.debug("new value -> {}", drone.getParameters().getParameter(parameter.name));
		Platform.runLater(() -> loadTable());
	}

	@Override
	public void onEndReceivingParameters(List<Parameter> list) {
		LOGGER.debug("Received updated parameter, amount {}", list.size());
		Platform.runLater(() -> loadTable());
	}
}
