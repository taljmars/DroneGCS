package com.dronegcs.console_plugin.services.internal.logevents;

import org.springframework.context.ApplicationEvent;

/**
 * Used for passing events between UI components and other
 * objects that register as a JMapViewerEventListener
 *
 * @author taljmars
 *
 */
public class QuadGuiEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 8701544867914969620L;
	
    /**
     * Command type - UI event only
     * @author taljmars
     *
     */
    public enum QUAD_GUI_COMMAND {
//        MOVE,
//        ZOOM,
//        CONTORL_KEYBOARD,
//        FLIGHT, 
        EDITMODE_EXISTING_LAYER_START,
        EDITMODE_EXISTING_LAYER_CANCELED,
        EDITMODE_EXISTING_LAYER_FINISH,
//        POPUP_MAP, 
        
        // Missions
        MISSION_UPDATED_BY_MAP, MISSION_UPDATED_BY_TABLE, MISSION_EDITING_STARTED, MISSION_EDITING_FINISHED, 
        MISSION_VIEW_ONLY_STARTED, MISSION_VIEW_ONLY_FINISHED,

        // Sessions
        PRIVATE_SESSION_STARTED,
        PUBLISH,
        DISCARD,
        
        
        EXIT, 
        SPLIT_FRAMECONTAINER, 
        
        CAMERA_DEVICEID, LAYER_PERIMETER_EDITING_FINISHED, DETECTOR_LOAD_FAILURE, PERIMETER_UPDATED_BY_TABLE, UPDATED_INTERNAL_FRAME_SIZE
    }

    private QUAD_GUI_COMMAND command;

    /**
     * @param cmd
     * @param source
     */
    public QuadGuiEvent(QUAD_GUI_COMMAND cmd, Object source) {
        super(source);

        this.command = cmd;
    }

    public QuadGuiEvent(QUAD_GUI_COMMAND cmd) {
        super(new Object());

        this.command = cmd;
    }

    /**
     * get command enum type
     * @return the command
     */
    public QUAD_GUI_COMMAND getCommand() {
        return command;
    }
}
