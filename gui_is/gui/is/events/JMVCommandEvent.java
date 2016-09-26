// License: GPL. For details, see Readme.txt file.
package gui.is.events;

import java.util.EventObject;

/**
 * Used for passing events between UI components and other
 * objects that register as a JMapViewerEventListener
 *
 * @author Jason Huntley
 *
 */
public class JMVCommandEvent extends EventObject {
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
     *
     */
    private static final long serialVersionUID = 8701544867914969620L;

    public JMVCommandEvent(COMMAND cmd, Object source) {
        super(source);

        setCommand(cmd);
    }

    public JMVCommandEvent(Object source) {
        super(source);
    }

    /**
     * @return the command
     */
    public COMMAND getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public void setCommand(COMMAND command) {
        this.command = command;
    }
}
