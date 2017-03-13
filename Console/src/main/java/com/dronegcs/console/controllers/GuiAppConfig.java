package com.dronegcs.console.controllers;

import com.dronegcs.gcsis.devices.KeyBoardController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.dronegcs.console.controllers.AppConfig.context;

/**
 * Created by taljmars on 3/13/17.
 */
@Configuration
@Lazy
public class GuiAppConfig
{
    public static final int WIDTH = 800;
    public static final int HEIGHT = 650;
    public static final String STYLE_FILE = "/com/dronegcs/console/application.css";

    private Stage stage;
    private Scene scene;
    private StackPane root;

    public void setPrimaryStage(Stage primaryStage) {
        this.stage = primaryStage;
    }

    public void showMainScreen() {
        Parent root = (Parent) getScreen("/com/dronegcs/console/views/DashboardView.fxml");
        root.setStyle("-fx-background-color: whitesmoke;");
        root.getStylesheets().add(STYLE_FILE);
        scene = new Scene(root, WIDTH, HEIGHT);
        KeyBoardController keyboardController = context.getBean(KeyBoardController.class);
        scene.setOnKeyPressed(keyboardController);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private Object getScreen(String url) {
        return load(url);
    }

    private FXMLLoader getFXMLLoaderForUrl(String url) {

        FXMLLoader fxmlloader = new FXMLLoader();
        URL location = getClass().getResource(url);
        fxmlloader.setLocation(location);
        fxmlloader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> clazz) {
                System.out.print("Fetch bean name '" + clazz + "' ");
                Object obj = context.getBean(clazz);
                if (obj != null)
                    System.out.println("[SUCCESS :'" + obj + "']");
                else
                    System.err.println("[FAIL]");
                return obj;
            }
        });

        return fxmlloader;
    }

    public Object load(String url) {
        try {
            InputStream fxmlStream = AppConfig.class.getResourceAsStream(url);
            FXMLLoader fxmlLoader = getFXMLLoaderForUrl(url);
            return fxmlLoader.load(fxmlStream);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Node loadInternalFrame(String internalFrameUrl, double width, double height) {
        try {
            InputStream fxmlStream = AppConfig.class.getResourceAsStream(internalFrameUrl);
            FXMLLoader fxmlLoader = getFXMLLoaderForUrl(internalFrameUrl);
            fxmlLoader.getNamespace().put("prefWidth", width);
            fxmlLoader.getNamespace().put("prefHeight", height);
            return fxmlLoader.load(fxmlStream);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

