package gui.core.internalFrames;

import javax.management.RuntimeErrorException;
import javax.swing.JInternalFrame;

public abstract class AbstractJInternalFrame extends JInternalFrame {

	private static final long serialVersionUID = -866759734300260983L;
	
	private static boolean loaded = false;
	
	public AbstractJInternalFrame(String name, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
		super(name, resizable, closable, maximizable, iconifiable);
		setLoaded(true);
	}

	protected synchronized void setLoaded(boolean isLoaded) {
		loaded = isLoaded;
	}
	
	public synchronized boolean isLoaded() {
		return loaded;
	}
	
	@Override
	public void dispose() {
		System.out.println(getClass().getName() + " In dispose");
		setLoaded(false);
		super.dispose();
	}

	public static String generateMyOwnMethodName = "generateMyOwn";
	public static AbstractJInternalFrame generateMyOwn() {
		throw new RuntimeErrorException(null, "This method must be overrided");
	}

}
