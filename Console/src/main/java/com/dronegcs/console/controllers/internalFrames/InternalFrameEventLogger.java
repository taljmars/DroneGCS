package com.dronegcs.console.controllers.internalFrames;

import com.auditdb.persistence.scheme.ExternalObjectLog;
import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.dronegcs.console.controllers.GuiAppConfig;
import com.dronegcs.console.controllers.dashboard.FloatingNodeManager;
import com.dronegcs.console.controllers.internalFrames.internal.EventLogs.EventLogTableEntry;
import com.dronegcs.console.controllers.internalPanels.internal.TableItemEntry;
import com.dronegcs.console_plugin.ActiveUserProfile;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.services.internal.logevents.DroneGuiEvent;
import com.dronegcs.tracker.objects.EventSource;
import com.dronegcs.tracker.objects.TrackerEvent;
import com.dronegcs.tracker.services.TrackerEventConsumer;
import com.dronegcs.tracker.services.TrackerSvc;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.function.Predicate;

@Component
public class InternalFrameEventLogger extends Pane implements Initializable, TrackerEventConsumer {

	private final static Logger LOGGER = LoggerFactory.getLogger(InternalFrameEventLogger.class);


	@Autowired private ActiveUserProfile activeUserProfile;

	@Autowired private ObjectCrudSvcRemoteWrapper objectCrudSvcRemoteWrapper;

	@NotNull @FXML private TextField txtSearchField;
	@NotNull @FXML private HBox tagsList;

	@NotNull @FXML private TableView<EventLogTableEntry> table;

	@NotNull @FXML private TableColumn<TableItemEntry,String> eventSource;
	@NotNull @FXML private TableColumn<TableItemEntry,ImageView> icon;
	@NotNull @FXML private TableColumn<TableItemEntry,Date> date;
	@NotNull @FXML private TableColumn<TableItemEntry,String> userName;
	@NotNull @FXML private TableColumn<TableItemEntry,String> topic;
	@NotNull @FXML private TableColumn<TableItemEntry,String> summary;

	@NotNull @FXML private Label lblEntries;

	@Autowired private RuntimeValidator runtimeValidator;
	@Autowired private TrackerSvc trackerSvc;

	@Autowired @NotNull( message="Internal Error: Failed to get application context" )
	private ApplicationContext applicationContext;

	@Autowired @NotNull( message="Internal Error: Failed to get floatingNodeManager" )
	private FloatingNodeManager floatingNodeManager;

	@NotNull @FXML private Pane root;

	@Autowired @NotNull( message="Internal Error: Failed to get application context" )
	private GuiAppConfig guiAppConfig;

//	private Date lastPull = null;

	private ObservableList<EventLogTableEntry> data;
	private FilteredList filterData;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		table.setPrefWidth(root.getPrefWidth());
		table.setPrefHeight(root.getPrefHeight());

		table.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.isPopupTrigger() && floatingNodeManager.isEditing()) {
				root.getParent().setVisible(false);
			}
			if (event.getClickCount() < 2)
				return;

			EventLogTableEntry eventLogTableEntry = table.getSelectionModel().getSelectedItem();
			if (eventLogTableEntry == null)
				return;

			Parent eventLogCard = (Parent) guiAppConfig.loadInternalFrame("/com/dronegcs/console/views/LogEntryView.fxml", eventLogTableEntry);
			Stage stage = new Stage();
			stage.setTitle("Log/ " + eventLogTableEntry.getUid());
			stage.setResizable(false);
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(new Scene(eventLogCard));
			stage.show();
		});

