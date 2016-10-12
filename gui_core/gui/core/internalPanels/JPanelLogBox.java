package gui.core.internalPanels;

import gui.is.events.LogAbstractDisplayerEvent;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.annotation.PostConstruct;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.text.html.HTMLEditorKit;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import logger.Logger;
import logger.Logger.Type;

@Component("areaLogBox")
public class JPanelLogBox extends JPanel {

	private static final long serialVersionUID = 7668860997050137469L;
	
	private static final int LOG_BOX_MAX_LINES = 50;// 7;
	
	private JTextPane logBox;

	public JPanelLogBox() {
		LayoutManager layout = new GridBagLayout();
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
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
	
	private String getDisplayedLoggerText() {
		if (logBox == null)
			System.err.println("LogBox was not created");
		return logBox.getText();
	}   
	
	
	private void setDisplayedLoggerText(String text) {
		if (logBox == null)
			System.err.println("LogBox was not created");
		logBox.setText(text);
	}

	private int getMaxLoggerDisplayedLines() {
		return LOG_BOX_MAX_LINES;
	}
	
	private void addGeneralMessegeToDisplay(String cmd) {
		System.out.println(cmd);
		addMessegeToDisplay(cmd, Type.GENERAL);
	}
	
	private void addErrorMessegeToDisplay(String cmd) {
		System.err.println(cmd);
		addMessegeToDisplay(cmd, Type.ERROR);
	}
	
	private void addOutgoingMessegeToDisplay(String cmd) {
		System.out.println(cmd);
		addMessegeToDisplay(cmd, Type.OUTGOING);
	}
	
	private void addIncommingMessegeToDisplay(String cmd) {
		System.out.println(cmd);
		addMessegeToDisplay(cmd, Type.INCOMING);
	}
	
	private void addMessegeToDisplay(String cmd, Type t) {
		addMessegeToDisplay(cmd, t, false);
	}
	
	private synchronized void addMessegeToDisplay(String cmd, Type t, boolean no_date) {
		String newcontent = Logger.generateDesignedMessege(cmd, t, no_date);
		
		String alltext = getDisplayedLoggerText();
		String content = "";
		if (!alltext.isEmpty())
			content = alltext.substring(alltext.indexOf("<body>") + "<body>".length(), alltext.indexOf("</body>"));
		int idx = content.indexOf("<font");
		if (idx == -1) {
			content = "";
		}
		else {
			content = content.substring(idx);
		}
		
		content = (newcontent + content);

		// To Screen
		String futureText = "<html>";
		int maxDisplayerLines = getMaxLoggerDisplayedLines();
		String[] sz = content.split("</font>", maxDisplayerLines);
		for (int i = 0 ; i < Math.min(maxDisplayerLines - 1, sz.length) ; i++) {
			futureText += (sz[i] + "</font>");
		}
		
		futureText += "</html>";
		setDisplayedLoggerText(futureText);
		
		Logger.LogDesignedMessege(newcontent);
	}
	
	@EventListener
	public void onLogDisplayerEvent(LogAbstractDisplayerEvent event) {
		switch (event.getType()) {
		case ERROR:
			addErrorMessegeToDisplay(event.getEntry());
			break;
		case GENERAL:
			addGeneralMessegeToDisplay(event.getEntry());
			break;
		case INCOMING:
			addIncommingMessegeToDisplay(event.getEntry());
			break;
		case OUTGOING:
			addOutgoingMessegeToDisplay(event.getEntry());
			break;
		}
	}
}
