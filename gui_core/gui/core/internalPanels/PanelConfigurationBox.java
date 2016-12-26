package gui.core.internalPanels;

import java.util.Arrays;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import gui.is.events.GuiEvent;
import gui.is.events.GuiEvent.COMMAND;
import gui.is.services.EventPublisherSvc;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import mavlink.is.drone.Drone;

@ComponentScan("tools.comm.internal")
@ComponentScan("mavlink.core.drone")
@Component("areaConfiguration")
public class PanelConfigurationBox extends Pane {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 8111843990697031411L;
	
	@Resource(name = "eventPublisherSvc")
	@NotNull(message = "Internal Error: Failed to get GUI event publisher")
	protected EventPublisherSvc eventPublisherSvc;
	
	@Resource(name = "drone")
	private Drone drone;
	
	private CheckBox cbActiveGeofencePerimeterAlertOnly;
	private ComboBox<Integer> cmbframeContainerCells;
	private Button btnUpdateDevice;
	private TextField txtDeviceId;
	
	private static int called = 0;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		GridPane panel = new GridPane();
		panel.setAlignment(Pos.CENTER);
		panel.setVgap(10);
		panel.setHgap(10);
		int columnIndex = 0;
		int rowIndex = 0;
		
        cbActiveGeofencePerimeterAlertOnly = new CheckBox("Active GeoFence/Perimeter Alert Only");
        cbActiveGeofencePerimeterAlertOnly.setOnAction( e -> drone.getPerimeter().setAlertOnly(cbActiveGeofencePerimeterAlertOnly.isSelected() ? true : false));
        cbActiveGeofencePerimeterAlertOnly.setSelected(false);
        panel.add(cbActiveGeofencePerimeterAlertOnly, columnIndex, rowIndex++);
        
        Vector<Integer> frames = new Vector<Integer>();
        frames.addAll(Arrays.asList(1,2,3,4));
        Label lbl = new Label("Frames Amount");
        panel.add(lbl, columnIndex, rowIndex);
        cmbframeContainerCells = new ComboBox<Integer>();
        cmbframeContainerCells.setValue(2);
        cmbframeContainerCells.setOnAction( e -> eventPublisherSvc.publish(new GuiEvent(COMMAND.SPLIT_FRAMECONTAINER, cmbframeContainerCells.getValue())));
        cmbframeContainerCells.getItems().addAll(new Vector<Integer>(frames));
        panel.add(cmbframeContainerCells, columnIndex + 1, rowIndex++);
        
        btnUpdateDevice = new Button("Update");
        panel.add(btnUpdateDevice, columnIndex, rowIndex);
        btnUpdateDevice.setOnAction( e -> {
        	int device = Integer.parseInt(txtDeviceId.getText());
        	eventPublisherSvc.publish(new GuiEvent(COMMAND.CAMERA_DEVICEID, device));	
        });
        txtDeviceId = new TextField("0");
        panel.add(txtDeviceId, columnIndex + 1, rowIndex++);
        
        getChildren().add(panel);
	}
}
