package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console_plugin.flightControllers.KeyBoardController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.*;

/**
 * Created by taljmars on 6/17/2017.
 */
@Component
public class KeyboardControllerTuning implements Initializable {

    // Setter
    private interface TuneSetter {
        void set(Integer val);
    }

    // Getter
    private interface TuneGetter {
        Integer get();
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(KeyboardControllerTuning.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get keyboard controller")
    private KeyBoardController keyBoardController;

    @NotNull @FXML private ScrollBar _STABILIZER_CYCLE;
    @NotNull @FXML private ScrollBar _TRIM_ANGLE;
    @NotNull @FXML private ScrollBar _MIN_PWM_RANGE;
    @NotNull @FXML private ScrollBar _MAX_PWM_RANGE;
    @NotNull @FXML private ScrollBar _MIN_PWM_ANGLE;
    @NotNull @FXML private ScrollBar _MAX_PWM_ANGLE;
    @NotNull @FXML private ScrollBar _ROLL_STEP;
    @NotNull @FXML private ScrollBar _PITCH_STEP;
    @NotNull @FXML private ScrollBar _THR_STEP;
    @NotNull @FXML private ScrollBar _YAW_STEP;
    @NotNull @FXML private ScrollBar _INIT_THR;

    private Map<ScrollBar, TuneSetter> setters;
    private Map<ScrollBar, TuneGetter> getters;
    private Set<ScrollBar> modifiedBars;

    @PostConstruct
    private void init() {
        setters = new HashMap<>();
        getters = new HashMap<>();
        modifiedBars = new HashSet<>();
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // Verify Clean
        setters.clear();
        getters.clear();
        modifiedBars.clear();

        // Settings
        setters.put(_STABILIZER_CYCLE, (val) -> keyBoardController.setStabilizeCycle(val));
        setters.put(_TRIM_ANGLE, (val) -> keyBoardController.setTrimAngle(val));
        setters.put(_MIN_PWM_RANGE, (val) -> keyBoardController.setMinPwmRange(val));
        setters.put(_MAX_PWM_RANGE, (val) -> keyBoardController.setMaxPwmRange(val));
        setters.put(_MIN_PWM_ANGLE, (val) -> keyBoardController.setMinPwmAngle(val));
        setters.put(_MAX_PWM_ANGLE, (val) -> keyBoardController.setMaxPwmAngle(val));
        setters.put(_ROLL_STEP, (val) -> keyBoardController.setRollStep(val));
        setters.put(_PITCH_STEP, (val) -> keyBoardController.setPitchStep(val));
        setters.put(_THR_STEP, (val) -> keyBoardController.setThrustStep(val));
        setters.put(_YAW_STEP, (val) -> keyBoardController.setYawStep(val));
        setters.put(_INIT_THR, (val) -> keyBoardController.setInitialThrust(val));

        // Getters
        getters.put(_STABILIZER_CYCLE, () -> keyBoardController.getStabilizeCycle());
        getters.put(_TRIM_ANGLE, () -> keyBoardController.getTrimAngle());
        getters.put(_MIN_PWM_RANGE, () -> keyBoardController.getMinPwmRange());
        getters.put(_MAX_PWM_RANGE, () -> keyBoardController.getMaxPwmRange());
        getters.put(_MIN_PWM_ANGLE, () -> keyBoardController.getMinPwmAngle());
        getters.put(_MAX_PWM_ANGLE, () -> keyBoardController.getMaxPwmAngle());
        getters.put(_ROLL_STEP, () -> keyBoardController.getRollStep());
        getters.put(_PITCH_STEP, () -> keyBoardController.getPitchStep());
        getters.put(_THR_STEP, () -> keyBoardController.getThrustStep());
        getters.put(_YAW_STEP, () -> keyBoardController.getYawStep());
        getters.put(_INIT_THR, () -> keyBoardController.getInitialThrust());

        refreshScrollBars();

        for (Map.Entry<ScrollBar, TuneGetter> tuple : getters.entrySet())
            tuple.getKey().valueProperty().addListener((observable, oldValue, newValue) -> signChange(tuple.getKey()));
    }

    private void refreshScrollBars() {
        Platform.runLater( () -> {
            for (Map.Entry<ScrollBar, TuneGetter> tuple : getters.entrySet())
                tuple.getKey().setValue(tuple.getValue().get());
        });
    }

    @FXML
    public void update(ActionEvent actionEvent) {
        LOGGER.debug("Going to update " + modifiedBars.size() + " value");
        for (ScrollBar scrollBar : modifiedBars) {
            TuneSetter setter = setters.get(scrollBar);
            LOGGER.debug("value of {} is {}", scrollBar, scrollBar.getValue());
            setter.set((int) scrollBar.getValue());
        }
    }

    @FXML
    public void refresh(ActionEvent actionEvent) {
        try {
            LOGGER.debug("Saving configuration to a file");
            keyBoardController.parse();
            refreshScrollBars();
        }
        catch (Exception e) {
            LOGGER.error("Failed to refresh", e);
        }
    }

    @FXML
    public void save(ActionEvent actionEvent) {
        try {
            LOGGER.debug("Saving configuration to a file");
            keyBoardController.dump();
        }
        catch (Exception e) {
            LOGGER.error("Failed to save", e);
        }
    }

    private void signChange(ScrollBar scrollBar) {
        if (modifiedBars.contains(scrollBar))
            return;

        LOGGER.debug("{} was modified", scrollBar);
        modifiedBars.add(scrollBar);
    }
}
