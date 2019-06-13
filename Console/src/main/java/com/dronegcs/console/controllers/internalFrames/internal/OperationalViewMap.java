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
package com.dronegcs.console.controllers.internalFrames.internal;

import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console.controllers.internalFrames.internal.Editors.DrawingEditorHelper;
import com.dronegcs.console.controllers.internalFrames.internal.Editors.MissionEditorHelper;
import com.dronegcs.console.controllers.internalFrames.internal.Editors.PerimeterEditorHelper;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.*;
import com.dronegcs.console.flightControllers.KeyBoardController;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnDroneListener;
import com.dronegcs.mavlink.is.drone.variables.GuidedPoint;
import com.dronegcs.mavlink.is.drone.variables.Home;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;
import com.gui.core.layers.AbstractLayer;
import com.gui.core.mapViewer.internal.LayeredViewMapImpl;
import com.gui.core.mapViewerObjects.MapLineImpl;
import com.gui.core.mapViewerObjects.MapMarkerCircle;
import com.gui.core.mapViewerObjects.MapMarkerDot;
import com.gui.core.mapViewerObjects.MapVectorImpl;
import com.gui.is.interfaces.mapObjects.MapLine;
import com.gui.is.interfaces.mapObjects.MapMarker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.awt.*;

/**
 *
 * @author taljmars
 */

@Component
public class OperationalViewMap extends LayeredViewMapImpl implements
OnDroneListener, EventHandler<ActionEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(OperationalViewMap.class);
    
    @Autowired @NotNull( message = "Internal Error: Failed to get drone" )
    private Drone drone;

    @Autowired @NotNull( message = "Internal Error: Failed to get keyboard controller" )
    private KeyBoardController keyboardController;

    @Autowired @NotNull( message = "Internal Error: Failed to get com.generic_tools.logger displayer" )
    private LoggerDisplayerSvc loggerDisplayerSvc;
    
    @Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
    private DialogManagerSvc dialogManagerSvc;

    @Autowired @NotNull( message = "Internal Error: Failed to get event publisher service" )
    protected ApplicationEventPublisher applicationEventPublisher;

    @Autowired @NotNull(message = "Internal Error: Failed to get application context")
    public ApplicationContext applicationContext;

