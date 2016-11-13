package gui.core.internalFrames;

import gui.core.mapTree.OperationalViewTree;
import gui.core.mapViewer.OperationalViewMap;
import gui.core.springConfig.AppConfig;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import tools.validations.RuntimeValidator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@ComponentScan("tools.validations")
@ComponentScan("gui.core.mapViewer")
@ComponentScan("gui.core.mapTree")
@ComponentScan("gui.is.services")
@Component("internalFrameMap")
public class InternalFrameMap extends Pane {

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
		
		BorderPane pane = new BorderPane();
		getChildren().add(pane);

		pane.setLeft(tree);
		setPrefSize(map.getWidth(), map.getHeight());
		pane.setCenter(map);

		Pane panelTop = new Pane();
		Pane panelBottom = new Pane();
		pane.setTop(panelTop);
		pane.setBottom(panelBottom);		
	}
	
	public void refreshGui() {
		// TODO: Fix this hack, need to calculate the width properly
		tree.setPrefWidth(Screen.getPrimary().getBounds().getWidth() * AppConfig.FRAME_CONTAINER_REDUCE_PRECENTAGE);
		map.setMapBounds( 0, 0, (int) (getPrefWidth() - (Screen.getPrimary().getBounds().getWidth() * AppConfig.FRAME_CONTAINER_REDUCE_PRECENTAGE)), (int) getPrefHeight());
		
	}
}