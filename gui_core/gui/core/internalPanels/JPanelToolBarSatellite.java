package gui.core.internalPanels;

import gui.core.internalFrames.AbstractJInternalFrame;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import logger.Logger;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.protocol.msg_metadata.ApmModes;
import org.springframework.stereotype.Component;

@Component("toolbarSatellite")
public class JPanelToolBarSatellite extends JToolBar implements MouseListener, OnDroneListener {
	
	private static final long serialVersionUID = 763689884103713162L;
	
	private JPanel pnl;
	private JButton btnSetMode;
	private JButton btnMap;
	private JButton btnActualPWM;
	JComboBox<ApmModes> flightModesCombo;
	private JLabel lblCriticalMsg;

	@Resource(name = "frameContainer")
	private JDesktopPane frameContainer;
	
	@Resource(name = "internalFrameMap")
	private AbstractJInternalFrame internalFrameMap;
	
	@Resource(name = "internalFrameActualPWM")
	private AbstractJInternalFrame internalFrameActualPWM;
	
	@Resource(name = "drone")
	public Drone drone;

	public JPanelToolBarSatellite() {
		
		pnl = new JPanel();
		pnl.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		btnMap = new JButton("Map");
        pnl.add(btnMap);
        btnMap.addMouseListener(this);
        
        btnActualPWM = new JButton("Actual PWM");
        pnl.add(btnActualPWM);
        btnActualPWM.addMouseListener(this);
        
        btnMap.setSelected(true);
        
        lblCriticalMsg = new JLabel("");
        lblCriticalMsg.setHorizontalAlignment(SwingConstants.TRAILING);
        pnl.add(lblCriticalMsg);
        
        
        JPanel pnlMode = new JPanel();
		pnlMode.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		pnlMode.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
        Vector<ApmModes> flightModes = new Vector<ApmModes>();
        flightModes.add(ApmModes.ROTOR_STABILIZE);
        flightModes.add(ApmModes.ROTOR_ACRO);
        flightModes.add(ApmModes.ROTOR_ALT_HOLD);
        flightModes.add(ApmModes.ROTOR_AUTO);
        flightModes.add(ApmModes.ROTOR_GUIDED);
        flightModes.add(ApmModes.ROTOR_LOITER);
        flightModes.add(ApmModes.ROTOR_RTL);
        flightModes.add(ApmModes.ROTOR_CIRCLE);
        flightModes.add(ApmModes.ROTOR_LAND);
        flightModes.add(ApmModes.ROTOR_TOY);
        flightModes.add(ApmModes.ROTOR_SPORT);
        flightModes.add(ApmModes.ROTOR_AUTOTUNE);
        flightModes.add(ApmModes.ROTOR_POSHOLD);

        flightModesCombo = new JComboBox<>(flightModes);
        pnlMode.add(flightModesCombo);
        btnSetMode = new JButton("Set Mode");
        btnSetMode.addMouseListener(this);
        pnlMode.add(btnSetMode);
        pnl.add(pnlMode);
        
        add(pnl);
	}
	
	@PostConstruct
	public void init() {
        frameContainer.add(internalFrameMap);
        frameContainer.add(internalFrameActualPWM);
        
        try {
        	internalFrameMap.setMaximum(true);
        	internalFrameActualPWM.setMaximum(true);
		} catch (PropertyVetoException e) {
			Logger.LogErrorMessege("Internal Error: Failed to load frame");
			Logger.LogErrorMessege(e.getMessage());
		}
        
        internalFrameActualPWM.setVisible(false);
        internalFrameMap.setVisible(true);
        
        drone.addDroneListener(this);
	}
	

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == btnMap) {
			internalFrameMap.setVisible(true);
			internalFrameMap.moveToFront();
			return;
		}
		
		if (e.getSource() == btnActualPWM) {
			internalFrameActualPWM.setVisible(true);
			internalFrameActualPWM.moveToFront();
			return;
		}
		
		if (e.getSource() == btnSetMode) {
			drone.getState().changeFlightMode((ApmModes) flightModesCombo.getSelectedItem());
			return;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	public void ClearNotification() {
		lblCriticalMsg.setVisible(false);
	}

	public void SetNotification(String notification) {
		lblCriticalMsg.setVisible(true);
		if (lblCriticalMsg.getBackground() != Color.BLUE) {
			lblCriticalMsg.setBackground(Color.BLUE);
			lblCriticalMsg.setForeground(Color.WHITE);
		}
		else {
			lblCriticalMsg.setBackground(Color.YELLOW);
			lblCriticalMsg.setForeground(Color.BLACK);
		}
		lblCriticalMsg.setOpaque(true);
		lblCriticalMsg.setText(notification);
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			flightModesCombo.setSelectedItem(drone.getState().getMode());
			return;
		}
	}

}
