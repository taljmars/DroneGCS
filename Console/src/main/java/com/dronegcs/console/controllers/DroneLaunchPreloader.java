package com.dronegcs.console.controllers;

import com.db.persistence.scheme.LoginResponse;
import com.db.persistence.scheme.RegistrationResponse;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static com.db.persistence.scheme.LoginLogoutStatus.OK;
import static com.dronegcs.console.controllers.DroneLaunchPreloader.PreloaderMode.LOGIN;
import static com.dronegcs.console.controllers.DroneLaunchPreloader.PreloaderMode.OFFLINE;
import static com.dronegcs.console.controllers.DroneLaunchPreloader.PreloaderMode.SIGNUP;

public class DroneLaunchPreloader extends Preloader implements EventHandler<KeyEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(DroneLaunchPreloader.class);

    //Don't deliver this - only for development
//    private boolean WA_FOR_LOGIN = false;
    private boolean WA_FOR_LOGIN = true;

    @Override
    public void handle(KeyEvent event) {
        if (!event.getCode().equals(KeyCode.ENTER))
            return;

        // 'Enter' key was down
        System.out.println("Enter");
    }

    public interface LoginLoader {
        LoginResponse handleLogin(String userName, String password, String server, int port);
        RegistrationResponse handleRegisterNewUser(String userName, String password, String server, int port);
        void handleOffline(String username);
    }

    private final static int PAGE_WIDTH = 600;
    private final static int PAGE_HIEGHT = 300;

    private Stage preloaderStage;
    private LoginLoader loginLoader;
    private Label status;

    private static final String backgroundDefinitions = "-fx-background-image: url(/com/dronegcs/console/guiImages/background.png); -fx-background-size: cover;";
    private static final String style = "-fx-bounds-type: logical_vertical_center; -fx-font-smoothing-type: lcd;";
    private static final Font font = new Font("Aharoni", 32);
    private static final Font versionFont = new Font("Aharoni", 10);
    private static final Label label = new Label("Drone GCS");
    private static final Label versionlabel = new Label("unknown");
    {
        versionlabel.setStyle(style);
        versionlabel.setFont(versionFont);
        label.setStyle(style);
        label.setFont(font);
    }

    public void start(Stage primaryStage) throws Exception {
        InputStream inputStream = null;
        try {
            inputStream = DroneLaunchPreloader.class.getClassLoader().getResourceAsStream("version");
            byte[] versionBuffer = new byte[32];
            inputStream.read(versionBuffer);
            String version = new String(versionBuffer);
            versionlabel.setText("(v1." + version.trim() + ")");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        this.preloaderStage = primaryStage;
        GUISettings._WIDTH.set((int) Screen.getPrimary().getVisualBounds().getMaxX());
        GUISettings._HEIGHT.set((int) Screen.getPrimary().getVisualBounds().getMaxY());
        BorderPane root = new BorderPane();
        VBox vBox = new VBox();
        root.setStyle(backgroundDefinitions);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(20, 20);
        vBox.getChildren().addAll(label, versionlabel, progressIndicator);
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
        OFFLINE,
        SIGNUP
    }

    private void loadLoginScreen(PreloaderMode mode, String message) {

        BorderPane root = new BorderPane();
        VBox vBox = new VBox();
        root.setStyle(backgroundDefinitions);

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register User");
        Button signingBtn = new Button("Register");
        Button offlineModeBtn = new Button("Offline Mode");
        Button exitBtn = new Button("Exit");

        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "localhost",
                        "127.0.0.1",
                        "drone-server.us-east-2.elasticbeanstalk.com"
                );
        final ComboBox serverIp = new ComboBox(options);
        serverIp.setValue(options.get(WA_FOR_LOGIN ? 0 : 1));
//        TextField serverIp = new TextField(WA_FOR_LOGIN ? "localhost" : "");
        TextField serverPort = new TextField("9024");

        TextField userName = new TextField(WA_FOR_LOGIN ? "admin" : "");
        userName.setAlignment(Pos.CENTER);
        userName.setMaxWidth(PAGE_WIDTH/2);
        PasswordField password = new PasswordField();
        password.setText(WA_FOR_LOGIN ? "admin" : "");
        password.setMaxWidth(PAGE_WIDTH/2);
        password.setAlignment(Pos.CENTER);
        password.setOnKeyReleased(key -> {
            if (!key.getCode().equals(KeyCode.ENTER))
                return;

            loginBtn.fire();
        });
        PasswordField password2 = new PasswordField();
        password2.setMaxWidth(PAGE_WIDTH/2);
        password2.setAlignment(Pos.CENTER);

        status = new Label(message);

        loginBtn.setOnAction((actionEvent) -> {
            LOGGER.debug("Trying to login to " + serverIp.getValue() + ":" + serverPort.getText() + " with user " + userName.getText());
            LoginResponse loginRestResponse = loginLoader.handleLogin(userName.getText(), password.getText(), serverIp.getValue().toString(), Integer.parseInt(serverPort.getText()));
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

        offlineModeBtn.setOnAction((actionEvent) -> {
            loginLoader.handleOffline(userName.getText());
            preloaderStage.hide();
        });

        signingBtn.setOnAction((actionEvent) -> {
            if (!password.getText().equals(password2.getText())) {
                status.setText("Password must be identical !");
                return;
            }

            RegistrationResponse registrationResponse = loginLoader.handleRegisterNewUser(userName.getText(), password.getText(), serverIp.getValue().toString(), Integer.parseInt(serverPort.getText()));
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

        HBox serverBox = new HBox();
//        serverIp.setAlignment(Pos.CENTER);
        serverIp.setMaxWidth(PAGE_WIDTH/2 - 10);
        serverPort.setAlignment(Pos.CENTER);
        serverPort.setMaxWidth(PAGE_WIDTH/4 - 10);

        switch (mode) {
            case LOGIN:
                hBox.getChildren().addAll(loginBtn, registerBtn, offlineModeBtn, exitBtn);
                serverBox.getChildren().addAll(serverIp, serverPort);
                vBox.getChildren().addAll(label, userName, password, serverBox, hBox, status);
                break;
            case SIGNUP:
                hBox.getChildren().addAll(signingBtn, exitBtn);
                serverBox.getChildren().addAll(serverIp, serverPort);
                vBox.getChildren().addAll(label, userName, password, password2, serverBox, hBox, status, serverIp, serverPort);
                break;
            case OFFLINE:
                hBox.getChildren().addAll(offlineModeBtn, exitBtn);
                serverBox.getChildren().addAll(serverIp, serverPort);
                vBox.getChildren().addAll(label, userName, password, serverBox, hBox, status);
                break;
        }

        serverBox.setSpacing(10);
        serverBox.setAlignment(Pos.CENTER);

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
