package gui.core.internalPanels;

import gui.is.services.LoggerDisplayerListener;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.text.html.HTMLEditorKit;

public class JPanelLogBox extends JPanel implements LoggerDisplayerListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7668860997050137469L;
	
	private static final int LOG_BOX_MAX_LINES = 50;// 7;
	
	private JTextPane logBox;

	public JPanelLogBox(LayoutManager layout) {
		setLayout(layout);
		
		JPanel pnlLogBox = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        logBox = new JTextPane();
        logBox.setLayout(new GridLayout(1,1,0,0));
        JPanel pnlLogbox = new JPanel(new GridLayout(1,1));
        JScrollPane logbox = new JScrollPane(logBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension southPanelDimension = new Dimension(1200, 150);
        
        logbox.setPreferredSize(southPanelDimension);
        logBox.setEditorKit(new HTMLEditorKit());
        logBox.setEditable(false);
        pnlLogbox.add(logbox);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 1.0;	// request any extra vertical space
        pnlLogBox.add(pnlLogbox, c);
        
        JPanel pnlLogToolBox = new JPanel(new GridLayout(0,1,0,0));
        JToggleButton areaLogLockTop = new JToggleButton("Top");        
        areaLogLockTop.addActionListener(e -> {if (areaLogLockTop.isSelected()) logbox.getVerticalScrollBar().setValue(0);});
        pnlLogToolBox.add(areaLogLockTop);
        
        JButton areaLogClear = new JButton("CLR");
        areaLogClear.addActionListener(e -> logBox.setText(""));
        pnlLogToolBox.add(areaLogClear);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 1.0;   //request any extra vertical space
        pnlLogBox.add(pnlLogToolBox, c);
        
        add(pnlLogBox);
	}
	
	@Override
	public String getDisplayedLoggerText() {
		if (logBox == null)
			System.err.println("LogBox was not created");
		return logBox.getText();
	}   
	
	@Override
	public void setDisplayedLoggerText(String text) {
		if (logBox == null)
			System.err.println("LogBox was not created");
		logBox.setText(text);
	}

	@Override
	public int getMaxLoggerDisplayedLines() {
		return LOG_BOX_MAX_LINES;
	}
}
