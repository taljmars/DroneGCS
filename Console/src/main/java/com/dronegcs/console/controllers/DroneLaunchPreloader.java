package com.dronegcs.console.controllers;

import com.db.persistence.scheme.LoginResponse;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.db.persistence.scheme.LoginLogoutStatus.OK;

public class DroneLaunchPreloader extends Preloader {

    private final static Logger LOGGER = LoggerFactory.getLogger(DroneLaunchPreloader.class);

    public interface LoginLoader {
        LoginResponse handleLogin(String userName, String password);
    }

    private final static int PAGE_WIDTH = 600;
    private final static int PAGE_HIEGHT = 300;

    private Stage preloaderStage;
    private LoginLoader loginLoader;
    private Label status;

    private static final String backgroundDefinitions = "-fx-background-image: url(/com/dronegcs/console/guiImages/background.png); -fx-background-size: cover;";
    private static final String style = "-fx-bounds-type: logical_vertical_center; -fx-font-smoothing-type: lcd;";
    private static final Font font = new Font("Aharoni", 32);
    private static final Label label = new Label("Drone GCS");
    {
        label.setStyle(style);
        label.setFont(font);
    }

    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;
        BorderPane root = new BorderPane();
        VBox vBox = new VBox();
        root.setStyle(backgroundDefinitions);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(20, 20);
        vBox.getChildren().addAll(label, progressIndicator);
        vBox.setAlignment(Pos.CENTER);
        root.setCenter(vBox);
        root.getStylesheets().add(DroneLaunch.STYLE_FILE);
        Scene scene = new Scene(root, PAGE_WIDTH, PAGE_HIEGHT);
        preloaderStage.initStyle(StageStyle.TRANSPARENT);
        preloaderStage.setScene(scene);
        preloaderStage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            //preloaderStage.hide();
            Platform.runLater(() -> {
                preloaderStage.hide();

                BorderPane root = new BorderPane();
                VBox vBox = new VBox();
                root.setStyle(backgroundDefinitions);
                TextField userName = new TextField();
                userName.setAlignment(Pos.CENTER);
                userName.setMaxWidth(PAGE_WIDTH/2);
                TextField password = new TextField();
                password.setMaxWidth(PAGE_WIDTH/2);
                password.setAlignment(Pos.CENTER);

                status = new Label();
                Button btn = new Button("Login");
                btn.setOnAction((actionEvent) -> {
                    LoginResponse loginRestResponse = loginLoader.handleLogin(userName.getText(), password.getText());
                    Integer loginStatus = loginRestResponse.getReturnCode();
                    if (loginStatus.equals(OK)) {
                        preloaderStage.hide();
                    }
                    else {
                        status.setText(loginRestResponse.getMessage());
                    }
                });
                vBox.getChildren().addAll(label, userName, password, btn, status);
                vBox.setSpacing(20);
                vBox.setAlignment(Pos.CENTER);
                root.setCenter(vBox);
                root.getStylesheets().add(DroneLaunch.STYLE_FILE);
                Scene scene = new Scene(root, PAGE_WIDTH, PAGE_HIEGHT);
//                preloaderStage.initStyle(StageStyle.TRANSPARENT);
                preloaderStage.setScene(scene);
                preloaderStage.show();
                preloaderStage.toFront();

                loginLoader = (LoginLoader) stateChangeNotification.getApplication();
            });
        }
    }
}
