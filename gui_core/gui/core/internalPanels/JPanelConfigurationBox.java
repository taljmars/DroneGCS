package gui.core.internalPanels;

import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mavlink.is.drone.Drone;

public class JPanelConfigurationBox extends JScrollPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8111843990697031411L;
	
	private JCheckBox cbActiveGeofencePerimeterAlertOnly;
	private Drone drone;
	
	public JPanelConfigurationBox(JPanel pnl, int verticalScrollbarAsNeeded, int horizontalScrollbarAsNeeded, Dimension panelDimension) {
		super(pnl, verticalScrollbarAsNeeded, horizontalScrollbarAsNeeded);
		
        setPreferredSize(panelDimension);
        
        cbActiveGeofencePerimeterAlertOnly = new JCheckBox("Active GeoFence/Perimeter Alert Only");
        cbActiveGeofencePerimeterAlertOnly.addActionListener( e -> drone.getPerimeter().setAlertOnly(cbActiveGeofencePerimeterAlertOnly.isSelected() ? true : false));
        cbActiveGeofencePerimeterAlertOnly.setSelected(false);
        pnl.add(cbActiveGeofencePerimeterAlertOnly);
	}

	public void setDrone(Drone drone) {
		this.drone = drone;
	}
	
//	public void setAlertOn(boolean isOn) {
//		cbActiveGeofencePerimeterAlertOnly.setSelected(isOn);
//	}
}
