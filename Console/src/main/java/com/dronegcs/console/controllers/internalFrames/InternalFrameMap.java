package com.dronegcs.console.controllers.internalFrames;

import com.dronegcs.console.controllers.GUISettings;
import com.dronegcs.console.controllers.dashboard.FloatingNodeManager;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console_plugin.ActiveUserProfile;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.mapviewer.os_utilities.Environment;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

import static com.dronegcs.console.controllers.dashboard.Dashboard.DisplayMode.DisplayMode;
import static com.dronegcs.console.controllers.dashboard.Dashboard.DisplayMode.HUD_MODE;


@Component
public class InternalFrameMap extends Pane implements Initializable {//, ChangeListener<Number> {
	
	@NotNull(message = "Internal Error: Missing operationalViewTree")
	private OperationalViewTree operationalViewTree;

	@Autowired
	private FloatingNodeManager draggableNode;

	@Autowired
	private ApplicationContext applicationContext;

//	@Autowired
	private ActiveUserProfile activeUserProfile;

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
		Assert.isTrue(++called == 1, "Not a Singleton");
		String maptilePath = System.getProperty("user.home");
		if (maptilePath != null && !maptilePath.isEmpty()) {
			maptilePath += "/DroneGCS/";
			System.out.println("Tile cache path sets to " + maptilePath);
			Environment.setBasicWorkingDirectory(maptilePath);
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
//		splitPane.setPrefWidth(root.getPrefWidth());
//		splitPane.setPrefHeight(root.getPrefHeight());

		TreeView tree = operationalViewTree.getTree();
		operationalViewMap.setMapBounds(0, 0, (int) splitPane.getPrefWidth(), (int) splitPane.getPrefHeight());

		activeUserProfile = applicationContext.getBean(ActiveUserProfile.class);
		if (activeUserProfile.getDefinition(String.valueOf(DisplayMode)) == null || !activeUserProfile.getDefinition(String.valueOf(DisplayMode)).equals(HUD_MODE.name())) {
			left.getChildren().add(tree);
		}
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