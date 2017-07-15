package com.dronegcs.console.controllers.internalPanels.internal;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerMission;
import com.dronegcs.console.controllers.internalPanels.PanelTableBox;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.services.EventPublisherSvc;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;

/**
 * Created by taljmars on 7/10/2017.
 */
@Component
public class MissionTableProfile extends TableProfile {

    private LayerMission layerMission;

    @Autowired
    private EventPublisherSvc eventPublisherSvc;

    @Autowired @NotNull(message = "Internal Error: Failed to get mission manager")
    private MissionsManager missionsManager;

    @Autowired @NotNull(message = "Internal Error: Failed to get logger")
    private Logger logger;

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

        Callback<TableColumn<TableItemEntry, Double>, TableCell<TableItemEntry, Double>> cellFactory = new Callback<TableColumn<TableItemEntry, Double>, TableCell<TableItemEntry, Double>>() {
            public TableCell<TableItemEntry, Double> call(TableColumn<TableItemEntry, Double> p) {
                return new EditingCell<Double>(new DoubleStringConverter());
            }
        };

        panelTableBox.getOrder().setCellValueFactory(new PropertyValueFactory<>("order"));
        panelTableBox.getType().setCellValueFactory(new PropertyValueFactory<>("type"));
        panelTableBox.getLat().setCellValueFactory(new PropertyValueFactory<>("lat"));
        panelTableBox.getLon().setCellValueFactory(new PropertyValueFactory<>("lon"));

        panelTableBox.getAltitude().setCellValueFactory(new PropertyValueFactory<>("height"));
        panelTableBox.getAltitude().setCellFactory(cellFactory);
        panelTableBox.getAltitude().setOnEditCommit(t -> {
            TableItemEntry entry = t.getTableView().getItems().get(t.getTablePosition().getRow());
//        	if (entry.getReferedItem() instanceof Altitudable) {
//        		Altitudable wp = (Altitudable) entry.getReferedItem();
//        		wp.setAltitude(t.getNewValue());
//        	}
            generateTable(true, this.layerMission);
            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
        });

        panelTableBox.getDelay().setCellValueFactory(new PropertyValueFactory<>("delay"));
        panelTableBox.getDelay().setCellFactory(cellFactory);
        panelTableBox.getDelay().setOnEditCommit(t -> {
            TableItemEntry entry = (TableItemEntry) t.getTableView().getItems().get(t.getTablePosition().getRow());
//        	if (entry.getReferedItem() instanceof Delayable) {
//        		Delayable wp = (Delayable) entry.getReferedItem();
//        		wp.setDelay(t.getNewValue());
//        	}
            generateTable(true, layerMission);
            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
        });

        panelTableBox.getRadius().setCellValueFactory(new PropertyValueFactory<>("radius"));
        panelTableBox.getRadius().setCellFactory(cellFactory);
        panelTableBox.getRadius().setOnEditCommit(t -> {
            TableItemEntry entry = (TableItemEntry) t.getTableView().getItems().get(t.getTablePosition().getRow());
//        	if (entry.getReferedItem() instanceof Radiusable) {
//        		Radiusable wp = (Radiusable) entry.getReferedItem();
//        		wp.setRadius(t.getNewValue());
//        	}
            generateTable(true, this.layerMission);
            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
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
                            mission.getMissionItemsUids().add(getIndex() - 1, ((MissionItem) entry.getReferedItem()).getKeyId().getObjId());
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
                            mission.getMissionItemsUids().add(getIndex() + 1, ((MissionItem)entry.getReferedItem()).getKeyId().getObjId());
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
                                missionEditor.removeMissionItem((MissionItem) entry.getReferedItem());
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
        if (!editMode)
            panelTableBox.getTable().getColumns().addAll(
                    panelTableBox.getOrder(), panelTableBox.getType(),
                    panelTableBox.getLat(), panelTableBox.getLon(), panelTableBox.getAltitude(),
                    panelTableBox.getDelay(), panelTableBox.getRadius());
        else
            panelTableBox.getTable().getColumns().addAll(
                    panelTableBox.getOrder(), panelTableBox.getType(),
                    panelTableBox.getLat(), panelTableBox.getLon(), panelTableBox.getAltitude(),
                    panelTableBox.getDelay(), panelTableBox.getRadius(),
                    panelTableBox.getUp(), panelTableBox.getDown(), panelTableBox.getRemove());

        panelTableBox.getAltitude().setEditable(editMode);
        panelTableBox.getDelay().setEditable(editMode);
        panelTableBox.getRadius().setEditable(editMode);

        // Start loading items
        ObservableList<TableItemEntry> data = FXCollections.observableArrayList();

        List<MissionItem> missionItemList = missionsManager.getMissionItems(layerMission.getMission());

        Iterator<MissionItem> it = missionItemList.iterator();

        int i = 0;
        while (it.hasNext()) {

            MissionItem mItem = (it.next());

            if (mItem instanceof Waypoint) {
                Waypoint wp = (Waypoint) mItem;
                entry = new TableItemEntry(i, Waypoint.class.getSimpleName(), wp.getLat(), wp.getLon(), wp.getAltitude(), wp.getDelay(), 0.0, mItem);
            }
            else if (mItem instanceof Circle) {
                Circle wp = (Circle) mItem;
                entry = new TableItemEntry(i, Circle.class.getSimpleName(), wp.getLat(), wp.getLon(), wp.getAltitude(), 0.0, wp.getRadius(), mItem);
            }
            else if (mItem instanceof Land) {
                entry = new TableItemEntry(i, Land.class.getSimpleName(), 0.0, 0.0, 0.0, 0.0, 0.0, mItem);
            }
            else if (mItem instanceof ReturnToHome) {
                entry = new TableItemEntry(i, ReturnToHome.class.getSimpleName(), 0.0, 0.0, 0.0, 0.0, 0.0, mItem);
            }
            else if (mItem instanceof Takeoff) {
//				msg_mission_item msg = mItem.packMissionItem().get(0);
//				double alt = (double) msg.z;
//				entry = new MissionItemTableEntry(i, MissionItemType.TAKEOFF, 0.0, 0.0, alt, 0.0, 0.0,  mItem);
            }
//				case SPLINE_WAYPOINT:
            //return new MavlinkSplineWaypoint(referenceItem);
//					break;
//				case CHANGE_SPEED:
            //return new MavlinkChangeSpeed(referenceItem);
//				case CAMERA_TRIGGER:
            //return new MavlinkCameraTrigger(referenceItem);
//				case EPM_GRIPPER:
//					return new MavlinkEpmGripper(referenceItem);
//				case ROI:
//					MavlinkRegionOfInterest roi = (MavlinkRegionOfInterest) mItem;
//					entry = new MissionItemTableEntry(i, MissionItemType.ROI, roi.getCoordinate().getLat(), roi.getCoordinate().getLon(), roi.getCoordinate().getAltitude(), 0.0, 0.0, mItem);
//					break;
//				case SURVEY:
            //return new MavlinkSurvey(referenceItem.getMission(), Collections.<Coord2D> emptyList());
//				case CYLINDRICAL_SURVEY:
            //return new MavlinkStructureScanner(referenceItem);
//				default:
//					break;

            else {
                System.out.println("Unexpected");
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
