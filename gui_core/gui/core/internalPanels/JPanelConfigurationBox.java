package gui.core.internalPanels;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import tools.comm.SerialConnection;
import mavlink.is.drone.Drone;

@ComponentScan("tools.comm.internal")
@ComponentScan("mavlink.core.drone")
@Component("areaConfiguration")
public class JPanelConfigurationBox extends JScrollPane {

	private static final long serialVersionUID = 8111843990697031411L;
	
	private JPanel panel;
	
	private JCheckBox cbActiveGeofencePerimeterAlertOnly;
	
	private JComboBox<Object> cmbPortList;
	
	private JComboBox<Integer> cmbBaud;
	
	@Resource(name = "drone")
	private Drone drone;
	
	@Resource(name = "twoWaySerialComm")
	private SerialConnection serialConnection;
	
	@Autowired
	public JPanelConfigurationBox(JPanel emptyPanel, Dimension dimension) {
		super(emptyPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setPreferredSize(dimension);
        panel = emptyPanel;
	}
	
	private static int called = 0;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
        cbActiveGeofencePerimeterAlertOnly = new JCheckBox("Active GeoFence/Perimeter Alert Only");
        cbActiveGeofencePerimeterAlertOnly.addActionListener( e -> drone.getPerimeter().setAlertOnly(cbActiveGeofencePerimeterAlertOnly.isSelected() ? true : false));
        cbActiveGeofencePerimeterAlertOnly.setSelected(false);
        panel.add(cbActiveGeofencePerimeterAlertOnly);

        Object[] portList = serialConnection.listPorts();
        cmbPortList = new JComboBox<>(portList);
        cmbPortList.addActionListener( e -> serialConnection.setPortName((String) cmbPortList.getSelectedItem()));
        if (cmbPortList.getItemCount() > 0)
        	serialConnection.setPortName((String) cmbPortList.getItemAt(0));
        panel.add(cmbPortList);
        
        List<Integer> baudList = Arrays.asList(57600, 115200);
        cmbBaud = new JComboBox<>(new Vector<Integer>(baudList));
        cmbBaud.addActionListener( e -> serialConnection.setBaud((Integer) cmbBaud.getSelectedItem()));
        if (cmbBaud.getItemCount() > 0)
        	serialConnection.setBaud(115200);
        panel.add(cmbBaud);
	}
}
