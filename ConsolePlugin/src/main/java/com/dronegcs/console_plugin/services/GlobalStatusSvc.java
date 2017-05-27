package com.dronegcs.console_plugin.services;

/**
 * Created by taljmars on 5/17/17.
 */
public interface GlobalStatusSvc {

    enum Component {
        ANTENNA,
        DETECTOR
    }

    boolean isComponentOn(GlobalStatusSvc.Component component);

    void setComponentStatus(GlobalStatusSvc.Component component, boolean isConnected);

}
