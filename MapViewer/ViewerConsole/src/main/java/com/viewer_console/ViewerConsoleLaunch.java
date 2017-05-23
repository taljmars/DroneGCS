package com.viewer_console;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ViewerConsoleLaunch extends AbstractJavaFxApplicationSupport {

    @Autowired
    private AppConfig appConfig;

    @Override
    public void start(Stage primaryStage) {
        Parent root = (Parent) appConfig.load("com/views/view.fxml");
        root.setStyle("-fx-background-color: whitesmoke;");
        Scene scene = new Scene(root, 650, 550);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launchApp(ViewerConsoleLaunch.class, args);
    }
}
