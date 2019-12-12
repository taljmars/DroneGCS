package com.dronegcs.console_plugin.simulator;

import com.dronegcs.mavlink.core.connection.USBConnection;
import com.generic_tools.devices.SerialConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DroneStreamSimulator {

    @Autowired
    private USBConnection usbConnection;

    @Autowired
    private SerialConnection serialConnection;

    @Autowired
    private ApplicationContext applicationContext;

    public final static String SIM_DEVICE_NAME = "Simulator - Serial";

    public void activate(File file) {
        DroneStreamFakeConnection fakeStream = new DroneStreamFakeConnection();
        fakeStream.setApplicationContext(applicationContext);
        fakeStream.openStream(file);
        usbConnection.setSerialConnection(fakeStream);
    }

    public void deactivate() {
        usbConnection.setSerialConnection(serialConnection);
    }


}
