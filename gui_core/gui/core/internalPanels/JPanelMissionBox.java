package gui.core.internalPanels;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class JPanelMissionBox extends JScrollPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 86865235148815438L;
	
	public JPanelMissionBox(JPanel pnl, int verticalScrollbarAsNeeded, int horizontalScrollbarAsNeeded, Dimension panelDimension) {
			super(pnl, verticalScrollbarAsNeeded, horizontalScrollbarAsNeeded);
			setPreferredSize(panelDimension);
	}

	public void clear() {
		setViewportView(null);
	}

	public void updateTable(JTable missionTable) {
		setViewportView(missionTable);
	}

}
