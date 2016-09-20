package gui.core.internalPanels;

import gui.core.internalFrames.JInternalFrameActualPWM;
import gui.core.internalFrames.JInternalFrameMap;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class JPanelToolBar extends JPanel implements MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 763689884103713162L;
	private JButton btnMap;
	private JButton btnActualPWM;
	private JLabel lblCriticalMsg;

	public JPanelToolBar() {
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		btnMap = new JButton("Map");
        btnMap.setSelected(true);
        add(btnMap);
        btnMap.addMouseListener(this);
        
        btnActualPWM = new JButton("Actual PWM");
        add(btnActualPWM);
        btnActualPWM.addMouseListener(this);
        
        lblCriticalMsg = new JLabel("MSG");
        lblCriticalMsg.setHorizontalAlignment(SwingConstants.TRAILING);
        add(lblCriticalMsg);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == btnMap) {
			JInternalFrameMap.Generate();
			return;
		}
		
		if (e.getSource() == btnActualPWM) {
			JInternalFrameActualPWM.Generate();
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
