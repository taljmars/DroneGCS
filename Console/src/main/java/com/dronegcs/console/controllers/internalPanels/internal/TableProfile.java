package com.dronegcs.console.controllers.internalPanels.internal;

import com.dronegcs.console.controllers.internalPanels.PanelTableBox;

/**
 * Created by taljmars on 7/10/2017.
 */
public abstract class TableProfile {

    public abstract void setBigTableView(PanelTableBox panelTableBox);

    public abstract void generateTable(boolean editMode, Object contentPayload);
}