//		Callback<TableColumn<EventLogTableEntry, Double>, TableCell<EventLogTableEntry, Double>> cellFactory = new Callback<TableColumn<EventLogTableEntry, Double>, TableCell<EventLogTableEntry, Double>>() {
//			public TableCell<EventLogTableEntry, Double> call(TableColumn<EventLogTableEntry, Double> p) {
//				EditingCell editingCell = new EditingCell<EventLogTableEntry, Double>(new DoubleStringConverter());
//				editingCell.setApplicationContext(applicationContext);
//				return editingCell;
//			}
//		};

		eventSource.setCellValueFactory(new PropertyValueFactory<>("eventSource"));
		icon.setCellValueFactory(new PropertyValueFactory<>("icon"));
		date.setCellValueFactory(new PropertyValueFactory<>("date"));
		userName.setCellValueFactory(new PropertyValueFactory<>("userName"));
		topic.setCellValueFactory(new PropertyValueFactory<>("topic"));
		summary.setCellValueFactory(new PropertyValueFactory<>("summary"));

		if (data != null && data.size() != 0)
			table.setItems(data);

		filterData = new FilteredList(data);
		loadTable();
	}

	private Predicate<EventLogTableEntry> searchPredicate = (EventLogTableEntry eventLogTableEntry) -> {
		if (tagsList.getChildren().isEmpty())
			return true;

		for (Node node: tagsList.getChildren()) {
			String key = (String) node.getUserData();
			if (eventLogTableEntry.getTopic().toLowerCase().contains(key.toLowerCase()))
				return true;

			if (eventLogTableEntry.getSummary().toLowerCase().contains(key.toLowerCase()))
				return true;

			if (eventLogTableEntry.getEventSource().toLowerCase().contains(key.toLowerCase()))
				return true;

			if (eventLogTableEntry.getUserName().toLowerCase().contains(key.toLowerCase()))
				return true;

//			return false;
		}

		return false;
	};

	private static int called;
	@PostConstruct
	private void init() {
		Assert.isTrue(++called == 1, "Not a Singleton");
		data = FXCollections.observableArrayList();
		filterData = new FilteredList(data);
		trackerSvc.addEventConsumer(this);
	}

	//	private Date lastDate = null;
