package com.dronegcs.console.controllers;

import com.db.persistence.scheme.LoginResponse;
import com.db.persistence.scheme.RegistrationResponse;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.db.persistence.scheme.LoginLogoutStatus.OK;
import static com.dronegcs.console.controllers.DroneLaunchPreloader.PreloaderMode.LOGIN;
import static com.dronegcs.console.controllers.DroneLaunchPreloader.PreloaderMode.SIGNUP;

public class DroneLaunchPreloader extends Preloader {

    private final static Logger LOGGER = LoggerFactory.getLogger(DroneLaunchPreloader.class);

    public interface LoginLoader {
        LoginResponse handleLogin(String userName, String password);
        RegistrationResponse handleRegisterNewUser(String userName, String password);
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
                loadLoginScreen(LOGIN, "");
                loginLoader = (LoginLoader) stateChangeNotification.getApplication();
            });
        }
    }

    public enum PreloaderMode {
        LOGIN,
        SIGNUP
    }

    private void loadLoginScreen(PreloaderMode mode, String message) {


        BorderPane root = new BorderPane();
        VBox vBox = new VBox();
        root.setStyle(backgroundDefinitions);
        TextField userName = new TextField();
        userName.setAlignment(Pos.CENTER);
        userName.setMaxWidth(PAGE_WIDTH/2);
        TextField password = new TextField();
        password.setMaxWidth(PAGE_WIDTH/2);
        password.setAlignment(Pos.CENTER);
        TextField password2 = new TextField();
        password2.setMaxWidth(PAGE_WIDTH/2);
        password2.setAlignment(Pos.CENTER);

        status = new Label(message);
        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register User");
        Button signingBtn = new Button("Register");
        Button exitBtn = new Button("Exit");

        loginBtn.setOnAction((actionEvent) -> {
            LoginResponse loginRestResponse = loginLoader.handleLogin(userName.getText(), password.getText());
            Integer loginStatus = loginRestResponse.getReturnCode();
            if (loginStatus.equals(OK)) {
                preloaderStage.hide();
            }
            else {
                status.setText(loginRestResponse.getMessage());
            }
        });

        registerBtn.setOnAction((actionEvent) -> {
            preloaderStage.hide();
            loadLoginScreen(SIGNUP, "");
        });

        signingBtn.setOnAction((actionEvent) -> {
            if (!password.getText().equals(password2.getText())) {
                status.setText("Password must be identical !");
                return;
            }

            RegistrationResponse registrationResponse = loginLoader.handleRegisterNewUser(userName.getText(), password.getText());
            Integer signingStatus = registrationResponse.getReturnCode();
            if (signingStatus.equals(OK)) {
                preloaderStage.hide();
                loadLoginScreen(LOGIN, "Try to login with your new user");
            }
            else {
                status.setText(registrationResponse.getMessage());
            }
        });

        exitBtn.setOnAction((actionEvent) -> System.exit(0));

        HBox hBox = new HBox();
        switch (mode) {
            case LOGIN:
                hBox.getChildren().addAll(loginBtn, registerBtn, exitBtn);
                vBox.getChildren().addAll(label, userName, password, hBox, status);
                break;
            case SIGNUP:
                hBox.getChildren().addAll(signingBtn, exitBtn);
                vBox.getChildren().addAll(label, userName, password, password2, hBox, status);
                break;
        }

        vBox.setSpacing(20);
        vBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);
        root.setCenter(vBox);
        root.getStylesheets().add(DroneLaunch.STYLE_FILE);
        Scene scene = new Scene(root, PAGE_WIDTH, PAGE_HIEGHT);
//                preloaderStage.initStyle(StageStyle.TRANSPARENT);
        preloaderStage.setScene(scene);
        preloaderStage.show();
        preloaderStage.toFront();
    }
}
