package com.dronegcs.console.controllers;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneLaunchPreloader extends Preloader {
    private final static Logger LOGGER = LoggerFactory.getLogger(DroneLaunchPreloader.class);
    private Stage preloaderStage;

    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;
        Scene scene = new Scene(new ProgressBar(), 300, 25);
        preloaderStage.initStyle(StageStyle.TRANSPARENT);
        preloaderStage.setScene(scene);
        preloaderStage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide();
        }
    }
}
