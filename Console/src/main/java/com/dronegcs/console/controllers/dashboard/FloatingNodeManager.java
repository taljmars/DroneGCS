package com.dronegcs.console.controllers.dashboard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Component;

@Component
public class FloatingNodeManager {

    private BooleanProperty dragModeActiveProperty = new SimpleBooleanProperty(this, "dragModeActivated", false);

    protected Pane makeDraggable(Pane parent, final Node node, double width, double height) {
        final DragContext dragContext = new DragContext();
        final Group wrapGroup = new Group(node);
        final Pane pane = new Pane();
        StackPane.setMargin(pane, new Insets(0));

        //pane.setStyle("-fx-border-color: yellow;");
        pane.setMaxSize(width, height);
        pane.setPrefSize(width, height);

        wrapGroup.addEventFilter(MouseEvent.ANY, event ->  {
                if (dragModeActiveProperty.get()) {
                    // disable mouse events for all children
                    event.consume();
                }
        });

        wrapGroup.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
                        if (dragModeActiveProperty.get()) {
                            if (mouseEvent.isPopupTrigger()) {
                                parent.getChildren().remove(pane);
                            }
                        }
                });

        wrapGroup.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
                        if (dragModeActiveProperty.get()) {
                            // remember initial mouse cursor coordinates
                            // and node position
                            dragContext.mouseAnchorX = mouseEvent.getX();
                            dragContext.mouseAnchorY = mouseEvent.getY();
                        }
                });

        wrapGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
                        if (dragModeActiveProperty.get()) {
                            // shift node from its initial position by delta
                            // calculated from mouse cursor movement
                            Insets currentInsets = StackPane.getMargin(pane);
                            double newTop = currentInsets.getTop() + (mouseEvent.getY() - dragContext.mouseAnchorY);
                            double newLeft = currentInsets.getLeft() + (mouseEvent.getX() - dragContext.mouseAnchorX);
                            Insets aa = new Insets(
                                    newTop,
                                    0,
                                    0,
                                    newLeft
                            );
                            StackPane.setMargin(pane, aa);
                        }
                });

        pane.getChildren().add(wrapGroup);
        return pane;
    }

    public boolean isEditing() {
        return dragModeActiveProperty != null && dragModeActiveProperty.get();
    }

    public void bind(BooleanProperty booleanProperty) {
        dragModeActiveProperty.bind(booleanProperty);
    }

    private static final class DragContext {
        public double mouseAnchorX;
        public double mouseAnchorY;
    }
}
