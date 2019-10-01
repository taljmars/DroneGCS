package com.dronegcs.console.controllers.internalFrames;

import com.auditdb.persistence.base_scheme.EventLogObject;
import com.auditdb.persistence.scheme.*;
import com.dronegcs.console.controllers.ActiveUserProfile;
import com.dronegcs.console.controllers.EditingCell;
import com.dronegcs.console.controllers.dashboard.FloatingNodeManager;
import com.dronegcs.console.controllers.internalFrames.internal.EventLogs.EventLogEntry;
import com.dronegcs.console.controllers.internalPanels.internal.TableItemEntry;
import com.dronegcs.console_plugin.event_logger.EventLogBundle;
import com.dronegcs.console_plugin.event_logger.EventLogManager;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
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
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

@Component
public class InternalFrameEventLogger extends Pane implements Initializable {

	private final static Logger LOGGER = LoggerFactory.getLogger(InternalFrameEventLogger.class);

	@Autowired private ActiveUserProfile activeUserProfile;

	@NotNull @FXML private TableView<EventLogEntry> table;

	@NotNull @FXML private TableColumn<TableItemEntry,ImageView> icon;
	@NotNull @FXML private TableColumn<TableItemEntry,String> id;
	@NotNull @FXML private TableColumn<TableItemEntry,Date> date;
	@NotNull @FXML private TableColumn<TableItemEntry,String> userName;
	@NotNull @FXML private TableColumn<TableItemEntry,String> code;
	@NotNull @FXML private TableColumn<TableItemEntry,String> topic;
	@NotNull @FXML private TableColumn<TableItemEntry,String> summary;
	
	@Autowired private RuntimeValidator runtimeValidator;
	@Autowired private EventLogManager eventLogManager;

	@Autowired @NotNull( message="Internal Error: Failed to get application context" )
	private ApplicationContext applicationContext;

	@Autowired @NotNull( message="Internal Error: Failed to get floatingNodeManager" )
	private FloatingNodeManager floatingNodeManager;

	@NotNull @FXML private Pane root;

	private Date lastPull = null;

