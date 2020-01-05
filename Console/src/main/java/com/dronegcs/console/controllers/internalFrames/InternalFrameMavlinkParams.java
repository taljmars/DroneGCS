package com.dronegcs.console.controllers.internalFrames;

import com.dronegcs.console.controllers.EditingCell;
import com.dronegcs.console.controllers.dashboard.FloatingNodeManager;
import com.dronegcs.console.controllers.internalFrames.internal.MavlinkParameters.ParamsTableEntry;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.services.internal.logevents.DroneGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnDroneListener;
import com.dronegcs.mavlink.is.drone.parameters.Parameter;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_PARAM_I;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_PARAM_UNIT;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_TYPE;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

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
	@NotNull @FXML private TableColumn<ParamsTableEntry,String> title;
	@NotNull @FXML private TableColumn<ParamsTableEntry,String> name;
	@NotNull @FXML private TableColumn<ParamsTableEntry,Number> value;
	@NotNull @FXML private TableColumn<ParamsTableEntry,Number> defaultValue;
	@NotNull @FXML private TableColumn<ParamsTableEntry,String> unit;
	@NotNull @FXML private TableColumn<ParamsTableEntry,String> modify;
	@NotNull @FXML private TableColumn<ParamsTableEntry,String> update;
	@NotNull @FXML private TableColumn<ParamsTableEntry,String> description;

	@NotNull @FXML private TextField txtSearchField;
	@NotNull @FXML private Label lblEntries;

	@NotNull @FXML private Button btnRefresh;
	@NotNull @FXML private ComboBox cbOfflineProfile;

	public static final String ALL_GROUPS = "All";
	@NotNull @FXML private ComboBox cbParamGroup;

	private ObservableList<ParamsTableEntry> data;

	@Autowired @NotNull( message="Internal Error: Failed to get drone" )
	private Drone drone;

	@Autowired @NotNull( message="Internal Error: Failed to get application context" )
	private ApplicationContext applicationContext;

	@Autowired
	private RuntimeValidator runtimeValidator;

	@NotNull @FXML private Pane root;

	private FilteredList<Parameter> filteredData = null;

	private class ParamGroup {
		String name;
		int amount;

		public ParamGroup(String name, int size) {
			this.name = name;
			this.amount = size;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name + " (" + amount + ")";
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		btnRefresh.setDisable(!drone.getMavClient().isConnected());
		cbOfflineProfile.setDisable(drone.getMavClient().isConnected());
		cbOfflineProfile.getItems().addAll(MAV_TYPE.values());
		cbOfflineProfile.setValue(drone.getMavClient().isConnected() ? drone.getType().getDroneType() : MAV_TYPE.MAV_TYPE_QUADROTOR);

		List<Parameter> parametersList = null;
		if (drone.getMavClient().isConnected())
			parametersList = drone.getParameters().getParametersList();
		else
			parametersList = drone.getParameters().getParametersMetadata();
		filteredData = new FilteredList<>(FXCollections.observableList(parametersList), p -> true);

		regenerateGroups();

		table.setPrefWidth(root.getPrefWidth());
//		table.setPrefHeight(root.getPrefHeight());

		Callback<TableColumn<ParamsTableEntry, Number>, TableCell<ParamsTableEntry, Number>> cellFactory = new Callback<TableColumn<ParamsTableEntry, Number>, TableCell<ParamsTableEntry, Number>>() {
			public TableCell<ParamsTableEntry, Number> call(TableColumn<ParamsTableEntry, Number> p) {
				EditingCell editingCell = new EditingCell<ParamsTableEntry, Number>(new NumberStringConverter());
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
		title.setCellValueFactory(new PropertyValueFactory<>("title"));

//		name.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
//		name.setCellFactory(param -> {
//			final TableCell<ParamsTableEntry, String> cell = new TableCell<ParamsTableEntry, String>() {
//				private final HighlightingLabelLayout layout = new HighlightingLabelLayout();
//				{
//					layout.highlightTextProperty().bind(txtSearchField.textProperty());
//				}
//
//				@Override
//				protected void updateItem(String data, boolean empty) {
//					super.updateItem(data, empty);
//					if (empty) {
//						setGraphic(null);
//					} else {
//						layout.setText(data);
//						setGraphic(layout);
//					}
//				}
//			};
//			return cell;
//		});

//		value.setCellValueFactory(new PropertyValueFactory<>("value"));
//		value.setCellFactory(cellFactory);
//		value.setEditable(true);
//		value.setOnEditCommit( t -> {
//			ParamsTableEntry entry = t.getTableView().getItems().get(t.getTablePosition().getRow());
//			entry.setValue(String.valueOf(t.getNewValue()));
//		});

		value.setCellFactory( param -> {
			final TableCell<ParamsTableEntry, Number> cell = new TableCell<ParamsTableEntry, Number>() {
				@Override
				public void updateItem( Number item, boolean empty ) {
					super.updateItem( item, empty );
					setGraphic(null);
					setText(null);

					if ( empty || getIndex() < 0 )
						return;

					ParamsTableEntry entry = getTableView().getItems().get( getIndex() );
					MAV_PARAM_I existParam = drone.getVehicleProfile().getParametersMetadataMap().get(entry.getName());
					if (existParam == null) {
						LOGGER.error("Parameter '" + entry.getName() + "' doesn't exist in the parameters list");
						return;
					}
					if (existParam.isReadOnly())
						return;

					HBox hbox = new HBox();
					if (existParam.getUnit().equals(MAV_PARAM_UNIT.FLAGS)) {
						ComboBox cb = new ComboBox();
						cb.setPrefWidth(value.getPrefWidth());
						cb.getItems().addAll(existParam.getOptions().values());
						Number tmpNumber = Double.parseDouble(entry.getValue());
						Number number;
						if (tmpNumber.intValue() == tmpNumber.doubleValue())
							number = tmpNumber.intValue();
						else
							number = tmpNumber.doubleValue();
						cb.setValue(existParam.getOptions().get(number));
						cb.setOnAction(value -> {
							if (!drone.getMavClient().isConnected()) {
								// In offile we force the original value
								cb.setValue(existParam.getOptions().get(number));
								return;
							}
							for (Map.Entry<Number, String> entrySet : existParam.getOptions().entrySet()) {
								if (entrySet.getValue().equals(cb.getValue())) {
									getTableView().getItems().get(getIndex()).setValue(String.valueOf(entrySet.getKey()));
									return;
								}
							}
						});
						hbox.getChildren().addAll(cb);
					}
					else {
						TextField textField = new TextField(entry.getValue());
						textField.setOnAction(value -> getTableView().getItems().get(getIndex()).setValue(textField.getText()));
						textField.setPrefWidth(value.getPrefWidth());
						textField.setEditable(drone.getMavClient().isConnected());
						hbox.getChildren().addAll(textField);
					}
					hbox.setAlignment(Pos.CENTER);
					setGraphic( hbox );
				}
			};
			return cell;
		});

		defaultValue.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));

		unit.setCellValueFactory(new PropertyValueFactory<>("unit"));

		modify.setVisible(drone.getMavClient().isConnected());
		modify.setCellFactory( param -> {
			final TableCell<ParamsTableEntry, String> cell = new TableCell<ParamsTableEntry, String>() {
				final Button left = new Button("<");
				final Button right = new Button(">");
				@Override
				public void updateItem( String item, boolean empty ) {
					super.updateItem( item, empty );
					setGraphic( null );
					setText( null );
					if ( !empty && getIndex() > 0 ) {
						ParamsTableEntry entry = getTableView().getItems().get( getIndex() );
						MAV_PARAM_I param = drone.getVehicleProfile().getParametersMetadataMap().get(entry.getName());
						if (param == null) {
							LOGGER.error("Parameter '" + entry.getName() + "' doesn't exist in the parameters list");
							return;
						}
						if (param.isReadOnly() || param.getUnit().equals(MAV_PARAM_UNIT.FLAGS)) {
							return;
						}

						left.setOnAction( ( ActionEvent event ) -> {
							Number increment = param.getIncrement();
							Number value;
							if (increment instanceof Double)
								value = Double.parseDouble(entry.getValue()) - increment.doubleValue();
							else
								value = Integer.parseInt(entry.getValue()) - increment.intValue();
							entry.setValue(value + "");
						});

						right.setOnAction( ( ActionEvent event ) -> {
							Number increment = param.getIncrement();
							Number value;
							if (increment instanceof Double)
								value = Double.parseDouble(entry.getValue()) + increment.doubleValue();
							else
								value = Integer.parseInt(entry.getValue()) + increment.intValue();
							entry.setValue(value + "");
						});
						HBox hbox = new HBox(left, right);
						hbox.setAlignment(Pos.CENTER);
						setGraphic( hbox );
					}
				}
			};
			return cell;
		});

		update.setVisible(drone.getMavClient().isConnected());
		update.setCellFactory( param -> {
			final TableCell<ParamsTableEntry, String> cell = new TableCell<ParamsTableEntry, String>() {
				final Button btn = new Button("Update");
				@Override
				public void updateItem( String item, boolean empty ) {
					super.updateItem( item, empty );
					setGraphic( null );
					setText( null );
					if ( !empty && getIndex() > 0 ) {
						ParamsTableEntry entry = getTableView().getItems().get( getIndex() );
						MAV_PARAM_I param = drone.getVehicleProfile().getParametersMetadataMap().get(entry.getName());
						if (param == null) {
							LOGGER.error("Parameter '" + entry.getName() + "' doesn't exist in the parameters list");
							return;
						}
						if (param.isReadOnly()) {
							return;
						}

						btn.setOnAction( ( ActionEvent event ) -> {
//							ParamsTableEntry entry = getTableView().getItems().get( getIndex() );
							Double value = Double.parseDouble(entry.getValue());
							if (value != null) {
								if (!drone.isConnectionAlive()) {
									LOGGER.debug("Connection is not a live, failed to update");
									loggerDisplayerSvc.logGeneral("Connection is not a live, reconnect before update");
									return;
								}
								Parameter parameter = new Parameter(entry.getName(),"dummy", value, value, "dummy", entry.getType(), true,"","Updated by GCS");
								drone.getParameters().sendParameter(parameter);
								LOGGER.debug("Update Parameter: {}", parameter);
								loggerDisplayerSvc.logGeneral("Update Parameter: " + parameter);
							}
						});
						HBox hbox = new HBox(btn);
						hbox.setAlignment(Pos.CENTER);
						setGraphic( hbox );
					}
				}
			};
			return cell;
		});

		description.setCellValueFactory(new PropertyValueFactory<>("description"));
//		description.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
//		description.setCellFactory(param -> {
//			final TableCell<ParamsTableEntry, String> cell = new TableCell<ParamsTableEntry, String>() {
//				private final HighlightingLabelLayout layout = new HighlightingLabelLayout();
//				{
//					layout.highlightTextProperty().bind(txtSearchField.textProperty());
//				}
//
//				@Override
//				protected void updateItem(String data, boolean empty) {
//					super.updateItem(data, empty);
//					if (empty) {
//						setGraphic(null);
//					} else {
//						layout.setText(data);
//						setGraphic(layout);
//					}
//				}
//			};
//			return cell;
//		});

		txtSearchField.textProperty().addListener((observable, oldValue, newValue) -> {

			predicatesSet.add(searchPredicate);

			Predicate<Parameter> predicate = (Parameter parameter) -> {
				for (Predicate predicateObj : predicatesSet) {
					if (!predicateObj.test(parameter))
						return false;
				}
				return true;
			};

			filteredData.setPredicate(predicate);
			loadTable(filteredData);

			regenerateGroups();
		});

		filteredData.setPredicate(p -> true);
		loadTable(filteredData);

		regenerateGroups();
	}

	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		Assert.isTrue(++called == 1, "Not a Singleton");

		drone.addDroneListener(this);
		drone.getParameters().addParameterListener(this);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (cbOfflineProfile == null) {
			// GUI frame is not initialized yet
			return;
		}

		Platform.runLater(() -> {
			switch (event) {
				case TYPE:
					if (drone.getParameters().getParametersList() == null || drone.getParameters().getParametersList().size() == 0) {
						filteredData = new FilteredList<>(FXCollections.observableList(new ArrayList<>()));
						cbOfflineProfile.setValue(drone.getType().getDroneType());
						loadTable(filteredData);
						regenerateGroups();
						setButtonForOnline();
					}
					break;
			}
		});
	}

	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(DroneGuiEvent command) {
		switch (command.getCommand()) {
			case EXIT:

				break;
		}
	}

	public void loadTable(FilteredList<Parameter> filteredData) {
		if (table == null) {
			LOGGER.debug("Table wasn't initialize yet");
			return;
		}

//		if (filteredData.size() > 0) {
		if (filteredData != null) {
			data = FXCollections.observableArrayList();

			for (int i = 0; i < filteredData.size(); i++) {
				Parameter parameter = filteredData.get(i);
				String defaultValue = String.valueOf(parameter.getDefaultValue());
				if (parameter.getUnit().equals(MAV_PARAM_UNIT.FLAGS.toString())) {
					defaultValue = parameter.getOptions().get(parameter.getDefaultValue());
				}

				ParamsTableEntry entry = new ParamsTableEntry(i, parameter.getName(), parameter.getTitle(), parameter.getValue() + "", defaultValue, parameter.getUnit(), parameter.getType(), parameter.getDescription());
				data.add(entry);
			}
			table.setItems(data);
			table.setEditable(true);
			lblEntries.setText(filteredData.size() + " Entries");
		}
	}

	@Override
	public void onBeginReceivingParameters() {
		setButtonForOnline();
	}

	public void setButtonForOnline() {
		if (cbOfflineProfile == null)
			return;
		cbOfflineProfile.setDisable(true);
		btnRefresh.setDisable(false);
		value.setEditable(true);
		modify.setVisible(true);
		update.setVisible(true);
	}

	@Override
	public void onParameterReceived(Parameter parameter, int i, int i1) {
		if (i != UNINDEX_PARAM)
			return;

		LOGGER.debug("Received updated parameter {}", parameter);
		LOGGER.debug("new value -> {}", drone.getParameters().getParameter(parameter.getName()));
//		if (filteredData != null) {
//			filteredData.setPredicate(p -> true);
//			Platform.runLater(() -> loadTable(filteredData));
//		}
	}

	@Override
	public void onEndReceivingParameters(List<Parameter> list) {
		LOGGER.debug("Received updated parameter, amount {}", list.size());
		if (filteredData != null) {
//			filteredData.setPredicate(p -> true);
			Platform.runLater(() -> {
				if (cbOfflineProfile == null)
					return;
				cbOfflineProfile.setValue(drone.getType().getDroneType());
				predicatesSet.remove(groupPredicate);
				filteredData = new FilteredList<>(FXCollections.observableList(drone.getParameters().getParametersList()), generatePredicateSequence());
				regenerateGroups();
				loadTable(filteredData);
			});
		}
	}

	@FXML
	public void handleRefresh(ActionEvent actionEvent) {
		drone.getParameters().refreshParameters();
	}

	@FXML
	public void handleOfflineProfile(ActionEvent actionEvent) {
		drone.setType((MAV_TYPE)cbOfflineProfile.getValue());
		predicatesSet.remove(groupPredicate);
		filteredData = new FilteredList<>(FXCollections.observableList(drone.getParameters().getParametersMetadata()), generatePredicateSequence());
		regenerateGroups();
		loadTable(filteredData);
	}

	public void regenerateGroups() {
		Map<String, ParamGroup> groups = new HashMap<String,ParamGroup>();
		for (int i = 0; i < filteredData.size(); i++) {
			Parameter parameter = filteredData.get(i);
			ParamGroup paramGroup = groups.getOrDefault(parameter.getGroup(),new ParamGroup(parameter.getGroup(),0));
			paramGroup.amount++;
			groups.put(parameter.getGroup(), paramGroup);
		}
		cbParamGroup.getItems().removeAll(cbParamGroup.getItems());
		ParamGroup allGroup = new ParamGroup(ALL_GROUPS, filteredData.size());
		ParamGroup[] groupsOrder = groups.values().toArray(new ParamGroup[0]);
		Arrays.sort(groupsOrder, Comparator.comparing(ParamGroup::getName));
		cbParamGroup.getItems().add(allGroup);
		cbParamGroup.getItems().addAll(groupsOrder);
		cbParamGroup.setValue(allGroup);
	}

	private Set<Predicate<Parameter>> predicatesSet = new HashSet<>();

	private Predicate<Parameter> groupPredicate = (Parameter parameter) -> {
		ParamGroup paramGroup = (ParamGroup) cbParamGroup.getValue();
		if (cbParamGroup.getValue() == null || paramGroup.name.equals(ALL_GROUPS))
			return true;

		if (parameter.getGroup().equals(paramGroup.name))
			return true;

		return false;
	};

	private Predicate<Parameter> searchPredicate = (Parameter parameter) -> {
		if (parameter.getDescription().toLowerCase().contains(txtSearchField.getText().toLowerCase()))
			return true;

		if (parameter.getTitle().toLowerCase().contains(txtSearchField.getText().toLowerCase()))
			return true;

		if (parameter.getName().toLowerCase().contains(txtSearchField.getText().toLowerCase()))
			return true;

		return false;
	};

	private Predicate<Parameter> generatePredicateSequence(){
		Predicate<Parameter> predicate = (Parameter parameter) -> {
			for (Predicate predicateObj : predicatesSet) {
				if (!predicateObj.test(parameter))
					return false;
			}
			return true;
		};
		return predicate;
	}

	@FXML
	public void handleGroupChange(ActionEvent actionEvent) {
		predicatesSet.add(groupPredicate);
		filteredData.setPredicate(generatePredicateSequence());
		loadTable(filteredData);
	}
}
