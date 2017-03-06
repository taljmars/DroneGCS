//==============================================================================
//   JFXMapPane is a Java library for parsing raw weather data
//   Copyright (C) 2012 Jeffrey L Smith
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//    
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//    
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//    
//  For more information, please email jsmith.carlsbad@gmail.com
//    
//==============================================================================
package gui.controllers.internalFrames.internal;

import java.awt.Point;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import gui.controllers.internalFrames.internal.view_tree_layers.LayerMission;
import gui.controllers.internalFrames.internal.view_tree_layers.LayerPolygonPerimeter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import gui.controllers.internalFrames.internal.view_tree_layers.LayerCircledPerimeter;
import gui.controllers.internalFrames.internal.view_tree_layers.LayerPerimeter;
import operations.core.operations.MissionBuilder;
import is.devices.KeyBoardController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import gui.core.mapTreeObjects.Layer;
import gui.core.mapViewer.LayeredViewMap;
import gui.core.mapViewerObjects.MapLineImpl;
import gui.core.mapViewerObjects.MapMarkerCircle;
import gui.core.mapViewerObjects.MapMarkerDot;
import gui.core.mapViewerObjects.MapVectorImpl;
import is.gui.events.QuadGuiEvent;
import gui.is.interfaces.mapObjects.MapLine;
import gui.is.interfaces.mapObjects.MapMarker;
import is.gui.services.DialogManagerSvc;
import is.gui.services.LoggerDisplayerSvc;
import is.gui.services.TextNotificationPublisherSvc;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import is.mavlink.drone.Drone;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.drone.DroneInterfaces.OnDroneListener;
import is.mavlink.drone.mission.Mission;
import is.mavlink.drone.variables.GuidedPoint;
import is.mavlink.drone.variables.Home;
import is.springConfig.AppConfig;
import geoTools.Coordinate;
import geoTools.GeoTools;
import is.validations.RuntimeValidator;

/**
 *
 * @author taljmars
 */

