package com.dronegcs.console_plugin.simulator;

import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.mavlink.is.connection.MirrorHandler;
import com.dronegcs.mavlink.is.drone.Drone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;

@Component
public class DroneStreamSerializer implements MirrorHandler {

    private long startTime;
    private FileOutputStream outputStream;
    private boolean isRunning;

    @Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
    private LoggerDisplayerSvc loggerDisplayerSvc;

    @Autowired
    private Drone drone;

    @PostConstruct
    private void init() {
        isRunning = false;
    }

    public void openStream(File fileInput) {
        if (this.isRunning) {
            loggerDisplayerSvc.logError("Stream record already open");
            return;
        }

        try {
            this.isRunning = true;
            drone.getMavClient().addMirrorHandler(this);
            this.startTime = System.currentTimeMillis();
            this.outputStream = new FileOutputStream(fileInput);
        }
        catch (Exception e) {

        }
    }
    @Override
    public void take(byte[] bytes, int i){
        try {
            System.out.println("Writing " + new String(bytes));
            outputStream.write(PREFIX.getBytes());
            outputStream.write(((System.currentTimeMillis() - this.startTime) + "").getBytes());
            outputStream.write(SEP.getBytes());
            if (i > 0) {
                System.out.println("Have something to write");
            }
            outputStream.write(bytes, 0, i);
            outputStream.write(SUFFIX.getBytes());
            outputStream.write(System.lineSeparator().getBytes());
        }
        catch (Exception e) {

        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void close() {
        try {
            if (outputStream != null) outputStream.close();
        }
        catch (Exception e) {

        }

        outputStream = null;
        isRunning = false;
        drone.getMavClient().removeMirrorHandler(this);
    }

    private static String PREFIX = "<<<";
    private static String SEP = "---";
    private static String SUFFIX = ">>>";


    enum ParseMode {
        INIT,
        PROCESS
    }
    private static ParseMode md = ParseMode.INIT;
    private static MessageBuffer mBuffer = null;

    public static MessageBuffer parse(String entryString) {
        String input = entryString;
        if (entryString.startsWith(PREFIX)) {
            if (md.equals(ParseMode.PROCESS)) {
                System.out.println("Corrupted Message");
                md = ParseMode.INIT;
            }
            input = input.substring(PREFIX.length());
        }
        if (md.equals(ParseMode.INIT)) {
            String[] spl = input.split(SEP,2);
            mBuffer = new MessageBuffer(Integer.parseInt(spl[0]));
            if (spl[1].endsWith(SUFFIX)) {
                mBuffer.appendBuffer(spl[1], spl[1].length() - SUFFIX.length());
                return mBuffer;
            }
            else {
                mBuffer.appendBuffer(spl[1] + System.lineSeparator(), spl[1].length() + System.lineSeparator().length());
            }
            md = ParseMode.PROCESS;
            return null;
        }

        if (md.equals(ParseMode.PROCESS)) {
            if (input.endsWith(SUFFIX)) {
                mBuffer.appendBuffer(input, input.length() - SUFFIX.length());
                md = ParseMode.INIT;
                return mBuffer;
            }
            else {
                mBuffer.appendBuffer(input + System.lineSeparator(), input.length() + System.lineSeparator().length());
                return null;
            }
        }

        return null;
    }

}