//    //@Resource(type = OperationalViewTreeImpl.class)
//    @Autowired
//    public void setOperationalViewTree(OperationalViewTree operationalViewTree) {
//        super.setCheckBoxViewTree(operationalViewTree.getTree());
//    }

    @Autowired
    private RuntimeValidator runtimeValidator;

    // Editors

    @Autowired @NotNull(message = "Internal Error: Failed to get missions editor helper")
    private MissionEditorHelper missionEditorMode;

    @Autowired @NotNull(message = "Internal Error: Failed to get perimeter editor helper")
    private PerimeterEditorHelper perimeterEditorMode;

    @Autowired @NotNull(message = "Internal Error: Failed to get drawing editor helper")
    private DrawingEditorHelper drawingEditorMode;


    // Fields

    private boolean lockMapOnMyPosition = true;
    private boolean paintTrail = true;
    private MapLineImpl myTrailPath = null;
    
    private MapMarkerDot guidedPoint = null;
    private MapMarkerDot myPos = null;
    private MapLine bearing = null;
    private MapMarkerDot myHome = null;
    private MapMarkerDot myBeacon = null;
    private MapMarkerDot myGCS = null;
    
    private CheckBox cbLockMyPos;
    private CheckBox cbFollowTrail;
    
    private ContextMenu popup;

    private static int called;

    @PostConstruct
    protected void init() {
        Assert.isTrue(++called == 1, "Not a Singleton");
        
        setDisplayPosition(new Coordinate(32.0684, 34.8248), 10);
        drone.addDroneListener(this);

        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());
    }
    
    private ContextMenu buildPopup(Point point) {
        LOGGER.debug("Building popup");
        ContextMenu popup = new ContextMenu();

        boolean isBuildingMode = drawingEditorMode.isBuildMode() || missionEditorMode.isBuildMode() || perimeterEditorMode.isBuildMode();

        popup.getItems().addAll(drawingEditorMode.buildMapViewPopup(this, point).getItems());
        popup.getItems().addAll(missionEditorMode.buildMapViewPopup(this, point).getItems());
        popup.getItems().addAll(perimeterEditorMode.buildMapViewPopup(this, point).getItems());

        MenuItem menuItemFlyTo = new MenuItem("Fly to Position");
        MenuItem menuItemDist = new MenuItem("Distance -m");

        menuItemFlyTo.setDisable(!drone.getGps().isPositionValid());
        menuItemDist.setDisable(!drone.getGps().isPositionValid());

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

                    guidedPoint = new MapMarkerDot("G", coord.getLat(), coord.getLon());
                    addMapMarker(guidedPoint);
                    loggerDisplayerSvc.logGeneral("Flying to guided point " + guidedPoint.getCoordinate().toString());
                } catch (Exception e1) {
                    dialogManagerSvc.showErrorMessageDialog("Failed to handle FlyTo request", e1);
                }
            }
        );

        return popup;
    }
    
    @Override
    protected void HandleMouseClick(MouseEvent me) {
        // TODO: remove point
        LOGGER.debug("Mouse click " + me);
        if (popup != null)
            popup.hide();
        
        if (!me.getButton().equals(MouseButton.SECONDARY))
            return;

        LOGGER.debug("Get point");
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

        myHome = new MapMarkerDot("H", Color.BLUE, home.getCoord());
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

    @Override
    protected void EditModeOff() {
        super.EditModeOff();
        missionEditorMode.setBuildMode(false);
        perimeterEditorMode.setBuildMode(false);
        drawingEditorMode.setBuildMode(false);

        loggerDisplayerSvc.logGeneral("Edit mode is off");
    }

    @Override
    protected void EditModeOn() {
        super.EditModeOn();
        loggerDisplayerSvc.logGeneral("Edit mode is on");
    }

    private static Coordinate perimeterBreachPoint = null;
    private static MapMarkerDot perimeterBreachPointMarker = null;

    public void setPerimeterBreachPoint() {
        if (perimeterBreachPointMarker == null) {
            perimeterBreachPoint = drone.getPerimeter().getClosestPointOnPerimeterBorder();
            perimeterBreachPointMarker = new MapMarkerDot("X", perimeterBreachPoint.getLat(),perimeterBreachPoint.getLon());
            addMapMarker(perimeterBreachPointMarker);
        }
    }

    public void unsetPerimeterBreachPoint() {
        if (perimeterBreachPointMarker != null) {
            removeMapMarker(perimeterBreachPointMarker);
            perimeterBreachPointMarker = null;
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
                setPerimeterBreachPoint();
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
            }
        });
    }

    private void UpdateGCSOnMap(Coordinate coord) {
        if (myGCS == null) {
            myGCS = new MapMarkerDot("H", coord.getLat(), coord.getLon());
            myGCS.setColor(Color.MAGENTA); 
        } 
        else if (myGCS.getCoordinate() == coord) {
            return;
        } 
        else {
            removeMapMarker(myGCS);
            myGCS = new MapMarkerDot("GCS", Color.MAGENTA, coord);
        }

        addMapMarker(myGCS);
        loggerDisplayerSvc.logGeneral("GCS was updated");
    }

    private void UpdateBeaconOnMap(Coordinate coord) {
        if (myBeacon == null) {
            myBeacon = new MapMarkerDot("X", coord.getLat(),coord.getLon());
            myBeacon.setColor(Color.MAGENTA); 
        }
        else if (myBeacon.getCoordinate() == coord) {
            return;
        }
        else {
            removeMapMarker(myBeacon);
            myBeacon = new MapMarkerDot("X", coord.getLat(),coord.getLon());
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

        Image img = new Image(this.getClass().getResource("/com/dronegcs/console/guiImages/droneDisconnected.png").toString());
        ImageView iview = new ImageView(img);
        iview.setFitHeight(50);
        iview.setFitWidth(50);

        //MapMarkerDot tmp = new MapMarkerDot( "", Color.RED, myPos.getCoordinate());
        MapMarkerDot tmp = new MapMarkerDot(iview, drone.getNavigation().getNavBearing(), myPos.getCoordinate());
        removeMapMarker(myPos);
        myPos = tmp;
        addMapMarker(myPos);
    }

    private void SetMyPositionMarker(Coordinate coord) {
        Coordinate c = new Coordinate(coord.getLat(), coord.getLon());
        if (myPos != null && myPos.getCoordinate().getLat() == c.getLat()
                && myPos.getCoordinate().getLon() == c.getLon()) {
            return;
        }

        Image img = new Image(this.getClass().getResource("/com/dronegcs/console/guiImages/droneConnected.png").toString());
        ImageView iview = new ImageView(img);
        iview.setFitHeight(50);
        iview.setFitWidth(50);

//        MapMarkerDot tmp = new MapMarkerDot("", Color.GREENYELLOW, c);
        MapMarkerDot tmp = new MapMarkerDot(iview, drone.getNavigation().getNavBearing(), c);

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

    @Override
    public void setEditedLayer(AbstractLayer layer) {
        super.setEditedLayer(layer);
    }

    @Override
    public void layerEditorDone() {
        super.layerEditorDone();

        if (perimeterEditorMode.isBuildMode())
            perimeterEditorMode.saveEditor();

        if (missionEditorMode.isBuildMode())
            missionEditorMode.saveEditor();

        if (drawingEditorMode.isBuildMode())
            drawingEditorMode.saveEditor();

        applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.EDITMODE_EXISTING_LAYER_FINISH));
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

    @SuppressWarnings("incomplete-switch")
    @EventListener
    public void onApplicationEvent(QuadGuiEvent command) {
        switch (command.getCommand()) {
            case EDITMODE_EXISTING_LAYER_START:
//                EditModeOn();
                AbstractLayer layer = (AbstractLayer) command.getSource();
                if (layer instanceof LayerMission) {
                    LOGGER.debug("Working on DroneMission Layer");
                    LayerMission modifiedLayerMissionOriginal = missionEditorMode.startEditing((LayerMission) layer);
                    setEditedLayer(modifiedLayerMissionOriginal);
                    applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_EDITING_STARTED, modifiedLayerMissionOriginal));
                }
                else if (layer instanceof LayerPolygonPerimeter || layer instanceof LayerCircledPerimeter) {
                    LOGGER.debug("Working on Perimeter Layer");
                    LayerPerimeter modifiedLayerPerimeterOriginal = perimeterEditorMode.startEditing((LayerPerimeter) layer);
                    setEditedLayer(modifiedLayerPerimeterOriginal);
                    applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PERIMETER_EDITING_STARTED, modifiedLayerPerimeterOriginal));
                }
                else if (layer instanceof LayerDraw) {
                    LOGGER.debug("Working on Drawing Layer");
                    setEditedLayer(layer);
                    applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.DRAW_EDITING_STARTED, layer));
                }
                else {
                    LOGGER.debug("Unrecognized Layer");
                    EditModeOff();
                    return;
                }

                break;
//            case EDITMODE_EXISTING_LAYER_CANCELED:
//                EditedLayer editedLayer = (EditedLayer) command.getSource();
//                editedLayer.stopEditing();
            case EDITMODE_EXISTING_LAYER_FINISH:
                EditModeOff();
                break;
            case MISSION_UPDATED_BY_TABLE:
                LayerMission layerMission = (LayerMission) command.getSource();
                layerMission.regenerateMapObjects();
                break;
            case PERIMETER_UPDATED_BY_TABLE:
                LayerPerimeter layerPerimeter = (LayerPerimeter) command.getSource();
                layerPerimeter.regenerateMapObjects();
                break;
        }
    }
}
