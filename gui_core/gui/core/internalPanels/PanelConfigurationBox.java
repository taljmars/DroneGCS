package gui.core.internalPanels;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import mavlink.is.drone.Drone;

@ComponentScan("tools.comm.internal")
@ComponentScan("mavlink.core.drone")
@Component("areaConfiguration")
public class PanelConfigurationBox extends Pane {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 8111843990697031411L;
	
	@Resource(name = "drone")
	private Drone drone;
	
	private CheckBox cbActiveGeofencePerimeterAlertOnly;
	
	private static int called = 0;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		GridPane panel = new GridPane();
		panel.setAlignment(Pos.CENTER);
		panel.setVgap(10);
		panel.setHgap(10);
		
        cbActiveGeofencePerimeterAlertOnly = new CheckBox("Active GeoFence/Perimeter Alert Only");
        cbActiveGeofencePerimeterAlertOnly.setOnAction( e -> drone.getPerimeter().setAlertOnly(cbActiveGeofencePerimeterAlertOnly.isSelected() ? true : false));
        cbActiveGeofencePerimeterAlertOnly.setSelected(false);
        panel.getChildren().add(cbActiveGeofencePerimeterAlertOnly);
        
        getChildren().add(panel);
	}
}
