package com.viewer_console;

import com.gui.core.mapTree.LayeredViewTree;
import com.gui.core.mapViewer.LayeredViewMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class MapView extends Pane implements ChangeListener<Number>, Initializable {

	private LayeredViewTree tree;
	private LayeredViewMap map;
	
	@FXML private SplitPane splitPane;
	@FXML private Pane left;
	@FXML private Pane right;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
	}

	@Autowired
	public void setTree(LayeredViewTree tree) {
		this.tree = tree;
	}

	@Autowired
	public void setMap(LayeredViewMap map) {
		this.map = map;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		left.getChildren().add(tree);
		right.getChildren().add(map);
		if (splitPane.getDividers().size() == 1)
			splitPane.getDividers().get(0).positionProperty().addListener(this);
	}

	@Override
	public void changed(ObservableValue<? extends Number> property, Number fromPrecentage, Number toPrecentage) {
		map.setMapBounds(0, 0, (int) (splitPane.getWidth() - splitPane.getWidth() * toPrecentage.doubleValue()), (int) splitPane.getHeight());
		tree.setTreeBound(0, 0, (int) (splitPane.getWidth() * toPrecentage.doubleValue()), (int) splitPane.getHeight());
	}
}
