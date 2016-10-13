package gui.is.events;

import org.springframework.context.ApplicationEvent;

/**
 * Used for passing events between UI components and other
 * objects that register as a JMapViewerEventListener
 *
 * @author taljmars
 *
 */
public class JMVCommandEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 8701544867914969620L;
	
    /**
     * Command type - UI event only
     * @author taljmars
     *
     */
    public enum COMMAND {
        MOVE,
        ZOOM,
        CONTORL_KEYBOARD,
        CONTORL_MAP,
        FLIGHT, 
        EDITMODE_EXISTING_LAYER_START, 
        EDITMODE_PUBLISH,
        EDITMODE_DISCARD, 
        POPUP_MAP,
    }

    private COMMAND command;

    /**
     * @param cmd
     * @param source
     */
    public JMVCommandEvent(COMMAND cmd, Object source) {
        super(source);

        this.command = cmd;
    }

    /**
     * get command enum type
     * @return the command
     */
    public COMMAND getCommand() {
        return command;
    }
}