//	private int idx = 0;
	public void loadTable() {
		if (table == null) {
//			LOGGER.debug("Table wasn't initialize yet");
//			Calendar cal = Calendar.getInstance();
//			cal.add(Calendar.MONTH, -1); // to get previous year add -1
//			lastDate = cal.getTime();
//			LOGGER.debug("Setting first timestamp to " + lastDate);
//			Date tmpDate = new Date();
//			eventLogBundle = trackerSvc.getAllEventLogsBetween(lastDate, tmpDate);
//		}
//		else {
//			eventLogBundle = trackerSvc.getLastEvents();
//			eventLogBundle = trackerSvc.getAllEventLogsBetween(lastDate, lastDate);
			return;
		}

		table.setItems(filterData);
		onFilterFinish();

//		if (eventLogBundle != null && eventLogBundle.size() > 0) {
//
//			if (data == null) {
//				data = FXCollections.observableArrayList();
//				idx = 0;
//			}
//			TrackerEvent lastLogObject = null;
//			for (TrackerEvent eventLogObject : eventLogBundle) {
//				EventLogTableEntry entry = buildEntry(eventLogObject);
//				entry.setId(idx++);
//				data.add(entry);
//				lastLogObject = eventLogObject;
//			}
//			if (lastLogObject != null) {
//				Calendar cal = Calendar.getInstance();
////				cal.setTime(lastLogObject.getEventTime());
//				cal.setTime(lastLogObject.getDate());
//				cal.add(Calendar.SECOND, 1);
//				lastDate = cal.getTime();
//			}
//			table.setItems(data);
//		}
	}

	private void onFilterFinish() {
		lblEntries.setText(filterData.size() + "/" + data.size() + " Entries");
	}

	private EventLogTableEntry buildEntry(TrackerEvent eventLogObject) {
//		if (eventLogObject instanceof AccessLog) {
//			AccessLog logObject = (AccessLog) eventLogObject;
//		TrackerEvent logObject = (TrackerEvent) eventLogObject;
//			LOGGER.debug("Building Entry:" + logObject.getUserName() + " " +
//					logObject.getEventCode() + " " +
//					logObject.getClz() + " " +
//					logObject.getEventTime() + " "
//			);
		EventLogTableEntry eventLogTableEntry = new EventLogTableEntry();
//			EventLogTableEntry.setIcon("");
		eventLogTableEntry.setUid(eventLogObject.getId());
		eventLogTableEntry.setEventSource(eventLogObject.getEventSource());
		eventLogTableEntry.setDate(eventLogObject.getDate());
		eventLogTableEntry.setType(eventLogObject.getType());
		eventLogTableEntry.setUserName(eventLogObject.getUserName());
		eventLogTableEntry.setTopic(eventLogObject.getTopic());
		eventLogTableEntry.setSummary(eventLogObject.getSummary());
		eventLogTableEntry.setData(eventLogObject.getPayload());
		return eventLogTableEntry;
//		}

//		if (eventLogObject instanceof RegistrationLog) {
//			RegistrationLog logObject = (RegistrationLog) eventLogObject;
//			LOGGER.debug("Building Entry:" + logObject.getUserName() + " " +
//					logObject.getEventCode() + " " +
//					logObject.getClz() + " " +
//					logObject.getEventTime() + " "
//			);
//			EventLogTableEntry EventLogTableEntry = new EventLogTableEntry();
////			EventLogTableEntry.setIcon("");
//			EventLogTableEntry.setCode(logObject.getEventCode());
//			EventLogTableEntry.setDate(logObject.getEventTime());
//			EventLogTableEntry.setUserName(logObject.getUserName());
//			EventLogTableEntry.setTopic(logObject.getClz().getSimpleName());
//			EventLogTableEntry.setSummary(logObject.getDescription());
//			return EventLogTableEntry;
//		}
//
//		if (eventLogObject instanceof ObjectCreationLog) {
//			ObjectCreationLog logObject = (ObjectCreationLog) eventLogObject;
//			EventLogTableEntry EventLogTableEntry = new EventLogTableEntry();
//			EventLogTableEntry.setCode(logObject.getEventCode());
//			EventLogTableEntry.setDate(logObject.getEventTime());
//			EventLogTableEntry.setUserName(logObject.getUserName());
//			EventLogTableEntry.setTopic(logObject.getClz().getSimpleName());
//			EventLogTableEntry.setSummary(logObject.getReferredObjType().getSimpleName() + " - " + logObject.getReferredObjId());
//			return EventLogTableEntry;
//		}
//
//		if (eventLogObject instanceof ObjectDeletionLog) {
//			ObjectDeletionLog logObject = (ObjectDeletionLog) eventLogObject;
//			EventLogTableEntry EventLogTableEntry = new EventLogTableEntry();
//			EventLogTableEntry.setCode(logObject.getEventCode());
//			EventLogTableEntry.setDate(logObject.getEventTime());
//			EventLogTableEntry.setUserName(logObject.getUserName());
//			EventLogTableEntry.setTopic(logObject.getClz().getSimpleName());
//			EventLogTableEntry.setSummary(logObject.getReferredObjType().getSimpleName() + " - " + logObject.getReferredObjId());
//			return EventLogTableEntry;
//		}
//
//		if (eventLogObject instanceof ObjectUpdateLog) {
//			ObjectUpdateLog logObject = (ObjectUpdateLog) eventLogObject;
//			EventLogTableEntry EventLogTableEntry = new EventLogTableEntry();
//			EventLogTableEntry.setCode(logObject.getEventCode());
//			EventLogTableEntry.setDate(logObject.getEventTime());
//			EventLogTableEntry.setUserName(logObject.getUserName());
//			EventLogTableEntry.setTopic(logObject.getClz().getSimpleName());
//			String summary = logObject.getReferredObjType().getSimpleName() + " - " + logObject.getReferredObjId() + " | ";
//			for (int i = 0 ; i < logObject.getChangedFields().size() ; i++) {
//				summary += String.format("{%s,%s->%s} ",
//						logObject.getChangedFields().get(i),
//						logObject.getChangedFromValues().get(i),
//						logObject.getChangedToValues().get(i)
//				);
//			}
//			EventLogTableEntry.setSummary(summary);
//			return EventLogTableEntry;
//		}

//		return null;
	}

	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(DroneGuiEvent command) {
		switch (command.getCommand()) {
			case EXIT:
				break;
		}
	}

	static final int SEC = 1000;
	@Scheduled(fixedRate = 2 * SEC)
	public void tik() {
		if (activeUserProfile.getMode() == ActiveUserProfile.Mode.OFFLINE)
			return;

//		LOGGER.debug("Refresh Log");
		Platform.runLater(() -> loadTable());
	}

	@Override
	public void offer(TrackerEvent trackerEvent) {
		data.add(0,buildEntry(trackerEvent));
		if (activeUserProfile.getMode().equals(ActiveUserProfile.Mode.ONLINE) && !trackerEvent.getEventSource().equals(EventSource.DB_SERVER.name())) {
			try {
				ExternalObjectLog externalObjectLog = objectCrudSvcRemoteWrapper.create(ExternalObjectLog.class.getCanonicalName());
				externalObjectLog.setDate(trackerEvent.getDate());
				externalObjectLog.setEventSource(trackerEvent.getEventSource());
				externalObjectLog.setPayload((String) trackerEvent.getPayload());
				externalObjectLog.setSummary(trackerEvent.getSummary());
				externalObjectLog.setTopic(trackerEvent.getTopic());
				externalObjectLog.setType(trackerEvent.getType().name());
				externalObjectLog.setExternalUser(trackerEvent.getUserName());
				objectCrudSvcRemoteWrapper.update(externalObjectLog);
			}
			catch (ObjectInstanceRemoteException e) {
				e.printStackTrace();
			} catch (DatabaseValidationRemoteException e) {
				e.printStackTrace();
			}
		}
//		lblEntries.setText(data.size() + " Entries");
	}

	@FXML
	private void onTextSearchKeyPressed(KeyEvent keyEvent) {
		KeyCode key = keyEvent.getCode();
		if(key == KeyCode.ENTER){
			if (txtSearchField.getText().isEmpty())
				return;

			Label label = new Label(txtSearchField.getText());
			Button button = new Button("X");
			Pane pane = new StackPane();

			HBox hBox = new HBox();
			hBox.setAlignment(Pos.CENTER);
			hBox.setStyle("-fx-background-color: transparent; " +
					"-fx-border-radius: 50; " +
					"-fx-border-color: grey;"
			);
			hBox.setPadding(new Insets(2,5,2,5));

//			button.setStyle("-fx-background-color: transparent;" +
			button.setStyle("-fx-background-color: "+
					"rgba(0,0,0,0.08),"+
					"linear-gradient(#9a9a9a, #909090),"+
					"linear-gradient(white 0%, #f3f3f3 50%, #ececec 51%, #f2f2f2 100%);"+
					"-fx-background-insets: 0 0 -1 0,0,1;"+
					"-fx-background-radius: 50;"+
					"-fx-font-size: 8px;" +
					"");
//			-fx-background-color:
//			linear-gradient(#ffd65b, #e68400),
//					linear-gradient(#ffef84, #f2ba44),
//					linear-gradient(#ffea6a, #efaa22),
//					linear-gradient(#ffe657 0%, #f8c202 50%, #eea10b 100%),
//					linear-gradient(from 0% 0% to 15% 50%, rgba(255,255,255,0.9), rgba(255,255,255,0));
//			-fx-background-radius: 30;
			button.setOnAction(e -> {
				tagsList.getChildren().remove(pane);
				refreshTable();
			});
			hBox.getChildren().addAll(label, button);
			hBox.setSpacing(7);
			pane.getChildren().addAll(hBox);
			pane.setUserData(txtSearchField.getText());
			pane.setPadding(new Insets(0, 5, 0,0));
			tagsList.getChildren().add(pane);
			txtSearchField.setText("");

			refreshTable();
		}
	}

	private void refreshTable() {
		Predicate<EventLogTableEntry> p = (EventLogTableEntry eventLogTableEntry) -> searchPredicate.test(eventLogTableEntry);
		filterData.setPredicate(p);
		loadTable();
	}
}
