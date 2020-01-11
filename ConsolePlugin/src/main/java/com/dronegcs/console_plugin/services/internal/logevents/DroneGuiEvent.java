package com.dronegcs.console_plugin.services.internal.logevents;

import org.springframework.context.ApplicationEvent;

/**
 * Used for passing events between UI components and other
 * objects that register as a JMapViewerEventListener
 *
 * @author taljmars
 *
 */
public class DroneGuiEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 8701544867914969620L;
	
    /**
     * Command type - UI event only
     * @author taljmars
     *
     */
    public enum DRONE_GUI_COMMAND {
//        MOVE,
//        ZOOM,
//        CONTORL_KEYBOARD,
//        FLIGHT, 
        EDITMODE_EXISTING_LAYER_START,
//        EDITMODE_EXISTING_LAYER_CANCELED,
        EDITMODE_EXISTING_LAYER_FINISH,
//        POPUP_MAP, 
        
        // Missions
        MISSION_UPDATED_BY_MAP, MISSION_UPDATED_BY_TABLE, MISSION_EDITING_STARTED, MISSION_EDITING_FINISHED,
        NEW_MISSION_EDITING_STARTED, NEW_MISSION_EDITING_CANCELED,
        MISSION_VIEW_ONLY_STARTED, MISSION_VIEW_ONLY_FINISHED,

        PERIMETER_EDITING_FINISHED, DETECTOR_LOAD_FAILURE, PERIMETER_UPDATED_BY_TABLE, PERIMETER_EDITING_STARTED, PERIMETER_VIEW_ONLY_STARTED, PERIMETER_VIEW_ONLY_FINISHED, PERIMETER_UPDATED_BY_MAP,
        NEW_PERIMETER_EDITING_STARTED, NEW_PERIMETER_EDITING_CANCELED,

        DRAW_EDITING_STARTED,

        UPDATED_INTERNAL_FRAME_SIZE,
        // Sessions
        PRIVATE_SESSION_STARTED,
        PUBLISH,
        DISCARD,


        EXIT,
        SPLIT_FRAMECONTAINER,

        CAMERA_DEVICEID,

        USER_PROFILE_LOADED,
    }

    private DRONE_GUI_COMMAND command;

    /**
     * @param cmd
     * @param source
     */
    public DroneGuiEvent(DRONE_GUI_COMMAND cmd, Object source) {
        super(source);

        this.command = cmd;
    }

    public DroneGuiEvent(DRONE_GUI_COMMAND cmd) {
        super(new Object());

        this.command = cmd;
    }

    /**
     * get command enum type
     * @return the command
     */
    public DRONE_GUI_COMMAND getCommand() {
        return command;
    }
}
