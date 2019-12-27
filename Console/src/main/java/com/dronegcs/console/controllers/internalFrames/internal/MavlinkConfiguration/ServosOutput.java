package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.dronegcs.mavlink.is.drone.parameters.Parameter;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmFrameTypes;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_FRAME_TYPE;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_TYPE;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class ServosOutput implements Initializable, DroneInterfaces.OnDroneListener {

    static Frame FRAME_X_4 = new Frame("Quad X", ApmFrameTypes.QUAD_X, new Engine[]{
            Engine.build(1, 45 - 90, false),
            Engine.build(3, -45 - 90, true),
            Engine.build(2, -135 - 90, false),
            Engine.build(4, 135 - 90, true),
    });

    static Frame FRAME_X_4_DOUBLE = new Frame("OCTO QUAD X8", ApmFrameTypes.OCTO_QUAD_X_8, new Engine[]{
            Engine.build(1, 45 - 90, false),
            Engine.build(2, -45 - 90, true),
            Engine.build(3, -135 - 90, false),
            Engine.build(4, 135 - 90, true),
            Engine.build(6, 45 - 90, true),
            Engine.build(5, -45 - 90, false),
            Engine.build(8, -135 - 90, true),
            Engine.build(7, 135 - 90, false),
    });

    static Frame FRAME_X_6 = new Frame("HEXA X", ApmFrameTypes.HEX_X, new Engine[]{
            Engine.build(5, 30 - 90, false),
            Engine.build(3, -30 - 90, true),
            Engine.build(2, -90 - 90, false),
            Engine.build(6, -150 - 90, true),
            Engine.build(4, 150 - 90, false),
            Engine.build(1, 90 - 90, true),
    });

    static Frame FRAME_X_8 = new Frame("OCTO X", ApmFrameTypes.OCTO_X, new Engine[]{
            Engine.build(1, 22.5 - 90, true),
            Engine.build(5, -22.5 - 90, false),
            Engine.build(7, -67.5 - 90, true),
            Engine.build(6, -112.5 - 90, false),
            Engine.build(2, -157.5 - 90, true),
            Engine.build(4, 157.5 - 90, false),
            Engine.build(8, 112.5 - 90, true),
            Engine.build(3, 67.5 - 90, false),
    });

    static Frame FRAME_V_TAIL = new Frame("QUAD V", ApmFrameTypes.QUAD_V_TAIL, new Engine[]{
            Engine.build(1, 60 - 90, false, 1.2),
            Engine.build(3, -60 - 90, true, 1.2),
            Engine.build(2, -150 - 90, false),
            Engine.build(4, 150 - 90, true),
    });

    static Frame FRAME_PLUS_4 = new Frame("QUAD +", ApmFrameTypes.QUAD_PLUS, new Engine[]{
            Engine.build(3, 0 - 90, true),
            Engine.build(2, -90 - 90, false),
            Engine.build(4, -180 - 90, true),
            Engine.build(1, 90 - 90, false),
    });

    static Frame FRAME_PLUS_6 = new Frame("HEXA +", ApmFrameTypes.HEX_PLUS, new Engine[]{
            Engine.build(1, 0 - 90, true),
            Engine.build(5, -60 - 90, false),
            Engine.build(3, -120 - 90, true),
            Engine.build(2, 180 - 90, false),
            Engine.build(6, 120 - 90, true),
            Engine.build(4, 60 - 90, false),
    });

    static Frame FRAME_PLUS_8 = new Frame("OCTO +", ApmFrameTypes.OCTO_PLUS, new Engine[]{
            Engine.build(1, 0 - 90, true),
            Engine.build(5, -45 - 90, false),
            Engine.build(7, -90 - 90, true),
            Engine.build(6, -135 - 90, false),
            Engine.build(2, 180 - 90, true),
            Engine.build(4, 135 - 90, false),
            Engine.build(8, 90 - 90, true),
            Engine.build(3, 45 - 90, false),
    });

    static Frame FRAME_H_4 = new Frame("QUAD H", ApmFrameTypes.QUAD_H, new Engine[]{
            Engine.build(1, 45 - 90, true),
            Engine.build(3, -45 - 90, false),
            Engine.build(4, 135 - 90, false),
            Engine.build(2, -135 - 90, true),
    });

    private static final Frame[] frames = new Frame[]{
            FRAME_X_4,
            FRAME_X_4_DOUBLE,
            FRAME_X_6,
            FRAME_X_8,
            FRAME_H_4,
            FRAME_V_TAIL,
            FRAME_PLUS_4,
            FRAME_PLUS_6,
            FRAME_PLUS_8
    };

    @Autowired
    public Drone drone;

    @FXML
    public Pane rootServoOutput;

    @Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
    private LoggerDisplayerSvc loggerDisplayerSvc;


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
                    imageView.setRotate(imageView.getRotate() + Math.pow(motor[engine.id-1]/1000.0,5));
                else
                    imageView.setRotate(imageView.getRotate() - Math.pow(motor[engine.id-1]/1000.0,5));
            }
        }

        List<Label> engineLabels = new ArrayList();
        engineLabels.addAll(Arrays.asList(lblEngine1, lblEngine2, lblEngine3, lblEngine4, lblEngine5, lblEngine6, lblEngine7, lblEngine8));

        for (int i = 0 ; i < motor.length ; i++) {
            if (i < enginesAmount)
                engineLabels.get(i).setText("Engine " + (i+1) + " - " + motor[i] + "    ");
            else
                engineLabels.get(i).setText("");
        }
    }));

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType droneEventsType, Drone drone) {
        switch (droneEventsType) {
            case RC_OUT:
                for (int i = 0 ; i < motor.length ; i++)
                    motor[i] = drone.getRC().out[i];
                break;
        }
    }

    public void handleFrameSelect(ActionEvent actionEvent) {
        Frame selected = (Frame) cbFrameType.getValue();
        loadGraphic(selected);
    }

    static class Frame {

        private final Engine[] engines;
        private final ApmFrameTypes apmFrameTypes;
        private final String name;

        public Frame(String name, ApmFrameTypes apmFrameTypes, Engine[] engines) {
            this.engines = engines;
            this.apmFrameTypes = apmFrameTypes;
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



    private void displaceEngine(Node imageView, double angle, double radius) {
        imageView.setTranslateX(radius * Math.cos(Math.toRadians(angle)));
        imageView.setTranslateY(radius * Math.sin(Math.toRadians(angle)));
    }

    static int[] motor = new int[]{0,0,0,0,0,0,0,0};

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (drone == null || drone.getParameters() == null || drone.getType().getDroneType() == MAV_TYPE.MAV_TYPE_GENERIC) {
            loggerDisplayerSvc.logGeneral("Drone isn't connected / synced");
            return;
        }

        if (drone.getType().isPlane()) {
            loggerDisplayerSvc.logGeneral("Plane frame tuning is not relevant");
            return;
        }

        Parameter parameter = drone.getParameters().getParameter("FRAME");
        if (parameter == null) {
            loggerDisplayerSvc.logGeneral("Drone parameter not found");
            return;
        }

        rootServoOutput.setVisible(true);
        MAV_FRAME_TYPE mav_frame_type = MAV_FRAME_TYPE.getFrameType(parameter.getValue().intValue());
        Frame currentFrame = null;
        for (Frame frame : frames) {
            if (drone.getType().getDroneType() == frame.apmFrameTypes.getDroneType()) {
                cbFrameType.getItems().add(frame);
                if (frame.apmFrameTypes.getFrameType() == mav_frame_type) {
                    // found current frame
                    currentFrame = frame;
                }
            }
        }

        cbFrameType.setValue(currentFrame);
        loadGraphic(currentFrame);

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void loadGraphic(Frame frame) {
        double centerX = body.getLayoutX() + body.getWidth()/2;
        double centerY = body.getLayoutY() + body.getHeight()/2;
        modelGraphic.getChildren().removeAll(modelGraphic.getChildren());

        Image imageCW = new Image(getClass().getResource("/com/dronegcs/console/guiImages/motors/motor-cw-75px.png").toExternalForm());
        Image imageCCW = new Image(getClass().getResource("/com/dronegcs/console/guiImages/motors/motor-ccw-75px.png").toExternalForm());
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

    @PostConstruct
    public void init() {
        drone.addDroneListener(this);
    }
}
