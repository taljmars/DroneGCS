package com.gui.is.events;

import org.springframework.context.ApplicationEvent;

/**
 * Used for passing events between UI components and other
 * objects that register as a JMapViewerEventListener
 *
 * @author taljmars
 *
 */
public class GuiEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 8701544867914969620L;
	
    /**
     * Command type - UI event only
     * @author taljmars
     *
     */
    public enum MAPVIEWER_GUI_COMMAND {
        EDITMODE_EXISTING_LAYER_START
    }

    private MAPVIEWER_GUI_COMMAND command;

    /**
     * @param cmd
     * @param source
     */
    public GuiEvent(MAPVIEWER_GUI_COMMAND cmd, Object source) {
        super(source);

        this.command = cmd;
    }

    /**
     * get command enum type
     * @return the command
     */
    public MAPVIEWER_GUI_COMMAND getCommand() {
        return command;
    }
}
