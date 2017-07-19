package com.dronegcs.console.controllers;

import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class DroneLaunchPreloader extends Preloader {

    private final static Logger LOGGER = LoggerFactory.getLogger(DroneLaunchPreloader.class);

    private Stage preloaderStage;

    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;
        BorderPane root = new BorderPane();
        VBox vBox = new VBox();
        root.setStyle(  "-fx-background-image: url(/com/dronegcs/console/guiImages/background.png);" +
                        "-fx-background-size: cover;");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(20, 20);
        Label label = new Label("Drone GCS");
        label.setStyle("-fx-bounds-type: logical_vertical_center;" +
                        "-fx-font-smoothing-type: lcd;");
        label.setFont(new Font("Aharoni", 32));
        vBox.getChildren().addAll(label, progressIndicator);
        vBox.setAlignment(Pos.CENTER);
        root.setCenter(vBox);
        root.getStylesheets().add(DroneLaunch.STYLE_FILE);
        Scene scene = new Scene(root, 600, 300);
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
