package com.ngxbridge.svc;

public interface CommunicationSvc {

    Object[] listPorts();

    void sync();

    void armDisarm(boolean shouldArm);

    void land();

    void rtl();

    void holdPosition();

    void startStopMission(boolean shouldStart);

    void enforcePerimeter(boolean isOn);

    void downloadMission();

}
