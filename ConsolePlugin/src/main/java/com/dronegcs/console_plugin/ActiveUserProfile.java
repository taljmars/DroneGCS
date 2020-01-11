package com.dronegcs.console_plugin;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class ActiveUserProfile implements Serializable {

    public enum Mode {
        OFFLINE,
        ONLINE
    }

    public enum DEFS {
        HeartBeatFreq("1"),
        ParamAutoFetch("false"),
        ;

        public final String defaultVal;
        DEFS(String i) {
            this.defaultVal = i;
        }
    }

//    public ActiveUserProfile() {
//        throw new RuntimeException("Such constructor should not be called");
//    }

    private void load(String username) {
        System.out.println("Create User Profile");
        ActiveUserProfile tmp = readUsrDefsObject(username);
        if (tmp == null) {
            System.out.println("Local definitions file wasn't found");
//            this.username = username;
            this.settingsMap = new HashMap<>();
            return;
        }
        System.out.println("Local definitions found");

        System.out.println("Object has been deserialized ");
        System.out.println("mode = " + tmp.mode);

        this.mode = tmp.mode;
//        this.username = tmp.username;
        this.settingsMap = tmp.settingsMap;
    }

    private Mode mode = Mode.OFFLINE;
    private String username = "";
    private HashMap<String, String> settingsMap = new HashMap<>();

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        writeUsrDefsObject(this.username, this);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
//        writeUsrDefsObject(this.username, this);
        load(username);
    }

    public void setDefinition(String defKey, String value) {
        settingsMap.put(defKey, value);
        writeUsrDefsObject(this.username, this);
    }

    public String getDefinition(String defKey) {
        return settingsMap.get(defKey);
    }

    public String getDefinition(String defKey, String defaultValue) {
        String val = getDefinition(defKey);
        if (val == null)
            return defaultValue;

        return val;
    }

    private static final String USER_DEF_FILE_SUFFIX = ".ser";

    private static ActiveUserProfile readUsrDefsObject(String username) {
        // Deserialization
        try
        {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(getGCSUserDirectory() + username + USER_DEF_FILE_SUFFIX);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            ActiveUserProfile obj = (ActiveUserProfile)in.readObject();

            in.close();
            file.close();

//            file = new FileInputStream(getGCSUserDirectory() + username + "map" +  USER_DEF_FILE_SUFFIX);
//            in = new ObjectInputStream(file);



            return obj;
        }

        catch(IOException ex)
        {
            System.out.println("IOException is caught");
        }

        catch(ClassNotFoundException ex)
        {
            System.out.println("ClassNotFoundException is caught");
        }
        return null;
    }

    private static void writeUsrDefsObject(String username, ActiveUserProfile activeUserProfile) {
        // Serialization
        try
        {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(getGCSUserDirectory() + username + USER_DEF_FILE_SUFFIX);
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(activeUserProfile);

            out.close();
            file.close();

            System.out.println("Object has been serialized");

        }

        catch(IOException ex)
        {
            System.out.println("IOException is caught");
        }

    }

    private static String getGCSUserDirectory() {
        String maptilePath = System.getProperty("user.home");
        if (maptilePath != null && !maptilePath.isEmpty()) {
            maptilePath += "/DroneGCS/";
            return maptilePath;
        }
        return "./";
    }

}
