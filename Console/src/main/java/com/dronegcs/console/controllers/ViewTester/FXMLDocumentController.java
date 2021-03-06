package com.dronegcs.console.controllers.ViewTester;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FXMLDocumentController implements Initializable {


    @FXML public Rectangle body;
    @FXML public AnchorPane modelGraphic;
    @FXML public ComboBox cbFrameType;

    @FXML public Label lblEngine1;
    @FXML public Label lblEngine2;
    @FXML public Label lblEngine3;
    @FXML public Label lblEngine4;
    @FXML public Label lblEngine5;
    @FXML public Label lblEngine6;
    @FXML public Label lblEngine7;
    @FXML public Label lblEngine8;

    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), actionEvent  ->
    {
        int enginesAmount = 0;
        for (Node imageView : modelGraphic.getChildren()) {
            if (imageView instanceof ImageView) {
                Engine engine = (Engine) imageView.getUserData();
                enginesAmount++;
                if (engine.cw)
                    imageView.setRotate(imageView.getRotate() + Math.pow(FXMLDocumentController.motor[engine.id-1]/1000.0,5));
                else
                    imageView.setRotate(imageView.getRotate() - Math.pow(FXMLDocumentController.motor[engine.id-1]/1000.0,5));
            }
        }

        List<Label> engineLabels = new ArrayList();
        engineLabels.addAll(Arrays.asList(lblEngine1, lblEngine2, lblEngine3, lblEngine4, lblEngine5, lblEngine6, lblEngine7, lblEngine8));

        for (int i = 0 ; i < motor.length ; i++) {
            if (motor[i] != 0 && i < enginesAmount)
                engineLabels.get(i).setText("Engine " + (i+1) + " - " + motor[i] + "    ");
            else
                engineLabels.get(i).setText("");
        }
    }));

    public void handleFrameSelect(ActionEvent actionEvent) {
        Frame selected = (Frame) cbFrameType.getValue();
        loadGraphic(selected);
    }

    static class Frame {

        private final Engine[] engines;
        private final String name;

        public Frame(String name, Engine[] engines) {
            this.engines = engines;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static class Engine {
        int id;
        double angle;
        boolean cw;
        double radius_ratio;

        public Engine(int engine_id, double position_angle, boolean is_clockwise, double radius_ratio) {
            this.id = engine_id;
            this.angle = position_angle;
            this.cw = is_clockwise;
            this.radius_ratio = radius_ratio;
        }

        public static Engine build(int engine_id, double position_angle, boolean is_clockwise) {
            return new Engine(engine_id, position_angle, is_clockwise, 1);
        }

        public static Engine build(int engine_id, double position_angle, boolean is_clockwise, double radius_ratio) {
            return new Engine(engine_id, position_angle, is_clockwise, radius_ratio);
        }
    }

    Frame FRAME_X_4 = new Frame("Quad X", new Engine[]{
            Engine.build(1, 45 - 90, false),
            Engine.build(3, -45 - 90, true),
            Engine.build(2, -135 - 90, false),
            Engine.build(4, 135 - 90, true),
    });

    Frame FRAME_X_4_DOUBLE = new Frame("OCTO QUAD X8", new Engine[]{
            Engine.build(1, 45 - 90, false),
            Engine.build(2, -45 - 90, true),
            Engine.build(3, -135 - 90, false),
            Engine.build(4, 135 - 90, true),
            Engine.build(6, 45 - 90, true),
            Engine.build(5, -45 - 90, false),
            Engine.build(8, -135 - 90, true),
            Engine.build(7, 135 - 90, false),
    });

    Frame FRAME_X_6 = new Frame("HEXA QUAD X", new Engine[]{
            Engine.build(5, 30 - 90, false),
            Engine.build(3, -30 - 90, true),
            Engine.build(2, -90 - 90, false),
            Engine.build(6, -150 - 90, true),
            Engine.build(4, 150 - 90, false),
            Engine.build(1, 90 - 90, true),
    });

    Frame FRAME_X_8 = new Frame("OCTO X", new Engine[]{
            Engine.build(1, 22.5 - 90, true),
            Engine.build(5, -22.5 - 90, false),
            Engine.build(7, -67.5 - 90, true),
            Engine.build(6, -112.5 - 90, false),
            Engine.build(2, -157.5 - 90, true),
            Engine.build(4, 157.5 - 90, false),
            Engine.build(8, 112.5 - 90, true),
            Engine.build(3, 67.5 - 90, false),
    });

    Frame FRAME_V_TAIL = new Frame("QUAD V", new Engine[]{
            Engine.build(1, 60 - 90, false, 1.2),
            Engine.build(3, -60 - 90, true, 1.2),
            Engine.build(2, -150 - 90, false),
            Engine.build(4, 150 - 90, true),
    });

    Frame FRAME_PLUS_4 = new Frame("QUAD +", new Engine[]{
            Engine.build(3, 0 - 90, true),
            Engine.build(2, -90 - 90, false),
            Engine.build(4, -180 - 90, true),
            Engine.build(1, 90 - 90, false),
    });

    Frame FRAME_PLUS_6 = new Frame("HEXA +", new Engine[]{
            Engine.build(1, 0 - 90, true),
            Engine.build(5, -60 - 90, false),
            Engine.build(3, -120 - 90, true),
            Engine.build(2, 180 - 90, false),
            Engine.build(6, 120 - 90, true),
            Engine.build(4, 60 - 90, false),
    });

    Frame FRAME_PLUS_8 = new Frame("OCTO +", new Engine[]{
            Engine.build(1, 0 - 90, true),
            Engine.build(5, -45 - 90, false),
            Engine.build(7, -90 - 90, true),
            Engine.build(6, -135 - 90, false),
            Engine.build(2, 180 - 90, true),
            Engine.build(4, 135 - 90, false),
            Engine.build(8, 90 - 90, true),
            Engine.build(3, 45 - 90, false),
    });

    Frame FRAME_H_4 = new Frame("QUAD H", new Engine[]{
            Engine.build(1, 45 - 90, true),
            Engine.build(3, -45 - 90, false),
            Engine.build(4, 135 - 90, false),
            Engine.build(2, -135 - 90, true),
    });

    private void displaceEngine(Node imageView, double angle, double radius) {
        imageView.setTranslateX(radius * Math.cos(Math.toRadians(angle)));
        imageView.setTranslateY(radius * Math.sin(Math.toRadians(angle)));
    }

    static int[] motor = new int[]{1500,1500,1500,1500,1500,1500,1500,1500};
    int lastId = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        cbFrameType.getItems().addAll(FRAME_X_4, FRAME_X_4_DOUBLE, FRAME_X_6, FRAME_X_8, FRAME_H_4, FRAME_V_TAIL, FRAME_PLUS_4, FRAME_PLUS_6, FRAME_PLUS_8);
        cbFrameType.setValue(FRAME_X_4);

        loadGraphic(FRAME_X_4);

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void loadGraphic(Frame frame) {
        double centerX = body.getLayoutX() + body.getWidth()/2;
        double centerY = body.getLayoutY() + body.getHeight()/2;
        modelGraphic.getChildren().removeAll(modelGraphic.getChildren());

        Image imageCW = new Image(getClass().getResource("/com/dronegcs/console/controllers/ViewTester/motor-cw-75px.png").toExternalForm());
        Image imageCCW = new Image(getClass().getResource("/com/dronegcs/console/controllers/ViewTester/motor-ccw-75px.png").toExternalForm());
        for (Engine engine : frame.engines) {
            ImageView imageView;
            Label label = new Label(engine.id + "");

            if (engine.cw)
                imageView = new ImageView(imageCW);
            else
                imageView = new ImageView(imageCCW);

            imageView.setLayoutX(centerX - imageCW.getWidth()/2);
            imageView.setLayoutY(centerY - imageCW.getHeight()/2);
            displaceEngine(imageView, engine.angle, imageCW.getWidth()*2*engine.radius_ratio);
            imageView.setUserData(engine);


            label.setLayoutX(centerX - imageCW.getWidth()/2);
            label.setLayoutY(centerY - imageCW.getHeight()/2);
            displaceEngine(label, engine.angle, imageCW.getWidth()*2*engine.radius_ratio);

            Line line = new Line(centerX, centerY, centerX+imageView.getTranslateX()*0.7 , centerY + imageView.getTranslateY()*0.7);

            modelGraphic.getChildren().addAll(label, imageView, line);
        }
        modelGraphic.getChildren().add(body);
    }
}
