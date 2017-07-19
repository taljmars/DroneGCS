package com.dronegcs.console.controllers;

import com.dronegcs.console_plugin.flightControllers.KeyBoardController;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by taljmars on 3/13/17.
 */
@ComponentScan("com.dronegcs.console_plugin")
@Component
public class GuiAppConfig implements EventHandler<WindowEvent> {
    private final static Logger LOGGER = LoggerFactory.getLogger(GuiAppConfig.class);
//    private static final int WIDTH = 800;
//    private static final int HEIGHT = 650;
//    private static final String STYLE_FILE = "/com/dronegcs/console/application.css";

    @Autowired
    private ConfigurableApplicationContext applicationContext;
//    @Autowired
//    private KeyBoardController keyBoardController;


    
    private Stage stage;

    public void setPrimaryStage(Stage primaryStage) {
        this.stage = primaryStage;
    }

//    public void showMainScreen() {
//        Parent root = (Parent) load("/com/dronegcs/console/views/DashboardView.fxml");
////        root.setStyle("-fx-background-color: whitesmoke;");
//        root.getStylesheets().add(STYLE_FILE);
//        Scene scene = new Scene(root, WIDTH, HEIGHT);
//        //scene.getStylesheets().add("talma.css");
//        scene.setOnKeyPressed(keyBoardController);
//        stage.setResizable(false);
//        stage.setScene(scene);
//        stage.setMaximized(true);
//        stage.show();
//        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                applicationContext.close();
//            }
//        });
//    }

    public Stage getRootStage() {
        return stage;
    }

    private FXMLLoader getFXMLLoaderForUrl(String url) {

        FXMLLoader fxmlloader = new FXMLLoader();
        URL location = getClass().getResource(url);
        fxmlloader.setLocation(location);
        fxmlloader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> clazz) {
                Object obj = applicationContext.getBean(clazz);
                String resultData;
                if (obj != null) {
                    resultData = "[SUCCESS :'" + obj + "']";
                } else {
                    resultData = "[FAIL]";
                }
                LOGGER.info("Fetch bean name '{}' {}", clazz, resultData);

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
        } catch (IOException e) {
            LOGGER.error("Failed to load configuration", e);
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
        } catch (IOException e) {
            LOGGER.error("Failed to load internal frames", e);
            return null;
        }
    }

    @Override
    public void handle(WindowEvent event) {
        applicationContext.close();
    }
}

