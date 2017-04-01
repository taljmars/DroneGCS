package com.dronegcs.console.controllers.internalPanels;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console.controllers.internalPanels.internal.EditingCell;
import com.dronegcs.console.controllers.internalPanels.internal.MissionItemTableEntry;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerMission;
import com.dronegcs.console_plugin.services.EventPublisherSvc;
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
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.gcsis.validations.RuntimeValidator;
import com.dronegcs.gcsis.validations.ValidatorResponse;

@Component
public class PanelMissionBox extends Pane implements Initializable {
	
	@Autowired @NotNull( message = "Internal Error: Fail to get event publisher" )
	protected EventPublisherSvc eventPublisherSvc;
	
	@NotNull @FXML private TableView<MissionItemTableEntry> table;
    
	@NotNull @FXML private TableColumn<MissionItemTableEntry,Integer> order;
	@NotNull @FXML private TableColumn<MissionItemTableEntry,String> type;
	@NotNull @FXML private TableColumn<MissionItemTableEntry,Double> lat;
	@NotNull @FXML private TableColumn<MissionItemTableEntry,Double> lon;
	@NotNull @FXML private TableColumn<MissionItemTableEntry,Double> height;
	@NotNull @FXML private TableColumn<MissionItemTableEntry,Double> delay;
	@NotNull @FXML private TableColumn<MissionItemTableEntry,Double> radius;
	@NotNull @FXML private TableColumn<MissionItemTableEntry,String> setUp;
	@NotNull @FXML private TableColumn<MissionItemTableEntry,String> setDown;
	@NotNull @FXML private TableColumn<MissionItemTableEntry,String> remove;

	@Autowired @NotNull(message = "Internal Error: Failed to get mission manager")
	private MissionsManager missionsManager;

	@Autowired
	private RuntimeValidator runtimeValidator;
	
	private LayerMission layerMission;
	
	static int called;
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
	    
	    Callback<TableColumn<MissionItemTableEntry, Double>, TableCell<MissionItemTableEntry, Double>> cellFactory = new Callback<TableColumn<MissionItemTableEntry, Double>, TableCell<MissionItemTableEntry, Double>>() {
             public TableCell<MissionItemTableEntry, Double> call(TableColumn<MissionItemTableEntry, Double> p) {
                return new EditingCell<Double>(new DoubleStringConverter());
             }
         };
        
