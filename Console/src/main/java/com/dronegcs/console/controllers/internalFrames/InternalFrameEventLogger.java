package com.dronegcs.console.controllers.internalFrames;

import com.auditdb.persistence.base_scheme.EventLogObject;
import com.auditdb.persistence.scheme.AccessLog;
import com.auditdb.persistence.scheme.ObjectCreationLog;
import com.auditdb.persistence.scheme.ObjectDeletionLog;
import com.auditdb.persistence.scheme.ObjectUpdateLog;
import com.dronegcs.console.controllers.EditingCell;
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

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

@Component
public class InternalFrameEventLogger extends Pane implements Initializable {

	private final static Logger LOGGER = LoggerFactory.getLogger(InternalFrameEventLogger.class);


	@NotNull @FXML private TableView<EventLogEntry> table;

	@NotNull @FXML private TableColumn<TableItemEntry,ImageView> icon;
	@NotNull @FXML private TableColumn<TableItemEntry,String> id;
	@NotNull @FXML private TableColumn<TableItemEntry,Date> date;
	@NotNull @FXML private TableColumn<TableItemEntry,String> userName;
	@NotNull @FXML private TableColumn<TableItemEntry,String> code;
	@NotNull @FXML private TableColumn<TableItemEntry,String> topic;
	@NotNull @FXML private TableColumn<TableItemEntry,String> summary;
	
	@Autowired
	private RuntimeValidator runtimeValidator;

	@Autowired
	private EventLogManager eventLogManager;

	private Date lastPull = null;

	private ObservableList<EventLogEntry> data;

	@Autowired @NotNull( message="Internal Error: Failed to get application context" )
	private ApplicationContext applicationContext;

	@NotNull @FXML private Pane root;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		table.setPrefWidth(root.getPrefWidth());
		table.setPrefHeight(root.getPrefHeight());

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
	}

	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
	}

	public void loadTable() {
		if (table == null) {
			LOGGER.debug("Table wasn't initialize yet");
			return;
		}

		EventLogBundle eventLogBundle = eventLogManager.getAllEventLogs();
		eventLogBundle = eventLogBundle.sortByEventDate();
		if (eventLogBundle.getLogs().size() > 0) {
			int i = 0;
			data = FXCollections.observableArrayList();
			for (EventLogObject eventLogObject : eventLogBundle.getLogs()) {
				EventLogEntry entry = buildEntry(eventLogObject);
				entry.setId(i++);
				data.add(entry);
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

	@Scheduled(fixedRate = 10 * 1000)
	public void tik() {
		LOGGER.debug("Refresh Log");
		Platform.runLater(() -> loadTable());
	}
}
