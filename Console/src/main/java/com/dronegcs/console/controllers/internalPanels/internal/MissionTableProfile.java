package com.dronegcs.console.controllers.internalPanels.internal;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console.controllers.EditingCell;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerMission;
import com.dronegcs.console.controllers.internalPanels.PanelTableBox;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.services.EventPublisherSvc;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.generic_tools.logger.Logger;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by taljmars on 7/10/2017.
 */
@Component
public class MissionTableProfile extends TableProfile {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MissionTableProfile.class);

    private LayerMission layerMission;

    @Autowired
    private EventPublisherSvc eventPublisherSvc;

    @Autowired @NotNull(message = "Internal Error: Failed to get mission manager")
    private MissionsManager missionsManager;

    @Autowired @NotNull(message = "Internal Error: Failed to get logger")
    private Logger logger;

    @Autowired @NotNull(message = "Internal Error: Failed to get drone")
    private Drone drone;

    @Autowired @NotNull(message = "Internal Error: Failed to get application context")
    private ApplicationContext applicationContext;

    @Autowired @NotNull(message = "Internal Error: Failed to get validator")
    private RuntimeValidator runtimeValidator;

    //Validation should be executed during load function
    @NotNull(message = "Internal Error: Table wasn't initialized")
    private PanelTableBox panelTableBox;


    public MissionTableProfile() {}

    @Override
    public void setBigTableView(PanelTableBox panelTableBox) {
        this.panelTableBox = panelTableBox;
    }

    private void load() {

        // Validate all the relevant fields were initialised
        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        Callback<TableColumn<TableItemEntry, Double>, TableCell<TableItemEntry, Double>> cellFactoryDouble = new Callback<TableColumn<TableItemEntry, Double>, TableCell<TableItemEntry, Double>>() {
            public TableCell<TableItemEntry, Double> call(TableColumn<TableItemEntry, Double> p) {
                EditingCell editingCell = new EditingCell<TableItemEntry, Double>(new DoubleStringConverter());
                editingCell.setApplicationContext(applicationContext);
                return editingCell;
            }
        };

        Callback<TableColumn<TableItemEntry, Integer>, TableCell<TableItemEntry, Integer>> cellFactoryInteger = new Callback<TableColumn<TableItemEntry, Integer>, TableCell<TableItemEntry, Integer>>() {
            public TableCell<TableItemEntry, Integer> call(TableColumn<TableItemEntry, Integer> p) {
                EditingCell editingCell = new EditingCell<TableItemEntry, Integer>(new IntegerStringConverter());
                editingCell.setApplicationContext(applicationContext);
                return editingCell;
            }
        };

        ColumnTypeAwareEditingCell.PostCommit postAction = new ColumnTypeAwareEditingCell.PostCommit() {
            @Override
            public boolean call(Object entry) {
                try {
                    // Updating DB
                    MissionEditor missionEditor = missionsManager.getMissionEditor(layerMission.getMission());
                    missionEditor.updateMissionItem((MissionItem) entry);

                    // Refresh view
                    generateTable(true, layerMission);
                    eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
                    return true;
                }
                catch (MissionUpdateException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        panelTableBox.getOrder().setCellValueFactory(new PropertyValueFactory<>("order"));
        panelTableBox.getType().setCellValueFactory(new PropertyValueFactory<>("type"));
        panelTableBox.getLat().setCellValueFactory(new PropertyValueFactory<>("lat"));
        panelTableBox.getLon().setCellValueFactory(new PropertyValueFactory<>("lon"));

        panelTableBox.getAltitude().setCellValueFactory(new PropertyValueFactory<>("altitude"));
        panelTableBox.getAltitude().setCellFactory(param -> {
            ColumnTypeAwareEditingCell col = new ColumnTypeAwareEditingCell<>(Arrays.asList(Takeoff.class), null,
                    new DoubleStringConverter(),
                    "setAltitude", "getAltitude",
                    postAction);
            return col;
        });

        panelTableBox.getDelayOrTime().setCellValueFactory(new PropertyValueFactory<>("delayOrTime"));
        panelTableBox.getDelayOrTime().setCellFactory(t -> {
            ColumnTypeAwareEditingCell<TableItemEntry, Integer> col = new ColumnTypeAwareEditingCell<TableItemEntry, Integer>(null, Arrays.asList(LoiterTime.class),
                    new IntegerStringConverter(),
                    "setSeconds", "getSeconds",
                    postAction);
            return col;
        });

        panelTableBox.getRadius().setCellValueFactory(new PropertyValueFactory<>("radius"));

        panelTableBox.getTurns().setCellValueFactory(new PropertyValueFactory<>("turns"));
        panelTableBox.getTurns().setCellFactory(t -> {
            ColumnTypeAwareEditingCell<TableItemEntry, Integer> col = new ColumnTypeAwareEditingCell<TableItemEntry, Integer>(null, Arrays.asList(LoiterTurns.class),
                    new IntegerStringConverter(),
                    "setTurns", "getTurns",
                    postAction);
            return col;
        });

        panelTableBox.getUp().setCellFactory(param -> {
            final TableCell<TableItemEntry, String> cell = new TableCell<TableItemEntry, String>() {
                final Button btn = new Button("Up");
                @Override
                public void updateItem( String item, boolean empty ) {
                    super.updateItem( item, empty );
                    setGraphic( null );
                    setText( null );
                    if ( !empty && getIndex() > 0 ) {
                        btn.setOnAction( ( ActionEvent event ) -> {
                            TableItemEntry entry = getTableView().getItems().get( getIndex() );
                            Mission mission = layerMission.getMission();
                            mission.getMissionItemsUids().remove(getIndex());
                            mission.getMissionItemsUids().add(getIndex() - 1, ((MissionItem) entry.getReferredItem()).getKeyId().getObjId());
                            generateTable(true, layerMission);
                            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
                        });
                        setGraphic( btn );
                    }
                }
            };
            return cell;
        });

        panelTableBox.getDown().setCellFactory(param -> {
            final TableCell<TableItemEntry, String> cell = new TableCell<TableItemEntry, String>() {
                final Button btn = new Button("Down");
                @Override
                public void updateItem( String item, boolean empty ) {
                    super.updateItem( item, empty );
                    setGraphic( null );
                    setText( null );
                    if ( !empty && getIndex() < getTableView().getItems().size() - 1 ) {
                        btn.setOnAction( ( ActionEvent event ) -> {
                            TableItemEntry entry = getTableView().getItems().get( getIndex() );
                            Mission mission = layerMission.getMission();
                            mission.getMissionItemsUids().remove(getIndex());
                            mission.getMissionItemsUids().add(getIndex() + 1, ((MissionItem)entry.getReferredItem()).getKeyId().getObjId());
                            generateTable(true, layerMission);
                            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
                        });
                        setGraphic( btn );
                    }
                }
            };
            return cell;
        });

        panelTableBox.getRemove().setCellFactory(param -> {
            final TableCell<TableItemEntry, String> cell = new TableCell<TableItemEntry, String>() {
                final Button btn = new Button("X");
                @Override
                public void updateItem( String item, boolean empty ) {
                    super.updateItem( item, empty );
                    setGraphic( null );
                    setText( null );
                    if ( !empty ) {
                        btn.setOnAction( ( ActionEvent event ) -> {
                            try {
                                TableItemEntry entry = getTableView().getItems().get( getIndex() );
                                MissionEditor missionEditor = missionsManager.getMissionEditor(layerMission.getMission());
                                missionEditor.removeMissionItem((MissionItem) entry.getReferredItem());
                                generateTable(true, layerMission);
                                eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
                            }
                            catch (MissionUpdateException e) {
                                logger.LogErrorMessege("Failed to change database, error: " + e.getMessage());
                            }
                        });
                        setGraphic( btn );
                    }
                }
            };
            return cell;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    //public void generateMissionTable(boolean editmode) {
    public void generateTable(boolean editMode, Object contentPayload) {

        setLayerMission((LayerMission) contentPayload);

        TableItemEntry entry = null;

        if (panelTableBox.getTable() == null) {
            throw new RuntimeException("Failed to get Table");
        }

        if (layerMission == null) {
            panelTableBox.getTable().setItems(null);
            return;
        }

        Mission droneMission = layerMission.getMission();
        if (droneMission == null) {
            panelTableBox.getTable().setItems(null);
            return;
        }

        // Setting columns
        panelTableBox.getTable().getColumns().removeAll(panelTableBox.getTable().getColumns());

        int radiusCentimeters = 0;
        if ((drone.getParameters()) != null && drone.getParameters().getParameter("CIRCLE_RADIUS") != null)
            radiusCentimeters = Integer.parseInt(drone.getParameters().getParameter("CIRCLE_RADIUS").getValue());
        double radiusMeters = radiusCentimeters / 100;
        if (!editMode)
            panelTableBox.getTable().getColumns().addAll(
                    panelTableBox.getOrder(), panelTableBox.getType(),
                    panelTableBox.getLat(), panelTableBox.getLon(), panelTableBox.getAltitude(),
                    panelTableBox.getDelayOrTime(), panelTableBox.getRadius(), panelTableBox.getTurns());
        else
            panelTableBox.getTable().getColumns().addAll(
                    panelTableBox.getOrder(), panelTableBox.getType(),
                    panelTableBox.getLat(), panelTableBox.getLon(), panelTableBox.getAltitude(),
                    panelTableBox.getDelayOrTime(), panelTableBox.getRadius(), panelTableBox.getTurns(),
                    panelTableBox.getUp(), panelTableBox.getDown(), panelTableBox.getRemove());

        panelTableBox.getAltitude().setEditable(editMode);
        panelTableBox.getDelayOrTime().setEditable(editMode);
//        panelTableBox.getRadius().setEditable(editMode);
        panelTableBox.getTurns().setEditable(editMode);

        // Start loading items
        ObservableList<TableItemEntry> data = FXCollections.observableArrayList();

        List<MissionItem> missionItemList = missionsManager.getMissionItems(layerMission.getMission());

        Iterator<MissionItem> it = missionItemList.iterator();

        int i = 0;
        while (it.hasNext()) {

            MissionItem mItem = (it.next());

            if (mItem instanceof Waypoint) {
                Waypoint wp = (Waypoint) mItem;
                entry = new TableItemEntry(i, Waypoint.class.getSimpleName(), wp.getLat(), wp.getLon(), wp.getAltitude(), wp.getDelay(), 0.0, 0, mItem);
            }
            else if (mItem instanceof SplineWaypoint) {
                SplineWaypoint wp = (SplineWaypoint) mItem;
                entry = new TableItemEntry(i, SplineWaypoint.class.getSimpleName(), wp.getLat(), wp.getLon(), wp.getAltitude(), 0.0, 0.0,0, mItem);
            }
            else if (mItem instanceof LoiterTurns) {
                LOGGER.error("MITEM- {}", mItem);
                LoiterTurns wp = (LoiterTurns) mItem;
                entry = new TableItemEntry(i, LoiterTurns.class.getSimpleName(), wp.getLat(), wp.getLon(), wp.getAltitude(), 0.0, radiusMeters, wp.getTurns(), mItem);
            }
            else if (mItem instanceof LoiterTime) {
                LOGGER.error("MITEM- {}", mItem);
                LoiterTime wp = (LoiterTime) mItem;
                entry = new TableItemEntry(i, LoiterTime.class.getSimpleName(), wp.getLat(), wp.getLon(), wp.getAltitude(), wp.getSeconds() * 1.0, radiusMeters, 0, mItem);
            }
            else if (mItem instanceof LoiterUnlimited) {
                LoiterUnlimited wp = (LoiterUnlimited) mItem;
                entry = new TableItemEntry(i, LoiterUnlimited.class.getSimpleName(), wp.getLat(), wp.getLon(), wp.getAltitude(), 0.0, radiusMeters, 0, mItem);
            }
            else if (mItem instanceof Land) {
                entry = new TableItemEntry(i, Land.class.getSimpleName(), 0.0, 0.0, 0.0, 0.0, 0.0, 0, mItem);
            }
            else if (mItem instanceof ReturnToHome) {
                entry = new TableItemEntry(i, ReturnToHome.class.getSimpleName(), 0.0, 0.0, 0.0, 0.0, 0.0, 0, mItem);
            }
            else if (mItem instanceof Takeoff) {
				Takeoff takeoff = (Takeoff) mItem;
				entry = new TableItemEntry(i, Takeoff.class.getSimpleName(), 0.0, 0.0, takeoff.getFinishedAlt(), 0.0, 0.0, 0, mItem);
            }
            else if (mItem instanceof RegionOfInterest) {
                RegionOfInterest regionOfInterest = (RegionOfInterest) mItem;
                entry = new TableItemEntry(i, RegionOfInterest.class.getSimpleName(), regionOfInterest.getLat(), regionOfInterest.getLon(), regionOfInterest.getAltitude(), 0.0, 0.0, 0, mItem);
            }
            else {
                throw new RuntimeException("Unexpected type: " + mItem);
            }

            i++;
            data.add(entry);
        }


        panelTableBox.getTable().setItems(data);
        panelTableBox.getTable().setEditable(editMode);
    }

    private void setLayerMission(LayerMission layerMission) {
        this.layerMission = layerMission;
    }

    private void unsetLayerMission() {
        this.layerMission = null;
    }

    @SuppressWarnings("incomplete-switch")
    @EventListener
    public void onApplicationEvent(QuadGuiEvent command) {
        Platform.runLater( () -> {
            switch (command.getCommand()) {
                case MISSION_EDITING_STARTED:
                case MISSION_UPDATED_BY_MAP:
                    load();
                    generateTable(true, command.getSource());
                    break;
                case PUBLISH:
                case DISCARD:
                    if (layerMission == null)
                        break;
                case MISSION_EDITING_FINISHED:
                    load();
                    generateTable(false, null);
                    break;
                case MISSION_VIEW_ONLY_STARTED:
                    load();
                    generateTable(false, command.getSource());
                    break;
                case MISSION_VIEW_ONLY_FINISHED:
                    load();
                    generateTable(false, null);
                    break;
            }
        });
    }
}
