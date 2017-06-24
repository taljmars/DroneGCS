package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.dronegcs.mavlink.is.drone.parameters.Parameter;
import com.dronegcs.mavlink.is.drone.profiles.Parameters;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmCommands;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmModes;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmTuning;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.*;

import static com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_PARAM_TYPE.MAV_PARAM_TYPE_INT16;
import static com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_PARAM_TYPE.MAV_PARAM_TYPE_INT8;
import static com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_TYPE.MAV_TYPE_QUADROTOR;

/**
 * Created by taljmars on 6/17/2017.
 */
@Component
public class Modes implements Initializable, DroneInterfaces.OnParameterManagerListener, DroneInterfaces.OnDroneListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(Modes.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get drone")
    private Drone drone;

    @NotNull @FXML private ComboBox cbFltMode1;
    @NotNull @FXML private ComboBox cbFltMode2;
    @NotNull @FXML private ComboBox cbFltMode3;
    @NotNull @FXML private ComboBox cbFltMode4;
    @NotNull @FXML private ComboBox cbFltMode5;
    @NotNull @FXML private ComboBox cbFltMode6;

    @NotNull @FXML private ComboBox cbChannel6; // TUNE
    @NotNull @FXML private Spinner<Double> spChannel6Min;
    @NotNull @FXML private Spinner<Double> spChannel6Max;

    @NotNull @FXML private ComboBox cbChannel7;
    @NotNull @FXML private ComboBox cbChannel8;

    private static final String FLTMODE_FORMAT = "FLTMODE%d"; // Example: FLTMODE1
    private static final String CH_FORMAT = "CH%d_OPT"; // Example: CHA7_OPT
    private static final String TUNE = "TUNE";
    private static final String TUNE_HIGH = "TUNE_HIGH";
    private static final String TUNE_LOW = "TUNE_LOW";

    private Map<String, ComboBox> comboBoxFltModeMap = null;
    private Map<String, ComboBox> comboBoxCommandsMap = null;
    private Map<String, ComboBox> comboBoxTunningMap = null;

    @PostConstruct
    private void init() {
        drone.getParameters().addParameterListener(this);
        drone.addDroneListener(this);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        comboBoxFltModeMap = new HashMap<>();
        List<ComboBox> cmList = Arrays.asList(cbFltMode1, cbFltMode2, cbFltMode3, cbFltMode4, cbFltMode5, cbFltMode6);
        for (int i = 0; i < cmList.size(); i++)
            comboBoxFltModeMap.put(String.format(FLTMODE_FORMAT, (i + 1)), cmList.get(i));

        comboBoxCommandsMap = new HashMap<>();
        comboBoxCommandsMap.put(String.format(CH_FORMAT, 7), cbChannel7);
        comboBoxCommandsMap.put(String.format(CH_FORMAT, 8), cbChannel8);

        comboBoxTunningMap = new HashMap<>();
        comboBoxTunningMap.put(TUNE, cbChannel6);

        // Generate flight mode combo-boxs and keys
        Vector<ApmModes> flightModes = new Vector<ApmModes>();
        flightModes.addAll(FXCollections.observableArrayList(ApmModes.getModeList(MAV_TYPE_QUADROTOR)));
        loadComboBox(flightModes, comboBoxFltModeMap);

        // Generate command combo-boxs and keys
        Vector<ApmCommands> commands = new Vector<ApmCommands>();
        commands.addAll(FXCollections.observableArrayList(ApmCommands.getCommandsList()));
        loadComboBox(commands, comboBoxCommandsMap);

        // Generate tunning combo-boxs and keys
        Vector<ApmTuning> tunes = new Vector<ApmTuning>();
        tunes.addAll(FXCollections.observableArrayList(ApmTuning.getTuningList()));
        loadComboBox(tunes, comboBoxTunningMap);

        Optional optional = drone.getParameters().getParametersList().stream().filter(parameter -> parameter.name.equals(TUNE_HIGH)).findFirst();
        if (optional.isPresent())
            spChannel6Max.getValueFactory().setValue(Double.parseDouble(((Parameter)optional.get()).getValue()));

        optional = drone.getParameters().getParametersList().stream().filter(parameter -> parameter.name.equals(TUNE_LOW)).findFirst();
        if (optional.isPresent())
            spChannel6Min.getValueFactory().setValue(Double.parseDouble(((Parameter)optional.get()).getValue()));
    }

    private void loadComboBox(Vector<?> options, Map<String, ComboBox> comboBoxMap) {
        for (Map.Entry<String, ComboBox> entry : comboBoxMap.entrySet()) {
            entry.getValue().getItems().addAll(options);
            Optional optional = drone.getParameters().getParametersList().stream().filter(parameter -> parameter.name.equals(entry.getKey())).findFirst();
            if (optional.isPresent())
                entry.getValue().setValue(optional.get());
        }
    }

    private ApmTuning getTuning(Parameter parameter) {
        if (parameter == null)
            return null;
        return ApmTuning.getTune(Integer.parseInt(parameter.getValue()));
    }

    private static ApmModes getMode(Parameter parameter) {
        if (parameter == null)
            return null;
        return ApmModes.getMode(Integer.parseInt(parameter.getValue()), MAV_TYPE_QUADROTOR);
    }

    private static ApmCommands getCommand(Parameter parameter) {
        if (parameter == null)
            return null;
        return ApmCommands.getCommand(Integer.parseInt(parameter.getValue()));
    }

    @FXML
    public void refresh(ActionEvent actionEvent) {
        for (Map.Entry<String, ComboBox> entry : comboBoxFltModeMap.entrySet())
            drone.getParameters().ReadParameter(entry.getKey());

        for (Map.Entry<String, ComboBox> entry : comboBoxCommandsMap.entrySet())
            drone.getParameters().ReadParameter(entry.getKey());

        for (Map.Entry<String, ComboBox> entry : comboBoxTunningMap.entrySet())
            drone.getParameters().ReadParameter(entry.getKey());

        drone.getParameters().ReadParameter(TUNE_HIGH);
        drone.getParameters().ReadParameter(TUNE_LOW);
    }

    @FXML
    public void updateModes(ActionEvent actionEvent) {
        Parameters params = drone.getParameters();
        Parameter parameter = null;

        if (!drone.isConnectionAlive() || params == null) {
            LOGGER.debug("Drone is not connected, cannot get flight modes");
            return;
        }

        for (Map.Entry<String, ComboBox> entry : comboBoxFltModeMap.entrySet()) {
            parameter = new Parameter(entry.getKey(), ((ApmModes) entry.getValue().getValue()).getNumber(), MAV_PARAM_TYPE_INT8, "");
            System.err.println("Send updated flt param " + parameter);
            drone.getParameters().sendParameter(parameter);
        }

        for (Map.Entry<String, ComboBox> entry : comboBoxCommandsMap.entrySet()) {
            parameter = new Parameter(entry.getKey(), ((ApmCommands) entry.getValue().getValue()).getNumber(), MAV_PARAM_TYPE_INT8, "");
            System.err.println("Send updated cmd param " + parameter);
            drone.getParameters().sendParameter(parameter);
        }

        for (Map.Entry<String, ComboBox> entry : comboBoxTunningMap.entrySet()) {
            parameter = new Parameter(entry.getKey(), ((ApmTuning) entry.getValue().getValue()).getNumber(), 9, "");
            System.err.println("Send updated tune param " + parameter);
            drone.getParameters().sendParameter(parameter);
        }

        parameter = new Parameter(TUNE_HIGH, spChannel6Max.getValue(), MAV_PARAM_TYPE_INT16, "");
        drone.getParameters().sendParameter(parameter);

        parameter = new Parameter(TUNE_LOW, spChannel6Min.getValue(), MAV_PARAM_TYPE_INT16, "");
        drone.getParameters().sendParameter(parameter);
    }

    @Override
    public void onBeginReceivingParameters() {}

    @Override
    public void onParameterReceived(Parameter parameter, int i, int i1) {
        Platform.runLater(() -> {
            if (comboBoxFltModeMap.keySet().contains(parameter.name)) {
                ApmModes mode = getMode(parameter);
                LOGGER.debug("Received flight mode = {}", mode);
                ((ComboBox<ApmModes>) comboBoxFltModeMap.get(parameter.name)).setValue(mode);
                return;
            }

            if (comboBoxCommandsMap.keySet().contains(parameter.name)) {
                ApmCommands cmd = getCommand(parameter);
                LOGGER.debug("Received command = {}", cmd);
                comboBoxCommandsMap.get(parameter.name).setValue(cmd);
                return;
            }

            if (comboBoxTunningMap.keySet().contains(parameter.name)) {
                ApmTuning tune = getTuning(parameter);
                LOGGER.debug("Received tune = {}", tune);
                comboBoxTunningMap.get(parameter.name).setValue(tune);
                return;
            }

            if (parameter.name.equals(TUNE_LOW)) {
                spChannel6Min.getValueFactory().setValue(Double.parseDouble(parameter.getValue()));
                return;
            }

            if (parameter.name.equals(TUNE_HIGH)) {
                spChannel6Max.getValueFactory().setValue(Double.parseDouble(parameter.getValue()));
                return;
            }
        });
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> list) {}

    public void resetAll() {
        for (ComboBox comboBox : comboBoxFltModeMap.values())
            comboBox.setValue(null);

        for (ComboBox comboBox : comboBoxCommandsMap.values())
            comboBox.setValue(null);

        for (ComboBox comboBox : comboBoxTunningMap.values())
            comboBox.setValue(null);

        spChannel6Min.getValueFactory().setValue(0.0);
        spChannel6Max.getValueFactory().setValue(0.0);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType droneEventsType, Drone drone) {
        Platform.runLater(() -> {
            switch (droneEventsType) {
                case DISCONNECTED: {
                    LOGGER.debug("Quad disconnected reset combo boxes");
                    resetAll();
                    break;
                }
            }
        });
    }
}
