package com.dronegcs.console.controllers.internalPanels.internal;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerCircledPerimeter;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerPerimeter;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerPolygonPerimeter;
import com.dronegcs.console.controllers.internalPanels.PanelTableBox;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.dronegcs.console_plugin.perimeter_editor.PolygonPerimeterEditor;
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
public class PerimeterTableProfile extends TableProfile {

    private LayerPerimeter layerPerimeter;

    @Autowired
    private EventPublisherSvc eventPublisherSvc;

    @Autowired @NotNull(message = "Internal Error: Failed to get perimeter manager")
    private PerimetersManager perimetersManager;

    @Autowired @NotNull(message = "Internal Error: Failed to get logger")
    private Logger logger;

    @Autowired @NotNull(message = "Internal Error: Failed to get validator")
    private RuntimeValidator runtimeValidator;

    //Validation should be executed during load function
    @NotNull(message = "Internal Error: Table wasn't initialized")
    private PanelTableBox panelTableBox;

    public PerimeterTableProfile() {}

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
                return new EditingCell<>(new DoubleStringConverter());
            }
        };

        panelTableBox.getOrder().setCellValueFactory(new PropertyValueFactory<>("order"));
        panelTableBox.getType().setCellValueFactory(new PropertyValueFactory<>("type"));
        panelTableBox.getLat().setCellValueFactory(new PropertyValueFactory<>("lat"));
        panelTableBox.getLon().setCellValueFactory(new PropertyValueFactory<>("lon"));
        panelTableBox.getRadius().setCellValueFactory(new PropertyValueFactory<>("radius"));
        panelTableBox.getRadius().setCellFactory(cellFactory);
        panelTableBox.getRadius().setOnEditCommit(t -> {
            TableItemEntry entry = t.getTableView().getItems().get(t.getTablePosition().getRow());
            CirclePerimeter perimeter = (CirclePerimeter) entry.getReferedItem();
            perimeter.setRadius(t.getNewValue());
            generateTable(true, this.layerPerimeter);
            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PERIMETER_UPDATED_BY_TABLE, layerPerimeter));
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
                            PolygonPerimeter polygonPerimeter = ((LayerPolygonPerimeter) layerPerimeter).getPolygonPerimeter();
                            polygonPerimeter.getPoints().remove(getIndex());
                            polygonPerimeter.getPoints().add(getIndex() - 1, ((MissionItem) entry.getReferedItem()).getKeyId().getObjId());
                            generateTable(true, layerPerimeter);
                            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerPerimeter));
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
                            PolygonPerimeter polygonPerimeter = ((LayerPolygonPerimeter) layerPerimeter).getPolygonPerimeter();
                            polygonPerimeter.getPoints().remove(getIndex());
                            polygonPerimeter.getPoints().add(getIndex() + 1, ((MissionItem)entry.getReferedItem()).getKeyId().getObjId());
                            generateTable(true, layerPerimeter);
                            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_TABLE, layerPerimeter));
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
                                PolygonPerimeterEditor polygonPerimeterEditor = perimetersManager.getPerimeterEditor(((LayerPolygonPerimeter) layerPerimeter).getPolygonPerimeter());
                                polygonPerimeterEditor.removePoint((Point) entry.getReferedItem());
                                generateTable(true, layerPerimeter);
                                eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PERIMETER_UPDATED_BY_TABLE, layerPerimeter));
                            }
                            catch (PerimeterUpdateException e) {
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
    public void generateTable(boolean editMode, Object contentPayload) {

        setLayerPerimeter((LayerPerimeter) contentPayload);

        logger.LogErrorMessege("Receiverd " + contentPayload);

        TableItemEntry entry = null;

        if (panelTableBox.getTable() == null) {
            throw new RuntimeException("Failed to get Table");
        }

        if (layerPerimeter == null) {
            panelTableBox.getTable().setItems(null);
            return;
        }

        Perimeter perimeter = layerPerimeter.getPerimeter();
        if (perimeter == null) {
            panelTableBox.getTable().setItems(null);
            return;
        }

        // Start loading items
        ObservableList<TableItemEntry> data = FXCollections.observableArrayList();

        // Setting columns
        panelTableBox.getTable().getColumns().removeAll(panelTableBox.getTable().getColumns());
        if (layerPerimeter instanceof LayerPolygonPerimeter) {
            if (!editMode)
                panelTableBox.getTable().getColumns().addAll(
                        panelTableBox.getOrder(), panelTableBox.getType(),
                        panelTableBox.getLat(), panelTableBox.getLon());
            else
                panelTableBox.getTable().getColumns().addAll(
                        panelTableBox.getOrder(), panelTableBox.getType(),
                        panelTableBox.getLat(), panelTableBox.getLon(),
                        panelTableBox.getUp(), panelTableBox.getDown(), panelTableBox.getRemove());

            List<Point> itemList = perimetersManager.getPoints(((LayerPolygonPerimeter) layerPerimeter).getPolygonPerimeter());

            Iterator<Point> it = itemList.iterator();

            int i = 0;
            while (it.hasNext()) {
                Point point = (Point) it.next();
                entry = new TableItemEntry(i, Point.class.getSimpleName(), point.getLat(), point.getLon(), 0.0, 0.0, 0.0, point);

                i++;
                data.add(entry);
            }
        }
        else if (layerPerimeter instanceof LayerCircledPerimeter) {
                panelTableBox.getTable().getColumns().addAll(
                        panelTableBox.getOrder(), panelTableBox.getType(),
                        panelTableBox.getLat(), panelTableBox.getLon(), panelTableBox.getRadius());

            CirclePerimeter circlePerimeter = ((LayerCircledPerimeter) layerPerimeter).getCirclePerimeter();
            List<Point> points = perimetersManager.getPoints(circlePerimeter);
            if (points == null || points.isEmpty()) {
                //TODO: add logger messege
                return;
            }
            entry = new TableItemEntry(0, CirclePerimeter.class.getSimpleName(), points.get(0).getLat(), points.get(0).getLon(), 0.0, 0.0, circlePerimeter.getRadius(), circlePerimeter);
            data.add(entry);
        }

        panelTableBox.getTable().setItems(data);
        panelTableBox.getTable().setEditable(editMode);
    }

    private void setLayerPerimeter(LayerPerimeter layerPerimeter) {
        this.layerPerimeter = layerPerimeter;
    }

    private void unsetLayerPolygonPerimeter() {
        this.layerPerimeter = null;
    }

    @SuppressWarnings("incomplete-switch")
    @EventListener
    public void onApplicationEvent(QuadGuiEvent command) {
        Platform.runLater( () -> {
            switch (command.getCommand()) {
                case PERIMETER_EDITING_STARTED:
                case PERIMETER_UPDATED_BY_MAP:
                    load();
                    generateTable(true, command.getSource());
                    break;
                case PUBLISH:
                case DISCARD:
                    if (layerPerimeter == null)
                        break;
                case PERIMETER_EDITING_FINISHED:
                    load();
                    generateTable(false, null);
                    break;
                case PERIMETER_VIEW_ONLY_STARTED:
                    load();
                    generateTable(false, command.getSource());
                    break;
                case PERIMETER_VIEW_ONLY_FINISHED:
                    load();
                    generateTable(false, null);
                    break;
            }
        });
    }

}
