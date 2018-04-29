package com.dronegcs.console.controllers.internalFrames;

import com.dronegcs.console.controllers.GUISettings;
import com.dronegcs.console.controllers.dashboard.DraggableNode;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.gui.core.mapTree.CheckBoxViewTree;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class InternalFrameMap extends Pane implements Initializable {//, ChangeListener<Number> {
	
	@NotNull(message = "Internal Error: Missing operationalViewTree")
	private OperationalViewTree operationalViewTree;

	@Autowired
	private DraggableNode draggableNode;

	@Autowired
	public void setOperationalViewTree(OperationalViewTree operationalViewTree) {
		this.operationalViewTree = operationalViewTree;
	}

	@Autowired
	public void setOperationalViewMap(OperationalViewMap operationalViewMap) {
		this.operationalViewMap = operationalViewMap;
	}

	@NotNull(message = "Internal Error: Missing operationalViewMap")
	private OperationalViewMap operationalViewMap;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	@NotNull @FXML private Pane root;
//	@NotNull @FXML private SplitPane splitPane;
	@NotNull @FXML private Pane splitPane;
	@NotNull @FXML private Pane left;
	@NotNull @FXML private Pane right;

	private static final double TREEVIEW_RATIO_H = 0.5;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		splitPane.setPrefWidth(root.getPrefWidth());
		splitPane.setPrefHeight(root.getPrefHeight());

		CheckBoxViewTree tree = operationalViewTree.getTree();
		operationalViewMap.setMapBounds(0, 0, (int) splitPane.getPrefWidth(), (int) splitPane.getPrefHeight());


		left.getChildren().add(tree);
		right.getChildren().add(operationalViewMap);

		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		operationalViewMap.setShowZoomControls(false);
		operationalViewMap.setShowToolbars(false);
		GUISettings._HEIGHT.addListener(val -> {
			newScreenSize(((IntegerProperty) val).getValue());
			tree.setPrefHeight(left.getPrefHeight());
		});
		newScreenSize(GUISettings._HEIGHT.get());
		tree.setPrefHeight(left.getPrefHeight());
	}

	private void newScreenSize(int intVal) {
		double newTreeHeight = intVal * TREEVIEW_RATIO_H;
		left.setPrefHeight(newTreeHeight);
		double padY = intVal * (1 - TREEVIEW_RATIO_H) / 2;
		Insets currentInsets = StackPane.getMargin(left);
		Insets insets = new Insets(padY, currentInsets.getRight(), padY, currentInsets.getLeft());
		StackPane.setMargin(left, insets);
	}

//	@Override
//	public void changed(ObservableValue<? extends Number> property, Number fromPercentage, Number toPrecentage) {
//		operationalViewMap.setMapBounds(0, 0, (int) (splitPane.getPrefWidth() - splitPane.getPrefWidth() * toPrecentage.doubleValue()), (int) splitPane.getPrefHeight());
//		operationalViewTree.getTree().setTreeBound(0, 0, (int) (splitPane.getPrefWidth() * toPrecentage.doubleValue()), (int) splitPane.getPrefHeight());
//	}

	public void reloadData() {
		operationalViewTree.reloadData();
	}
}