package gui.core.internalPanels;

import java.awt.Dimension;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import mavlink.is.drone.Drone;

@ComponentScan("mavlink.core.drone")
@Component("areaConfiguration")
public class JPanelConfigurationBox extends JScrollPane {

	private static final long serialVersionUID = 8111843990697031411L;
	
	private JCheckBox cbActiveGeofencePerimeterAlertOnly;
	
	@Resource(name = "drone")
	private Drone drone;
	
	@Autowired
	public JPanelConfigurationBox(JPanel emptyPanel, Dimension dimension) {
		super(emptyPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
        setPreferredSize(dimension);
        
        cbActiveGeofencePerimeterAlertOnly = new JCheckBox("Active GeoFence/Perimeter Alert Only");
        cbActiveGeofencePerimeterAlertOnly.addActionListener( e -> drone.getPerimeter().setAlertOnly(cbActiveGeofencePerimeterAlertOnly.isSelected() ? true : false));
        cbActiveGeofencePerimeterAlertOnly.setSelected(false);
        emptyPanel.add(cbActiveGeofencePerimeterAlertOnly);
	}
	
	private static int called = 0;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
}
