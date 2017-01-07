package controllers.internalFrames;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.SplitPane;
import javafx.scene.input.DragEvent;
import javafx.stage.Screen;
import springConfig.AppConfig;
import validations.RuntimeValidator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import controllers.internalFrames.internal.OperationalViewMap;
import controllers.internalFrames.internal.OperationalViewTree;

@ComponentScan("controllers.internalFrames.internal")
@ComponentScan("validations")
@ComponentScan("gui.services")
@Component("internalFrameMap")
public class InternalFrameMap extends SplitPane implements ChangeListener<Number> {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "tree")
	@NotNull(message = "Internal Error: Missing tree view")
	private OperationalViewTree tree;
	
	@Resource(name = "map")
	@NotNull(message = "Internal Error: Missing map view")
	private OperationalViewMap map;
	
	@Resource(name = "validator")
	@NotNull(message = "Internal Error: Failed to get validator")
	private RuntimeValidator validator;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		if (!validator.validate(tree))
			throw new RuntimeException("Failed to validate tree view");
	
		getItems().addAll(tree, map);
		
		tree.widthProperty().addListener(this);
		tree.setMaxWidth(Screen.getPrimary().getBounds().getWidth() * AppConfig.FRAME_CONTAINER_REDUCE_PRECENTAGE);
		tree.setPrefWidth(Screen.getPrimary().getBounds().getWidth() * AppConfig.FRAME_CONTAINER_REDUCE_PRECENTAGE);
		
		setOnDragDropped(myDragEvent);
	}
	
	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		Platform.runLater(() -> map.setMapBounds(0, 0,(int) ( getPrefWidth() - newValue.intValue()), (int) getPrefHeight()));
	}
	
	EventHandler<DragEvent> myDragEvent = new EventHandler<DragEvent>() {

		@Override
		public void handle(DragEvent event) {
			System.err.println(event.toString());
		}
	};
	
	
}