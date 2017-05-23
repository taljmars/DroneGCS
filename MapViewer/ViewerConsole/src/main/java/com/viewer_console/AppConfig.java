package com.viewer_console;

import com.gui.core.mapTree.CheckBoxViewTree;
import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@ComponentScan("com.gui.core.mapTree")
@ComponentScan("com.gui.core.mapViewer")
@Import(MapView.class)
@Configuration
public class AppConfig {
    private final static Logger LOGGER = LoggerFactory.getLogger(CheckBoxViewTree.class);

    @Autowired
    private ApplicationContext applicationContext;

    private FXMLLoader getFXMLLoaderForUrl(String url) {
        FXMLLoader fxmlloader = new FXMLLoader();
        URL location = getClass().getResource(url);
        fxmlloader.setLocation(location);
        fxmlloader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> clazz) {
                LOGGER.info("Fetch bean name '{}'", clazz);
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
            InputStream fxmlStream = this.getClass().getClassLoader().getResourceAsStream(url);
            FXMLLoader fxmlLoader = getFXMLLoaderForUrl(url);
            return fxmlLoader.load(fxmlStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