        order.setCellValueFactory(new PropertyValueFactory<>("order"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        lat.setCellValueFactory(new PropertyValueFactory<>("lat"));
        lon.setCellValueFactory(new PropertyValueFactory<>("lon"));
        
        height.setCellValueFactory(new PropertyValueFactory<>("height"));
        height.setCellFactory(cellFactory);
        height.setOnEditCommit( t -> {
        	MissionItemTableEntry entry = t.getTableView().getItems().get(t.getTablePosition().getRow());
//        	if (entry.getMissionItem() instanceof Altitudable) {
//        		Altitudable wp = (Altitudable) entry.getMissionItem();
//        		wp.setAltitude(t.getNewValue());
//        	}
        	generateMissionTable(true);
        	eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
        });
        
        delay.setCellValueFactory(new PropertyValueFactory<>("delay"));
        delay.setCellFactory(cellFactory);
        delay.setOnEditCommit( t -> {
        	MissionItemTableEntry entry = (MissionItemTableEntry) t.getTableView().getItems().get(t.getTablePosition().getRow());
//        	if (entry.getMissionItem() instanceof Delayable) {
//        		Delayable wp = (Delayable) entry.getMissionItem();
//        		wp.setDelay(t.getNewValue());
//        	}
        	generateMissionTable(true);
        	eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
        });
        
        radius.setCellValueFactory(new PropertyValueFactory<>("radius"));
        radius.setCellFactory(cellFactory);
        radius.setOnEditCommit( t -> {
        	MissionItemTableEntry entry = (MissionItemTableEntry) t.getTableView().getItems().get(t.getTablePosition().getRow());
//        	if (entry.getMissionItem() instanceof Radiusable) {
//        		Radiusable wp = (Radiusable) entry.getMissionItem();
//        		wp.setRadius(t.getNewValue());
//        	}
        	generateMissionTable(true);
        	eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
        });
        
        setUp.setCellFactory( param -> {
            final TableCell<MissionItemTableEntry, String> cell = new TableCell<MissionItemTableEntry, String>() {
                final Button btn = new Button("Up");
                @Override
                public void updateItem( String item, boolean empty ) {
                    super.updateItem( item, empty );
                    setGraphic( null );
                    setText( null );
                    if ( !empty && getIndex() > 0 ) {
                        btn.setOnAction( ( ActionEvent event ) -> {
                        	MissionItemTableEntry entry = getTableView().getItems().get( getIndex() );
                        	Mission mission = layerMission.getMission();
                        	mission.getMissionItemsUids().remove(getIndex());
                        	mission.getMissionItemsUids().add(getIndex() - 1, entry.getMissionItem().getObjId());
                            generateMissionTable(true);
                            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
                        });
                        setGraphic( btn );
                    }
                }
            };
            return cell;
        });
        
        setDown.setCellFactory( param -> {
            final TableCell<MissionItemTableEntry, String> cell = new TableCell<MissionItemTableEntry, String>() {
                final Button btn = new Button("Down");
                @Override
                public void updateItem( String item, boolean empty ) {
                    super.updateItem( item, empty );
                    setGraphic( null );
                    setText( null );
                    if ( !empty && getIndex() < getTableView().getItems().size() - 1 ) {
                        btn.setOnAction( ( ActionEvent event ) -> {
                        	MissionItemTableEntry entry = getTableView().getItems().get( getIndex() );
                        	Mission mission = layerMission.getMission();
                            mission.getMissionItemsUids().remove(getIndex());
                            mission.getMissionItemsUids().add(getIndex() + 1, entry.getMissionItem().getObjId());
                            generateMissionTable(true);
                            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
                        });
                        setGraphic( btn );
                    }
                }
            };
            return cell;
        });
        
        remove.setCellFactory( param -> {
            final TableCell<MissionItemTableEntry, String> cell = new TableCell<MissionItemTableEntry, String>() {
                final Button btn = new Button("X");
                @Override
                public void updateItem( String item, boolean empty ) {
                    super.updateItem( item, empty );
                    setGraphic( null );
                    setText( null );
                    if ( !empty ) {
                        btn.setOnAction( ( ActionEvent event ) -> {
                        	MissionItemTableEntry entry = getTableView().getItems().get( getIndex() );
							MissionEditor missionEditor = missionsManager.getMissionEditor(layerMission.getMission());
							missionEditor.removeMissionItem(entry.getMissionItem());
                            generateMissionTable(true);
                            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerMission));
                        });
                        setGraphic( btn );
                    }
                }
            };
            return cell;
        });
	}

	@SuppressWarnings("unchecked")
	public void generateMissionTable(boolean editmode) {
		MissionItemTableEntry entry = null;
		
		if (table == null) {
			throw new RuntimeException("Failed to get Table");
		}
		
		if (layerMission == null) {
			table.setItems(null);
			return;
		}
		
		Mission droneMission = layerMission.getMission();
		if (droneMission == null) {
			table.setItems(null);
			return;
		}
		
		// Setting columns
		table.getColumns().removeAll(table.getColumns());
		if (!editmode)
			table.getColumns().addAll(order, type, lat, lon, height, delay, radius);
		else 
			table.getColumns().addAll(order, type, lat, lon, height, delay, radius, setUp, setDown, remove);
		
		height.setEditable(editmode);
		delay.setEditable(editmode);
		radius.setEditable(editmode);
		
		// Start loading items
		ObservableList<MissionItemTableEntry> data = FXCollections.observableArrayList();

		List<MissionItem> missionItemList = missionsManager.getMissionItems(layerMission.getMission());

		Iterator<MissionItem> it = missionItemList.iterator();

		int i = 0;
		while (it.hasNext()) {

			MissionItem mItem = (it.next());

			if (mItem instanceof Waypoint) {
				Waypoint wp = (Waypoint) mItem;
				entry = new MissionItemTableEntry(i, Waypoint.class.getSimpleName(), wp.getLat(), wp.getLon(), wp.getAltitude(), wp.getDelay(), 0.0, mItem);
			}
			else if (mItem instanceof Circle) {
				Circle wp = (Circle) mItem;
				entry = new MissionItemTableEntry(i, Circle.class.getSimpleName(), wp.getLat(), wp.getLon(), wp.getAltitude(), 0.0, wp.getRadius(), mItem);
			}
			else if (mItem instanceof Land) {
				entry = new MissionItemTableEntry(i, Land.class.getSimpleName(), 0.0, 0.0, 0.0, 0.0, 0.0, mItem);
			}
			else if (mItem instanceof ReturnToHome) {
				entry = new MissionItemTableEntry(i, ReturnToHome.class.getSimpleName(), 0.0, 0.0, 0.0, 0.0, 0.0, mItem);
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
				System.out.println("Unepectd");
			}
			
			i++;
			data.add(entry);
		}
		
		
		table.setItems(data);
		table.setEditable(editmode);
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
			LayerMission layerMission;
			switch (command.getCommand()) {
				case MISSION_EDITING_STARTED:
				case MISSION_UPDATED_BY_MAP:
					layerMission = (LayerMission) command.getSource();
					setLayerMission(layerMission);
					generateMissionTable(true);
					break;
				case MISSION_EDITING_FINISHED:
					unsetLayerMission();
					generateMissionTable(false);
					break;
				case MISSION_VIEW_ONLY_STARTED:
					layerMission = (LayerMission) command.getSource();
					setLayerMission(layerMission);
					generateMissionTable(false);
					break;
				case MISSION_VIEW_ONLY_FINISHED:
					setLayerMission(null);
					generateMissionTable(false);
					break;
			}
		});
	}

}
