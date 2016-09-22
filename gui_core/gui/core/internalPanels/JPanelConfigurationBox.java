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
	
	private JCheckBox cbActiveGeofencePerimeterAlert;
	private JCheckBox cbActiveGeofencePerimeterEnforce;
	private Drone drone;
	
	public JPanelConfigurationBox(JPanel pnl, int verticalScrollbarAsNeeded, int horizontalScrollbarAsNeeded, Dimension panelDimension) {
		super(pnl, verticalScrollbarAsNeeded, horizontalScrollbarAsNeeded);
		
        setPreferredSize(panelDimension);
        
        cbActiveGeofencePerimeterAlert = new JCheckBox("Active GeoFence/Perimeter Alert");
        cbActiveGeofencePerimeterAlert.addActionListener( e -> drone.getPerimeter().setAlert(cbActiveGeofencePerimeterAlert.isSelected() ? true : false));
        cbActiveGeofencePerimeterAlert.setSelected(false);
        pnl.add(cbActiveGeofencePerimeterAlert);
        
        cbActiveGeofencePerimeterEnforce = new JCheckBox("Active GeoFence/Perimeter Enforcement");
        cbActiveGeofencePerimeterEnforce.addActionListener( e -> drone.getPerimeter().setEnforce(cbActiveGeofencePerimeterEnforce.isSelected() ? true : false));
        cbActiveGeofencePerimeterEnforce.setSelected(false);
        pnl.add(cbActiveGeofencePerimeterEnforce);
	}

	public void setDrone(Drone drone) {
		this.drone = drone;
	}
	
	public void setAlertOn(boolean isOn) {
		cbActiveGeofencePerimeterAlert.setSelected(isOn);
	}
	
	public void setEnforceOn(boolean isOn) {
		cbActiveGeofencePerimeterEnforce.setSelected(isOn);		
	}

}
