package gui.core.internalFrames;

import javax.swing.JInternalFrame;

public abstract class AbstractJInternalFrame extends JInternalFrame {

	private static final long serialVersionUID = -866759734300260983L;
	
	public AbstractJInternalFrame(String name, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
		super(name, resizable, closable, maximizable, iconifiable);
	}
}