@ComponentScan("tools.validations")
@ComponentScan("gui.is.services")
@ComponentScan("gui.controllers.internalFrames.internal")
@Component
public class OperationalViewMap extends LayeredViewMap implements
OnDroneListener, EventHandler<ActionEvent> {
	
	@Autowired @NotNull( message = "Internal Error: Failed to get drone" )
	private Drone drone;

	@Autowired @NotNull( message = "Internal Error: Failed to get keyboard controler" )
	private MissionBuilder missionBuilder;
	
	@Autowired @NotNull( message = "Internal Error: Failed to get keyboard controler" )
	private KeyBoardController keyboardController;
	
	@Autowired @NotNull( message = "Internal Error: Failed to get notification publisher" )
	private TextNotificationPublisherSvc textNotificationPublisherSvc;
	
	@Autowired @NotNull( message = "Internal Error: Failed to get logger displayer" )
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;

	@Resource(type = OperationalViewTree.class)
	public void setOperationalViewTree(OperationalViewTree operationalViewTree) {
		super.setCheckBoxViewTree(operationalViewTree);
	}

	public OperationalViewTree getOperationalViewTree() {
		return (OperationalViewTree) super.getCheckBoxViewTree();
	}
	
	@Autowired
	private RuntimeValidator runtimeValidator;

	private boolean lockMapOnMyPosition = true;
	private boolean paintTrail = true;
	private MapLineImpl myTrailPath = null;
	
	private MapMarkerDot guidedPoint = null;
	private MapMarkerDot myPos = null;
	private MapLine bearing = null;
	private MapMarkerDot myHome = null;
	private MapMarkerDot myBeacon = null;
	private MapMarkerDot myGCS = null;
	
	// Mission Builder
	private LayerMission modifyiedLayerMission = null;
	private LayerMission modifyiedLayerMissionOriginal = null;

	// Perimeter Builder
	private LayerPerimeter modifyiedLayerPerimeter = null;
	private LayerPerimeter modifyiedLayerPerimeterOriginal = null;
	
	private boolean isPerimeterBuildMode = false;
	private boolean isMissionBuildMode = false;
	
	private CheckBox cbLockMyPos;
	private CheckBox cbFollowTrail;
	
	private ContextMenu popup;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		setDisplayPosition(new Coordinate(32.0684, 34.8248), 10);
		drone.addDroneListener(this);
		
		if (!runtimeValidator.validate(this))
			throw new RuntimeException("Value weren't initialized");
	}
	
	private ContextMenu buildPopup(Point point) {
		ContextMenu popup = new ContextMenu();		
		
		MenuItem menuItemFlyTo = new MenuItem("Fly to Position");
		MenuItem menuItemMissionBuild = new MenuItem("Build Mission");
		MenuItem menuItemMissionAddWayPoint = new MenuItem("Add Way Point");
		MenuItem menuItemMissionAddCircle = new MenuItem("Add Circle");
		MenuItem menuItemMissionAddROI = new MenuItem("Add ROI");
		MenuItem menuItemMissionSetHome = new MenuItem("Set Home");
		MenuItem menuItemMissionSetLandPoint = new MenuItem("Set Land Point");
		MenuItem menuItemMissionSetRTL = new MenuItem("Set RTL");
		MenuItem menuItemMissionSetTakeOff = new MenuItem("Set Takeoff");
		MenuItem menuItemDist = new MenuItem("Distance -m");
		MenuItem menuItemPerimeterBuild = new MenuItem("Build Perimeter");
		MenuItem menuItemPerimeterAddPoint = new MenuItem("Add Point");
		MenuItem menuItemSyncMission = new MenuItem("Sync Mission");
		MenuItem menuItemFindClosest = new MenuItem("Find closest Here");

		menuItemFlyTo.setDisable(!drone.getGps().isPositionValid());
		menuItemDist.setDisable(!drone.getGps().isPositionValid());
		menuItemMissionAddWayPoint.setVisible(isMissionBuildMode);
		menuItemMissionAddCircle.setVisible(isMissionBuildMode);
		menuItemMissionAddROI.setVisible(isMissionBuildMode);
		menuItemMissionSetHome.setVisible(drone.getGps().isPositionValid() && !isMissionBuildMode && !isPerimeterBuildMode);
		menuItemMissionSetLandPoint.setVisible(isMissionBuildMode);
		menuItemMissionSetRTL.setVisible(isMissionBuildMode);
		menuItemMissionSetTakeOff.setVisible(isMissionBuildMode);
		menuItemMissionBuild.setDisable(isMissionBuildMode || isPerimeterBuildMode);
		menuItemPerimeterBuild.setDisable(isMissionBuildMode || isPerimeterBuildMode);
		menuItemSyncMission.setDisable(isMissionBuildMode || isPerimeterBuildMode);
		menuItemPerimeterAddPoint.setVisible(isPerimeterBuildMode);

		if (drone.getGps().isPositionValid()) {
			Coordinate iCoord = getPosition(point);
			Coordinate to = new Coordinate(iCoord.getLat(), iCoord.getLon());
			Coordinate from = drone.getGps().getPosition();
			int dist = (int) GeoTools.getDistance(from, to);
			menuItemDist.setText("Distance " + dist + "m");
		}

		// Create the popup menu.
		popup.getItems().add(menuItemFlyTo);
		popup.getItems().add(menuItemDist);
		//popup.getItems().addSeparator();
		popup.getItems().add(menuItemMissionBuild);
		popup.getItems().add(menuItemPerimeterBuild);
		//popup.getItems().addSeparator();
		popup.getItems().add(menuItemMissionAddWayPoint);
		popup.getItems().add(menuItemMissionAddCircle);
		popup.getItems().add(menuItemMissionAddROI);
		popup.getItems().add(menuItemMissionSetLandPoint);
		popup.getItems().add(menuItemMissionSetRTL);
		popup.getItems().add(menuItemMissionSetHome);
		popup.getItems().add(menuItemMissionSetTakeOff);
		popup.getItems().add(menuItemPerimeterAddPoint);
		//popup.getItems().addSeparator();
		popup.getItems().add(menuItemSyncMission);
		popup.getItems().add(menuItemFindClosest);
		
		menuItemFindClosest.setOnAction( arg -> {
			MapMarker mm = new MapMarkerDot(getPosition(point));
			addMapMarker(mm);
		});

		menuItemFlyTo.setOnAction( arg -> {
				if (!drone.getGps().isPositionValid()) {
					dialogManagerSvc.showAlertMessageDialog("Drone must have a GPS connection to use guideness");
					return;
				}
				if (!GuidedPoint.isGuidedMode(drone)) {
					int n = dialogManagerSvc.showConfirmDialog("Drone Mode must be changed to GUIDED inorder to set point.\n"
									+ "Would you like to change mode?", "");
					if (n == DialogManagerSvc.NO_OPTION) {
						return;
					}
				}

				try {
					if (guidedPoint != null) {
						guidedPoint.setColor(Color.GRAY);
					}

					Coordinate coord = getPosition(point);
					Coordinate coord2d = new Coordinate(coord.getLat(), coord.getLon());

					drone.getGuidedPoint().forcedGuidedCoordinate(coord2d);

					guidedPoint = new MapMarkerDot(coord.getLat(), coord.getLon());
					addMapMarker(guidedPoint);
					loggerDisplayerSvc.logGeneral("Flying to guided point " + guidedPoint.getCoordinate().toString());
				} catch (Exception e1) {
					dialogManagerSvc.showErrorMessageDialog("Failed to handle FlyTo request", e1);
				}
			}
		);

		menuItemSyncMission.setOnAction( arg -> {
				System.out.println(getClass().getName() + " Start Sync Mission");
				drone.getWaypointManager().getWaypoints();
				loggerDisplayerSvc.logOutgoing("Send Sync Request");
			}
		);
		
		menuItemPerimeterBuild.setOnAction( arg -> {
				System.out.println(getClass().getName() + " Start GeoFence");
				String[] options = { "Cycle", "Polygon", "Cancel" };
				int n = dialogManagerSvc
						.showOptionsDialog("Choose a way to create perimeter.",
								"Perimeter Limitation",
								null, options,
								options[2]);
				switch (n) {
				case 0:
					removeMapMarker(perimeterBreachPointMarker);
					perimeterBreachPointMarker = null;
					loggerDisplayerSvc.logGeneral("Start GeoFence of perimeter type");
					
					if (modifyiedLayerPerimeter == null) {
						modifyiedLayerPerimeter = new LayerCircledPerimeter("New Circled Perimeter*", this);
						getOperationalViewTree().addLayer(modifyiedLayerPerimeter);
						System.err.println(getOperationalViewTree().dumpTree());
						super.startModifiedLayerMode(modifyiedLayerPerimeter);
					}
					isPerimeterBuildMode = true;
					
					loggerDisplayerSvc.logGeneral("Start GeoFence of manual circle type");
					break;
				case 1:
					removeMapMarker(perimeterBreachPointMarker);
					perimeterBreachPointMarker = null;
					loggerDisplayerSvc.logGeneral("Start GeoFence of perimeter type");

					if (modifyiedLayerPerimeter == null) {
						modifyiedLayerPerimeter = new LayerPolygonPerimeter("New Perimeter*", this);
						getOperationalViewTree().addLayer(modifyiedLayerPerimeter);
						super.startModifiedLayerMode(modifyiedLayerPerimeter);
					}
					isPerimeterBuildMode = true;

					break;
				default:
					return;
				}
			}
		);

		menuItemMissionBuild.setOnAction( arg -> {
				if (modifyiedLayerMission == null) {
					//modifyiedLayerMission = (LayerMission) AppConfig.context.getBean("layerMission");
					// TALMA i am trying not to use bean here
					modifyiedLayerMission = new LayerMission("New Mission*", this);
					getOperationalViewTree().addLayer(modifyiedLayerMission);
					Mission msn = AppConfig.context.getBean(Mission.class);
					msn.setDrone(drone);
					modifyiedLayerMission.setMission(msn);
					
					super.startModifiedLayerMode(modifyiedLayerMission);
					
					isMissionBuildMode = true;
					missionBuilder.startMissionLayer(modifyiedLayerMission);
				}
			}
		);

		menuItemMissionAddWayPoint.setOnAction( arg -> missionBuilder.addWayPoint(getPosition(point)));

		menuItemMissionAddCircle.setOnAction( arg -> missionBuilder.addCircle(getPosition(point)));

		menuItemMissionSetLandPoint.setOnAction( arg -> missionBuilder.addLandPoint(getPosition(point)));
		
		menuItemMissionAddROI.setOnAction( arg -> missionBuilder.addROI(getPosition(point)));

		menuItemMissionSetRTL.setOnAction( arg -> missionBuilder.addRTL());

		menuItemMissionSetTakeOff.setOnAction( arg -> missionBuilder.addTakeOff());

		menuItemPerimeterAddPoint.setOnAction( arg -> modifyiedLayerPerimeter.add(getPosition(point)));

		return popup;
	}
	
	@Override
	protected void HandleMouseClick(MouseEvent me) {
		if (popup != null)
			popup.hide();
		
		if (!me.isPopupTrigger())
			return;
		
		Point point = new Point((int) me.getX(), (int) me.getY());
		popup = buildPopup(point);
		//this.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {if (e.getButton() == MouseButton.SECONDARY)   popup.show(this, e.getScreenX(), e.getScreenY()); else popup.hide();});
		popup.show(this, me.getScreenX(), me.getScreenY());
	}
	
	private void SetBearing(double deg) {
		if (!drone.getGps().isPositionValid())
			return;

		if (bearing != null && bearing.getBearing() == deg)
			return;

		if (bearing != null)
			removeMapLine(bearing);

		Coordinate start = drone.getGps().getPosition();
		Coordinate end = GeoTools.newCoordFromBearingAndDistance(start, deg /* + 180 */, 300);

		bearing = new MapVectorImpl(start, end, Color.RED);
		addMapLine(bearing);
	}

	private void removeBearing() {
		if (bearing != null) {
			removeMapLine(bearing);
			bearing = null;
		}
	}

	private MapMarkerCircle myMapCircle25 = null;
	private MapMarkerCircle myMapCircle50 = null;
	private MapMarkerCircle myMapCircle75 = null;
	private MapMarkerCircle myMapCircle100 = null;

	private void SetHome(Home home) {
		if (home == null || !home.isValid())
			return;

		if (myHome != null && myHome.getCoordinate().equals(home.getCoord()))
			return;

		if (myHome != null)
			removeMapMarker(myHome);

		if (myMapCircle25 != null)
			removeMapMarker(myMapCircle25);

		if (myMapCircle50 != null)
			removeMapMarker(myMapCircle50);

		if (myMapCircle75 != null)
			removeMapMarker(myMapCircle75);

		if (myMapCircle100 != null)
			removeMapMarker(myMapCircle100);

		myHome = new MapMarkerDot(Color.BLUE, home.getCoord());
		addMapMarker(myHome);

		myMapCircle25 = new MapMarkerCircle(myHome.getCoordinate(), 25);
		myMapCircle50 = new MapMarkerCircle(myHome.getCoordinate(), 50);
		myMapCircle75 = new MapMarkerCircle(myHome.getCoordinate(), 70);
		myMapCircle100 = new MapMarkerCircle(myHome.getCoordinate(), 100);
		addMapMarker(myMapCircle25);
		addMapMarker(myMapCircle50);
		addMapMarker(myMapCircle75);
		addMapMarker(myMapCircle100);

		loggerDisplayerSvc.logGeneral("Setting new Home position");
	}

	private void EditModeOff() {
		isMissionBuildMode = false;
		isPerimeterBuildMode = false;

		loggerDisplayerSvc.logGeneral("Edit mode is off");
	}

	private void EditModeOn() {
		loggerDisplayerSvc.logGeneral("Edit mode is on");
	}

	private static Coordinate perimeterBreachPoint = null;
	private static MapMarkerDot perimeterBreachPointMarker = null;

	private void SetPerimeterBreachPoint() {
		if (perimeterBreachPointMarker == null) {
			perimeterBreachPoint = drone.getPerimeter().getClosestPointOnPerimeterBorder();
			perimeterBreachPointMarker = new MapMarkerDot(perimeterBreachPoint.getLat(),perimeterBreachPoint.getLon());
			addMapMarker(perimeterBreachPointMarker);
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		Platform.runLater( () -> {
			switch (event) {
			case GPS:
				SetMyPosition(drone.getGps().getPosition());
				return;
			case HOME:
				SetHome(drone.getHome());
				return;
			case HEARTBEAT_TIMEOUT:
				loggerDisplayerSvc.logError("Heartbeat reached timeout");
				removeBearing();
			case DISCONNECTED:
				SetLastKnownPosition();
				return;
			case ORIENTATION:
				SetBearing(drone.getNavigation().getNavBearing());
				return;
			case LEFT_PERIMETER:
				SetPerimeterBreachPoint();
				return;
			case BEACON_BEEP:
				UpdateBeaconOnMap(drone.getBeacon().getPosition());
				return;
			case GCS_LOCATION:
				if (drone.getGCS().getPosition() == null) {
					loggerDisplayerSvc.logError("GCS location doesn't exist");
					return;
				}
				UpdateGCSOnMap(drone.getGCS().getPosition().dot(1));
				SetBearing(drone.getNavigation().getNavBearing());
				return;
			}
		});
	}

	private void UpdateGCSOnMap(Coordinate coord) {
		if (myGCS == null) {
			myGCS = new MapMarkerDot(coord.getLat(), coord.getLon());
			myGCS.setColor(Color.MAGENTA); 
		} 
		else if (myGCS.getCoordinate() == coord) {
			return;
		} 
		else {
			removeMapMarker(myGCS);
			myGCS = new MapMarkerDot(Color.MAGENTA, coord);
		}

		addMapMarker(myGCS);
		loggerDisplayerSvc.logGeneral("GCS was updated");
	}

	private void UpdateBeaconOnMap(Coordinate coord) {
		if (myBeacon == null) {
			myBeacon = new MapMarkerDot(coord.getLat(),coord.getLon());
			myBeacon.setColor(Color.MAGENTA); 
		}
		else if (myBeacon.getCoordinate() == coord) {
			return;
		}
		else {
			removeMapMarker(myBeacon);
			myBeacon = new MapMarkerDot(coord.getLat(),coord.getLon());
			myBeacon.setColor(Color.MAGENTA); 
		}

		addMapMarker(myBeacon);
		loggerDisplayerSvc.logGeneral("Beacon was updated");
	}
	
	@Override
	public HBox getMapButtomPane() {
		HBox hb = super.getMapButtomPane();
		
		cbLockMyPos = new CheckBox("Lock On My Position");
		cbLockMyPos.setSelected(true);
		cbLockMyPos.setOnAction(this);
		hb.getChildren().add(cbLockMyPos);
		
		cbFollowTrail = new CheckBox("Paint Trail");
		cbFollowTrail.setSelected(true);
		cbFollowTrail.setOnAction(this);
		hb.getChildren().add(cbFollowTrail);
		
		return hb;
	}

	private void SetLastKnownPosition() {
		if (myPos == null || myPos.getColor() == Color.RED)
			return;

		MapMarkerDot tmp = new MapMarkerDot( Color.RED, myPos.getCoordinate());
		removeMapMarker(myPos);
		myPos = tmp;
		addMapMarker(myPos);
	}

	private void SetMyPositionMarker(Coordinate coord) {
		Coordinate c = new Coordinate(coord.getLat(), coord.getLon());
		if (myPos != null && myPos.getCoordinate().getLat() == c.getLat()
				&& myPos.getCoordinate().getLon() == c.getLon()) {
			// System.out.println(getClass().getName() + " Same position");
			return;
		}

		MapMarkerDot tmp = new MapMarkerDot(Color.GREENYELLOW, c);

		if (myPos != null) {
			removeMapMarker(myPos);
		} else {
			// During the first time we have a GPS lock
			setDisplayPosition(c, 17);
		}
		addMapMarker(tmp);
		myPos = tmp;

		if (lockMapOnMyPosition)
			setDisplayPosition(myPos.getCoordinate(), getZoom());
	}

	private void SetMyPositionTrail(Coordinate coord) {
		if (!paintTrail)
			return;

		// Handling trail
		if (myTrailPath == null)
			myTrailPath = new MapLineImpl();

		int trailSize = myTrailPath.getCoordinates().size();
		if (trailSize >= 1 && myTrailPath.getCoordinates().get(trailSize - 1) == coord)
			return;

		myTrailPath.addCoordinate(coord);

		if (trailSize >= 2) {
			removeMapLine(myTrailPath);
			addMapLine(myTrailPath);
		}
	}

	private void SetMyPosition(Coordinate coord) {
		if (coord == null) {
			System.err.println("No Positions");
			return;
		}

		SetMyPositionMarker(coord);
		SetMyPositionTrail(coord);
	}
	
	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(QuadGuiEvent command) {
		switch (command.getCommand()) {
		case EDITMODE_EXISTING_LAYER_START:
			EditModeOn();
			Layer layer = (Layer) command.getSource();
			if (layer instanceof LayerMission) {
				System.out.println("Working on Mission Layer");
				modifyiedLayerMission = (LayerMission) layer;
				modifyiedLayerMissionOriginal = new LayerMission(modifyiedLayerMission, this);
				super.startModifiedLayerMode(modifyiedLayerMission);
				isMissionBuildMode = true;
				missionBuilder.startMissionLayer(modifyiedLayerMission);
				modifyiedLayerMission.setName(modifyiedLayerMission.getName() + OperationalViewTree.EDIT_SUFFIX);
			} else if (layer instanceof LayerPolygonPerimeter) {
				System.out.println("Working on Perimeter Layer");
				modifyiedLayerPerimeter = (LayerPolygonPerimeter) layer;
				modifyiedLayerPerimeterOriginal = new LayerPolygonPerimeter((LayerPolygonPerimeter) modifyiedLayerPerimeter, this);
				isPerimeterBuildMode = true;
				modifyiedLayerPerimeter.setName(modifyiedLayerPerimeter.getName() + OperationalViewTree.EDIT_SUFFIX);
			} else {
				EditModeOff();
				return;
			}

			break;
		case MISSION_UPDATED_BY_TABLE:
			LayerMission layerMission = (LayerMission) command.getSource();
			layerMission.regenerateMapObjects();
			break;
		}
	}

	@Override
	public void LayerEditorCancel() {
		// Missions
		if (modifyiedLayerMission != null) {
			getOperationalViewTree().removeLayer(modifyiedLayerMission);
			modifyiedLayerMission = null;
		}
		if (modifyiedLayerMissionOriginal != null) {
			getOperationalViewTree().addLayer(modifyiedLayerMissionOriginal);
			modifyiedLayerMissionOriginal = null;
		}

		// Perimeters
		if (modifyiedLayerPerimeter != null) {
			getOperationalViewTree().removeLayer(modifyiedLayerPerimeter);
			modifyiedLayerPerimeter = null;
		}
		if (modifyiedLayerPerimeterOriginal != null) {
			modifyiedLayerPerimeterOriginal.setName(modifyiedLayerPerimeterOriginal.getName().substring(0,modifyiedLayerPerimeterOriginal.getName().length()));
			getOperationalViewTree().addLayer(modifyiedLayerPerimeterOriginal);
			modifyiedLayerPerimeterOriginal = null;
		}

		//missionBox.clear();
		super.finishModifiedLayerMode();
		if (isMissionBuildMode)
			missionBuilder.stopBuildMission();
		
		isMissionBuildMode = false;
		isPerimeterBuildMode = false;
	}

	@Override
	public void LayerEditorSave() {
		if (!runtimeValidator.validate(getOperationalViewTree())) {
			throw new RuntimeException("Failed to save layer, validatio failed");
		}
		
		if (modifyiedLayerMission != null)
			modifyiedLayerMission.setName(modifyiedLayerMission.getName().substring(0,modifyiedLayerMission.getName().length() - 1));

		if (modifyiedLayerPerimeter != null)
			modifyiedLayerPerimeter.setName(modifyiedLayerPerimeter.getName().substring(0,modifyiedLayerPerimeter.getName().length() - 1));

		modifyiedLayerMissionOriginal = null;
		modifyiedLayerMission = null;

		modifyiedLayerPerimeterOriginal = null;
		modifyiedLayerPerimeter = null;
		
		super.finishModifiedLayerMode();
		if (isMissionBuildMode)
			missionBuilder.stopBuildMission();
		
		isMissionBuildMode = false;
		isPerimeterBuildMode = false;

		getOperationalViewTree().refresh();
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource().equals(cbLockMyPos)) {
			if (cbLockMyPos.isSelected()) {
				loggerDisplayerSvc.logGeneral("Lock on my position");
				lockMapOnMyPosition = true;
			} else {
				loggerDisplayerSvc.logGeneral("Release lock on my position");
				lockMapOnMyPosition = false;
			}
			return;
		}
		
		if (event.getSource().equals(cbFollowTrail)) {
			if (cbFollowTrail.isSelected()) {
				loggerDisplayerSvc.logGeneral("Paint My Trail");
				myTrailPath = null;
				paintTrail = true;
			} else {
				loggerDisplayerSvc.logGeneral("Stop Paint My Trail");
				paintTrail = false;
				removeMapLine(myTrailPath);
				myTrailPath = null;
			}
			return;
		}
	}
}
