package com.dronegcs.console_plugin.simulator;

import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.generic_tools.devices.SerialConnection;
import javafx.application.Platform;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DroneStreamFakeConnection implements SerialConnection {

    private long startTime;
    private BufferedReader inputStream;
    private long lineIndex = 0;

    private long receivedBytes = 0L;
    private long transmittedBytes = 0L;
    private long lastReadTimestamp = 0L;
    private long bytesSinceLastRead = 0L;
    private long receivedBytesPerSecond = 0L;
    private long lastWriteTimestamp = 0L;
    private long bytesSinceLastWrite = 0L;
    private long transmittedBytesPerSecond = 0L;

    private ApplicationContext applicationContext;
    private LoggerDisplayerSvc loggerDisplayerSvc;
    private Drone drone;


    public void openStream(File fileInput) {
        try {
            this.startTime = System.currentTimeMillis();
            this.inputStream = new BufferedReader(new FileReader(fileInput));
        }
        catch (Exception e) {
            loggerDisplayerSvc.logError("Failed to open stream " + e.getMessage());
        }
    }


    @Override
    public boolean connect() throws Exception {
        loggerDisplayerSvc.logGeneral("Drone simulator stream connected");
        return true;
    }

    @Override
    public boolean disconnect() {
        try {
            if (inputStream != null) inputStream.close();
            loggerDisplayerSvc.logGeneral("Drone simulator stream disconnected");
        }
        catch (Exception e) {
            loggerDisplayerSvc.logError(e.getMessage());
        }
        inputStream = null;
        return true;
    }

    @Override
    public Object[] listPorts() {
        //dummy
        ArrayList<String> ans = new ArrayList();
        return ans.toArray();
    }

    @Override
    public void write(String s) {
        System.out.println("Ignoring write: " + s);
    }

    @Override
    public void write(byte[] bytes) {
        System.out.println("Ignoring write: " + bytes);
    }

    @Override
    public int read(byte[] bytes, int i) {
        MessageBuffer streamRecord = null;
        String line = null;
        try {
            while (streamRecord == null) {
                line = inputStream.readLine();
                if (line == null) {
                    loggerDisplayerSvc.logGeneral("Finished streaming the file (" + lineIndex + " lines)");
                    drone.getMavClient().disconnect();
                    Platform.runLater(()-> drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED));
                    return -1;
                }
                lineIndex++;
                streamRecord = DroneStreamSerializer.parse(line);
            }

            // streamRecord is not null

            if (streamRecord.getLength() > i)
                loggerDisplayerSvc.logError("Buffer is not enough");

            if (streamRecord.getLength() > 0)
                System.arraycopy(streamRecord.getBuffer(), 0, bytes, 0, bytes.length);

            this.bytesSinceLastRead += (long)streamRecord.getLength();
            this.receivedBytes += (long)streamRecord.getLength();
            if (System.currentTimeMillis() - this.lastReadTimestamp > 1000L) {
                this.receivedBytesPerSecond = (long)(1.0D * (double)this.bytesSinceLastRead / (double)((System.currentTimeMillis() - this.lastReadTimestamp) / 1000L));
                this.lastReadTimestamp = System.currentTimeMillis();
                this.bytesSinceLastRead = 0L;
            }

            long timestamp = System.currentTimeMillis() - this.startTime;
            if (timestamp <= streamRecord.getTimestamp()) {
//                wait(streamRecord.timestamp - timestamp);
                System.out.println("Waiting " + (streamRecord.getTimestamp() - timestamp));
                Thread.sleep(streamRecord.getTimestamp() - timestamp);
            }
            return streamRecord.getLength();
        }
        catch (IndexOutOfBoundsException e) {
            loggerDisplayerSvc.logError(e.getMessage());
            e.printStackTrace();
            System.out.println("Timestamp: " + streamRecord.getTimestamp());
            System.out.println("Source Length: " + streamRecord.getLength());
            System.out.println("Dest Length: " + bytes.length);
        }
        catch (NullPointerException e) {
            loggerDisplayerSvc.logError(e.getMessage());
            System.out.println("Entry String: " + line);
            e.printStackTrace();
        }
        catch (IOException e) {
            loggerDisplayerSvc.logError(e.getMessage());
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            loggerDisplayerSvc.logError(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void setPortName(String s) {
        System.out.println("Ignoring setting port: " + s);
    }

    @Override
    public void setBaud(Integer integer) {
        System.out.println("Ignoring setting baud: " + integer);
    }

    @Override
    public Integer[] baudList() {
        Integer[] oblist = new Integer[]{666};
        return oblist;
    }

    @Override
    public Integer getDefaultBaud() {
        return 666;
    }

    @Override
    public long getReceivedBytesPerSeconds() {
        return receivedBytesPerSecond;
    }

    @Override
    public long getTransmittedBytesPerSeconds() {
        return transmittedBytesPerSecond;
    }

    @Override
    public long getTx() {
        return transmittedBytes;
    }

    @Override
    public long getRx() {
        return receivedBytes;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        loggerDisplayerSvc = applicationContext.getBean(LoggerDisplayerSvc.class);
        drone = applicationContext.getBean(Drone.class);
    }
}
