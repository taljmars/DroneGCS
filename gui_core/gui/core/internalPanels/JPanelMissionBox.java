package gui.core.internalPanels;

import java.awt.Dimension;

import javax.annotation.PostConstruct;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("areaMission")
public class JPanelMissionBox extends JScrollPane {

	private static final long serialVersionUID = 86865235148815438L;
	
	@Autowired	
	public JPanelMissionBox(Dimension dimension) {
			super(null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			setPreferredSize(dimension);
	}
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}

	public void clear() {
		setViewportView(null);
	}

	public void updateTable(JTable missionTable) {
		setViewportView(missionTable);
	}

}
