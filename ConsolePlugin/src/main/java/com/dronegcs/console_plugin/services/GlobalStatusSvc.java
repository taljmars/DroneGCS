package com.dronegcs.console_plugin.services;

/**
 * Created by oem on 5/17/17.
 */
public interface GlobalStatusSvc {

    boolean isAntennaConnected();

    void setAntennaConnection(boolean isConnected);

    boolean isDetectorConnected();

    void setDetectorConnected(boolean isConnected);
}