	private ObservableList<EventLogEntry> data;

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
		});

		Callback<TableColumn<EventLogEntry, Double>, TableCell<EventLogEntry, Double>> cellFactory = new Callback<TableColumn<EventLogEntry, Double>, TableCell<EventLogEntry, Double>>() {
			public TableCell<EventLogEntry, Double> call(TableColumn<EventLogEntry, Double> p) {
				EditingCell editingCell = new EditingCell<EventLogEntry, Double>(new DoubleStringConverter());
				editingCell.setApplicationContext(applicationContext);
				return editingCell;
			}
		};

		icon.setCellValueFactory(new PropertyValueFactory<>("icon"));
		id.setCellValueFactory(new PropertyValueFactory<>("id"));
		date.setCellValueFactory(new PropertyValueFactory<>("date"));
		userName.setCellValueFactory(new PropertyValueFactory<>("userName"));
		code.setCellValueFactory(new PropertyValueFactory<>("code"));
		topic.setCellValueFactory(new PropertyValueFactory<>("topic"));
		summary.setCellValueFactory(new PropertyValueFactory<>("summary"));

		loadTable();
		if (data != null && data.size() != 0)
            table.setItems(data);
	}

	private static int called;
	@PostConstruct
	private void init() {
        Assert.isTrue(++called == 1, "Not a Singleton");
	}

	private Date lastDate = null;
	private int idx = 0;
	public void loadTable() {
		EventLogBundle eventLogBundle;
		if (table == null) {
			LOGGER.debug("Table wasn't initialize yet");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -1); // to get previous year add -1
			lastDate = cal.getTime();
			LOGGER.debug("Setting first timestamp to " + lastDate);
			Date tmpDate = new Date();
			eventLogBundle = eventLogManager.getAllEventLogsBetween(lastDate, tmpDate);
			eventLogBundle = eventLogBundle.sortByEventDate();
		}
		else {
			eventLogBundle = eventLogManager.getLastEvents();
		}
		if (eventLogBundle.getLogs().size() > 0) {

			if (data == null) {
				data = FXCollections.observableArrayList();
				idx = 0;
			}
			EventLogObject lastLogObject = null;
			for (EventLogObject eventLogObject : eventLogBundle.getLogs()) {
				EventLogEntry entry = buildEntry(eventLogObject);
				entry.setId(idx++);
				data.add(entry);
				lastLogObject = eventLogObject;
			}
			if (lastLogObject != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(lastLogObject.getEventTime());
				cal.add(Calendar.SECOND, 1);
				lastDate = cal.getTime();
			}
			table.setItems(data);
		}
	}

	private EventLogEntry buildEntry(EventLogObject eventLogObject) {
		if (eventLogObject instanceof AccessLog) {
			AccessLog logObject = (AccessLog) eventLogObject;
			LOGGER.debug("Building Entry:" + logObject.getUserName() + " " +
					logObject.getEventCode() + " " +
					logObject.getClz() + " " +
					logObject.getEventTime() + " "
			);
			EventLogEntry eventLogEntry = new EventLogEntry();
//			eventLogEntry.setIcon("");
			eventLogEntry.setCode(logObject.getEventCode());
            eventLogEntry.setDate(logObject.getEventTime());
            eventLogEntry.setUserName(logObject.getUserName());
			eventLogEntry.setTopic(logObject.getClz().getSimpleName());
			eventLogEntry.setSummary(logObject.isLogin() ? "Logged In" : "Logged Out");
			return eventLogEntry;
		}

		if (eventLogObject instanceof RegistrationLog) {
			RegistrationLog logObject = (RegistrationLog) eventLogObject;
			LOGGER.debug("Building Entry:" + logObject.getUserName() + " " +
					logObject.getEventCode() + " " +
					logObject.getClz() + " " +
					logObject.getEventTime() + " "
			);
			EventLogEntry eventLogEntry = new EventLogEntry();
//			eventLogEntry.setIcon("");
			eventLogEntry.setCode(logObject.getEventCode());
			eventLogEntry.setDate(logObject.getEventTime());
			eventLogEntry.setUserName(logObject.getUserName());
			eventLogEntry.setTopic(logObject.getClz().getSimpleName());
			eventLogEntry.setSummary(logObject.getDescription());
			return eventLogEntry;
		}

		if (eventLogObject instanceof ObjectCreationLog) {
			ObjectCreationLog logObject = (ObjectCreationLog) eventLogObject;
			EventLogEntry eventLogEntry = new EventLogEntry();
			eventLogEntry.setCode(logObject.getEventCode());
			eventLogEntry.setDate(logObject.getEventTime());
			eventLogEntry.setUserName(logObject.getUserName());
			eventLogEntry.setTopic(logObject.getClz().getSimpleName());
			eventLogEntry.setSummary(logObject.getReferredObjType().getSimpleName() + " - " + logObject.getReferredObjId());
			return eventLogEntry;
		}

		if (eventLogObject instanceof ObjectDeletionLog) {
			ObjectDeletionLog logObject = (ObjectDeletionLog) eventLogObject;
			EventLogEntry eventLogEntry = new EventLogEntry();
			eventLogEntry.setCode(logObject.getEventCode());
			eventLogEntry.setDate(logObject.getEventTime());
			eventLogEntry.setUserName(logObject.getUserName());
			eventLogEntry.setTopic(logObject.getClz().getSimpleName());
			eventLogEntry.setSummary(logObject.getReferredObjType().getSimpleName() + " - " + logObject.getReferredObjId());
			return eventLogEntry;
		}

		if (eventLogObject instanceof ObjectUpdateLog) {
			ObjectUpdateLog logObject = (ObjectUpdateLog) eventLogObject;
			EventLogEntry eventLogEntry = new EventLogEntry();
			eventLogEntry.setCode(logObject.getEventCode());
			eventLogEntry.setDate(logObject.getEventTime());
			eventLogEntry.setUserName(logObject.getUserName());
			eventLogEntry.setTopic(logObject.getClz().getSimpleName());
			String summary = logObject.getReferredObjType().getSimpleName() + " - " + logObject.getReferredObjId() + " | ";
			for (int i = 0 ; i < logObject.getChangedFields().size() ; i++) {
				summary += String.format("{%s,%s->%s} ",
						logObject.getChangedFields().get(i),
						logObject.getChangedFromValues().get(i),
						logObject.getChangedToValues().get(i)
				);
			}
			eventLogEntry.setSummary(summary);
			return eventLogEntry;
		}

		return null;
	}

	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(QuadGuiEvent command) {
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

		LOGGER.debug("Refresh Log");
		Platform.runLater(() -> loadTable());
	}
}
