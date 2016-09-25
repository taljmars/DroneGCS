package gui.core.internalPanels;

import gui.core.internalFrames.JInternalFrameActualPWM;
import gui.core.internalFrames.JInternalFrameMap;
import gui.is.interfaces.KeyBoardControler;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import mavlink.is.drone.Drone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("toolbarSatellite")
public class JPanelToolBarSatellite extends JToolBar implements MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 763689884103713162L;
	
	private JPanel pnl;
	private JButton btnMap;
	private JButton btnActualPWM;
	private JLabel lblCriticalMsg;

	private JPanelMissionBox missionBox;
	private JPanelConfigurationBox configurationBox;
	private JDesktopPane container;
	
	JInternalFrameMap internalMapFrame = null;

	@Autowired
	public JPanelToolBarSatellite(JDesktopPane container, JPanelMissionBox missionBox, JPanelConfigurationBox configurationBox, KeyBoardControler keyBoardController, Drone drone) {		
		this.missionBox = missionBox;
		this.configurationBox = configurationBox;
		this.container = container;
		
		pnl = new JPanel();
		pnl.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		btnMap = new JButton("Map");
        btnMap.setSelected(true);
        pnl.add(btnMap);
        btnMap.addMouseListener(this);
        
        btnActualPWM = new JButton("Actual PWM");
        pnl.add(btnActualPWM);
        btnActualPWM.addMouseListener(this);
        
        lblCriticalMsg = new JLabel("MSG");
        lblCriticalMsg.setHorizontalAlignment(SwingConstants.TRAILING);
        pnl.add(lblCriticalMsg);
        
        add(pnl);
        
        internalMapFrame = new JInternalFrameMap("Map View", container, missionBox, configurationBox, drone, keyBoardController);
        container.add(internalMapFrame);
        try {
			internalMapFrame.setMaximum(true);
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == btnMap) {
			internalMapFrame.setVisible(true);
			internalMapFrame.moveToFront();
			return;
		}
		
		if (e.getSource() == btnActualPWM) {
			JInternalFrameActualPWM.Generate(container);
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

}
