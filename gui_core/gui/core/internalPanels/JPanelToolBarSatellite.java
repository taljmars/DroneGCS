package gui.core.internalPanels;

import gui.core.internalFrames.AbstractJInternalFrame;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import logger.Logger;

import org.springframework.stereotype.Component;

@Component("toolbarSatellite")
public class JPanelToolBarSatellite extends JToolBar implements MouseListener {
	
	private static final long serialVersionUID = 763689884103713162L;
	
	private JPanel pnl;
	private JButton btnMap;
	private JButton btnActualPWM;
	private JLabel lblCriticalMsg;

	@Resource(name = "frameContainer")
	private JDesktopPane frameContainer;
	
	@Resource(name = "internalFrameMap")
	private AbstractJInternalFrame internalFrameMap;
	
	@Resource(name = "internalFrameActualPWM")
	private AbstractJInternalFrame internalFrameActualPWM;

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
        
        lblCriticalMsg = new JLabel("MSG");
        lblCriticalMsg.setHorizontalAlignment(SwingConstants.TRAILING);
        pnl.add(lblCriticalMsg);
        
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
