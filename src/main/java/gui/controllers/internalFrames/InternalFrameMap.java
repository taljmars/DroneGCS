package gui.controllers.internalFrames;

import gui.controllers.internalFrames.internal.OperationalViewTree;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import is.validations.RuntimeValidator;

import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import gui.controllers.internalFrames.internal.OperationalViewMap;

@ComponentScan("controllers.internalFrames.internal")
@ComponentScan("validations")
@ComponentScan("gui.services")
@Component
public class InternalFrameMap extends Pane implements ChangeListener<Number>, Initializable {
	
	@Autowired @NotNull(message = "Internal Error: Missing tree view")
	private OperationalViewTree tree;
	
	@Autowired @NotNull(message = "Internal Error: Missing map view")
	private OperationalViewMap map;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	@NotNull @FXML private Pane root;
	@NotNull @FXML private SplitPane splitPane;
	@NotNull @FXML private Pane left;
	@NotNull @FXML private Pane right;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		splitPane.setPrefWidth(root.getPrefWidth());
		splitPane.setPrefHeight(root.getPrefHeight());
		left.getChildren().add(tree);
		right.getChildren().add(map);
		if (splitPane.getDividers().size() == 1)
			splitPane.getDividers().get(0).positionProperty().addListener(this);
		
		if (!runtimeValidator.validate(this))
			throw new RuntimeException("Value weren't initialized");
	}
	
	@Override
	public void changed(ObservableValue<? extends Number> property, Number fromPrecentage, Number toPrecentage) {
		map.setMapBounds(0, 0, (int) (splitPane.getPrefWidth() - splitPane.getPrefWidth() * toPrecentage.doubleValue()), (int) splitPane.getPrefHeight());
		tree.setTreeBound(0, 0, (int) (splitPane.getPrefWidth() * toPrecentage.doubleValue()), (int) splitPane.getPrefHeight());
	}	
}