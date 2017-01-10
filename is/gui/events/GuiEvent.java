package gui.events;

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
    public enum COMMAND {
        MOVE,
        ZOOM,
        CONTORL_KEYBOARD,
        FLIGHT, 
        EDITMODE_EXISTING_LAYER_START,
        POPUP_MAP, 
        
        // Missions
        MISSION_UPDATED_BY_MAP, MISSION_UPDATED_BY_TABLE, MISSION_EDITING_STARTED, MISSION_EDITING_FINISHED, 
        MISSION_VIEW_ONLY_STARTED, MISSION_VIEW_ONLY_FINISHED,
        
        
        EXIT, 
        SPLIT_FRAMECONTAINER, 
        
        CAMERA_DEVICEID, UPDATED_INTERNAL_FRAME_SIZE
    }

    private COMMAND command;

    /**
     * @param cmd
     * @param source
     */
    public GuiEvent(COMMAND cmd, Object source) {
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
