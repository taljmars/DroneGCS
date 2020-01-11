package com.dronegcs.console.controllers.ViewTester;

import com.dronegcs.console.controllers.GuiAppConfig;
import com.dronegcs.console.controllers.internalFrames.InternalFrameMap;
import com.dronegcs.console.operations.OpGCSTerminationHandler;
import com.dronegcs.console_plugin.ConsolePluginConfig;
import com.mapviewer.gui.core.mapViewer.internal.LayerEditorView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;


@Import({GuiAppConfig.class, InternalFrameMap.class , OpGCSTerminationHandler.class,
        ConsolePluginConfig.class, LayerEditorView.class})
@Configuration
@ComponentScan(value = {
        "com.dronegcs.console",
        "com.dronegcs.console_plugin"
})
public class MyAppViewTester extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("FXMLDocument.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

}
