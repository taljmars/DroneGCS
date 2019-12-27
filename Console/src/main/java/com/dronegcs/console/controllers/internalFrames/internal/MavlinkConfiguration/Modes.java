package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.dronegcs.mavlink.is.drone.parameters.Parameter;
import com.dronegcs.mavlink.is.drone.profiles.Parameters;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmCommands;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmModes;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmTuning;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_TYPE;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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

/**
 * Created by taljmars on 6/17/2017.
 */
@Component
public class Modes implements Initializable, DroneInterfaces.OnParameterManagerListener, DroneInterfaces.OnDroneListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(Modes.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get drone")
    private Drone drone;

    @NotNull @FXML private Label cbFltMode1Label;
    @NotNull @FXML private Label cbFltMode2Label;
    @NotNull @FXML private Label cbFltMode3Label;
    @NotNull @FXML private Label cbFltMode4Label;
    @NotNull @FXML private Label cbFltMode5Label;
    @NotNull @FXML private Label cbFltMode6Label;

    @NotNull @FXML private ComboBox cbFltMode1;
    @NotNull @FXML private ComboBox cbFltMode2;
    @NotNull @FXML private ComboBox cbFltMode3;
    @NotNull @FXML private ComboBox cbFltMode4;
    @NotNull @FXML private ComboBox cbFltMode5;
    @NotNull @FXML private ComboBox cbFltMode6;

    @NotNull @FXML private CheckBox cbFltMode1Simple;
    @NotNull @FXML private CheckBox cbFltMode2Simple;
    @NotNull @FXML private CheckBox cbFltMode3Simple;
    @NotNull @FXML private CheckBox cbFltMode4Simple;
    @NotNull @FXML private CheckBox cbFltMode5Simple;
    @NotNull @FXML private CheckBox cbFltMode6Simple;

    @NotNull @FXML private CheckBox cbFltMode1SuperSimple;
    @NotNull @FXML private CheckBox cbFltMode2SuperSimple;
    @NotNull @FXML private CheckBox cbFltMode3SuperSimple;
    @NotNull @FXML private CheckBox cbFltMode4SuperSimple;
    @NotNull @FXML private CheckBox cbFltMode5SuperSimple;
    @NotNull @FXML private CheckBox cbFltMode6SuperSimple;

    private int cbFltMode1Min = 0;
    private int cbFltMode1Max = 1230;
    private int cbFltMode2Min = 1231;
    private int cbFltMode2Max = 1360;
    private int cbFltMode3Min = 1361;
    private int cbFltMode3Max = 1490;
    private int cbFltMode4Min = 1491;
    private int cbFltMode4Max = 1620;
    private int cbFltMode5Min = 1621;
    private int cbFltMode5Max = 1749;
    private int cbFltMode6Min = 1755;
    private int cbFltMode6Max = 2200;

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
    private static final String SIMPLE_MODE = "SIMPLE";
    private static final String SUPER_SIMPLE_MODE = "SUPER_SIMPLE";

    private Map<String, ComboBox> comboBoxFltModeMap = null;
    private Map<String, ComboBox> comboBoxCommandsMap = null;
    private Map<String, ComboBox> comboBoxTuningMap = null;
    private List<CheckBox> checkBoxSimpleModeList = null;
    private List<CheckBox> checkBoxSuperSimpleModeList = null;
    private List<Label> fltLabels = null;

    @PostConstruct
    private void init() {
        drone.getParameters().addParameterListener(this);
        drone.addDroneListener(this);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        comboBoxFltModeMap = new HashMap<>();
        List<ComboBox> cmList = Arrays.asList(cbFltMode1, cbFltMode2, cbFltMode3, cbFltMode4, cbFltMode5, cbFltMode6);
        for (int i = 0; i < cmList.size(); i++) {
            comboBoxFltModeMap.put(String.format(FLTMODE_FORMAT, (i + 1)), cmList.get(i));
            int finalI = i;
            cmList.get(i).valueProperty().addListener((observable, oldValue,  newValue) -> {
                if (newValue != null) {
                    ApmModes mode;
                    if (newValue instanceof ApmModes)
                        mode = (ApmModes) newValue;
                    else if (newValue instanceof Parameter)
                        mode = getMode((Parameter) newValue, drone.getType().getDroneType());
                    else throw new RuntimeException("Unexpected type " + newValue.getClass());

                    checkBoxSimpleModeList.get(finalI).setVisible(mode.isSuperSimpleOrSimpleModeAvailable());
                    checkBoxSuperSimpleModeList.get(finalI).setVisible(mode.isSuperSimpleOrSimpleModeAvailable());

                    if (!mode.isSuperSimpleOrSimpleModeAvailable()) {
                        checkBoxSimpleModeList.get(finalI).setSelected(false);
                        checkBoxSuperSimpleModeList.get(finalI).setSelected(false);
                    }
                }
            });
        }

        checkBoxSimpleModeList = new ArrayList<>();
        checkBoxSimpleModeList.addAll(Arrays.asList(cbFltMode1Simple, cbFltMode2Simple, cbFltMode3Simple, cbFltMode4Simple, cbFltMode5Simple, cbFltMode6Simple));

        checkBoxSuperSimpleModeList = new ArrayList<>();
        checkBoxSuperSimpleModeList.addAll(Arrays.asList(cbFltMode1SuperSimple, cbFltMode2SuperSimple, cbFltMode3SuperSimple, cbFltMode4SuperSimple, cbFltMode5SuperSimple, cbFltMode6SuperSimple));

        comboBoxCommandsMap = new HashMap<>();
        comboBoxCommandsMap.put(String.format(CH_FORMAT, 7), cbChannel7);
        comboBoxCommandsMap.put(String.format(CH_FORMAT, 8), cbChannel8);

        comboBoxTuningMap = new HashMap<>();
        comboBoxTuningMap.put(TUNE, cbChannel6);

        // Generate flight mode combo-boxs and keys
        Vector<ApmModes> flightModes = new Vector<ApmModes>();
        flightModes.addAll(FXCollections.observableArrayList(ApmModes.getModeList(drone.getType().getDroneType())));
        loadComboBox(flightModes, comboBoxFltModeMap);

        // Generate command combo-boxs and keys
        Vector<ApmCommands> commands = new Vector<ApmCommands>();
        commands.addAll(FXCollections.observableArrayList(ApmCommands.getCommandsList()));
        loadComboBox(commands, comboBoxCommandsMap);

        // Generate tunning combo-boxs and keys
        Vector<ApmTuning> tunes = new Vector<ApmTuning>();
        tunes.addAll(FXCollections.observableArrayList(ApmTuning.getTuningList()));
        loadComboBox(tunes, comboBoxTuningMap);

        loadSimplesModes();

        spChannel6Max.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 32.767, 1.0, 0.01));
        Optional optional = drone.getParameters().getParametersList().stream().filter(parameter -> parameter.getName().equals(TUNE_HIGH)).findFirst();
        if (optional.isPresent())
            spChannel6Max.getValueFactory().valueProperty().setValue(((Parameter)optional.get()).getValue().doubleValue());

        spChannel6Min.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 32.767, 0.0, 0.01));
        optional = drone.getParameters().getParametersList().stream().filter(parameter -> parameter.getName().equals(TUNE_LOW)).findFirst();
        if (optional.isPresent())
            spChannel6Min.getValueFactory().valueProperty().setValue(((Parameter)optional.get()).getValue().doubleValue());

        fltLabels = new ArrayList<>();
        fltLabels.addAll(Arrays.asList(cbFltMode1Label, cbFltMode2Label, cbFltMode3Label, cbFltMode4Label, cbFltMode5Label, cbFltMode6Label));
    }

    private void loadComboBox(Vector<?> options, Map<String, ComboBox> comboBoxMap) {
        for (Map.Entry<String, ComboBox> entry : comboBoxMap.entrySet()) {
            entry.getValue().getItems().addAll(options);
            Optional optional = drone.getParameters().getParametersList().stream().filter(parameter -> parameter.getName().equals(entry.getKey())).findFirst();
            if (optional.isPresent()) {
                Parameter parameter = (Parameter) optional.get();
                ApmModes modes = getMode(parameter, drone.getType().getDroneType());
                entry.getValue().setValue(modes);
            }
        }
    }

    private void loadSimplesModes() {
        Byte simple = 0;
        Byte superSimple = 0;
        Optional optional = drone.getParameters().getParametersList().stream().filter(parameter -> parameter.getName().equals(SIMPLE_MODE)).findFirst();
        if (optional.isPresent())
            simple = ((Parameter) optional.get()).getValue().byteValue();

        optional = drone.getParameters().getParametersList().stream().filter(parameter -> parameter.getName().equals(SUPER_SIMPLE_MODE)).findFirst();
        if (optional.isPresent())
            //superSimple = (Byte) optional.get();
            superSimple = ((Parameter) optional.get()).getValue().byteValue();

        loadSimplesModes(simple, checkBoxSimpleModeList);
        loadSimplesModes(superSimple, checkBoxSuperSimpleModeList);
    }

    private void loadSimplesModes(Byte modeBitMap, List<CheckBox> checkBoxes) {
        for ( int i = 1 ; i <= 6 ; i++ ) {
            boolean tr = getSimplesOrSuperSimpleMode(i, modeBitMap);
            checkBoxes.get(i-1).setSelected(tr);
        }
    }

    private boolean getSimplesOrSuperSimpleMode(int modeBitIndex, Byte mode) {
        int placer = 0x1 << (modeBitIndex - 1);
        return (mode & placer) != 0;
    }

    private ApmTuning getTuning(Parameter parameter) {
        if (parameter == null)
            return null;
        return ApmTuning.getTune(parameter.getValue().intValue());
    }

    private static ApmModes getMode(Parameter parameter, MAV_TYPE droneType) {
        if (parameter == null)
            return null;
        return ApmModes.getMode(parameter.getValue().intValue(), droneType);
    }

    private static ApmCommands getCommand(Parameter parameter) {
        if (parameter == null)
            return null;
        return ApmCommands.getCommand(parameter.getValue().intValue());
    }

    @FXML
    public void refresh(ActionEvent actionEvent) {
        for (Map.Entry<String, ComboBox> entry : comboBoxFltModeMap.entrySet())
            drone.getParameters().ReadParameter(entry.getKey());

        for (Map.Entry<String, ComboBox> entry : comboBoxCommandsMap.entrySet())
            drone.getParameters().ReadParameter(entry.getKey());

        for (Map.Entry<String, ComboBox> entry : comboBoxTuningMap.entrySet())
            drone.getParameters().ReadParameter(entry.getKey());

        drone.getParameters().ReadParameter(TUNE_HIGH);
        drone.getParameters().ReadParameter(TUNE_LOW);

        drone.getParameters().ReadParameter(SIMPLE_MODE);
        drone.getParameters().ReadParameter(SUPER_SIMPLE_MODE);
    }

    @FXML
    public void updateModes(ActionEvent actionEvent) {
        Parameters params = drone.getParameters();
        Parameter parameter = null, existingParameter = null;

        if (!drone.isConnectionAlive() || params == null) {
            LOGGER.debug("Drone is not connected, cannot get flight modes");
            return;
        }

        for (Map.Entry<String, ComboBox> entry : comboBoxFltModeMap.entrySet()) {
            existingParameter = params.getParameter(entry.getKey());
            parameter = new Parameter(
                    entry.getKey(),
                    existingParameter.getGroup(),
                    ((ApmModes) entry.getValue().getValue()).getNumber(),
                    existingParameter.getDefaultValue(),
                    existingParameter.getUnit(),
//                    MAV_PARAM_TYPE_INT8,
                    existingParameter.getType(),
                    existingParameter.isReadOnly(),
                    existingParameter.getTitle(),
                    existingParameter.getDescription()
            );
            System.err.println("Send updated flt param " + parameter);
            drone.getParameters().sendParameter(parameter);
        }

        for (Map.Entry<String, ComboBox> entry : comboBoxCommandsMap.entrySet()) {
            existingParameter = params.getParameter(entry.getKey());
            parameter = new Parameter(
                    entry.getKey(),
                    existingParameter.getGroup(),
                    ((ApmCommands) entry.getValue().getValue()).getNumber(),
                    existingParameter.getDefaultValue(),
                    existingParameter.getUnit(),
//                    MAV_PARAM_TYPE_INT8,
                    existingParameter.getType(),
                    existingParameter.isReadOnly(),
                    existingParameter.getTitle(),
                    existingParameter.getDescription()
            );
            System.err.println("Send updated cmd param " + parameter);
            drone.getParameters().sendParameter(parameter);
        }

        for (Map.Entry<String, ComboBox> entry : comboBoxTuningMap.entrySet()) {
            existingParameter = params.getParameter(entry.getKey());
            parameter = new Parameter(
                    entry.getKey(),
                    existingParameter.getGroup(),
                    ((ApmTuning) entry.getValue().getValue()).getNumber(),
                    existingParameter.getDefaultValue(),
                    existingParameter.getUnit(),
//                    9,
                    existingParameter.getType(),
                    existingParameter.isReadOnly(),
                    existingParameter.getTitle(),
                    existingParameter.getDescription());
            System.err.println("Send updated tune param " + parameter);
            drone.getParameters().sendParameter(parameter);
        }

        existingParameter = params.getParameter(TUNE_HIGH);
        parameter = new Parameter(
                TUNE_HIGH,
                existingParameter.getGroup(),
                spChannel6Max.getValue(),
                existingParameter.getDefaultValue(),
                existingParameter.getUnit(),
//                MAV_PARAM_TYPE_INT16,
                existingParameter.getType(),
                existingParameter.isReadOnly(),
                existingParameter.getTitle(),
                existingParameter.getDescription()
        );
        drone.getParameters().sendParameter(parameter);

        existingParameter = params.getParameter(TUNE_LOW);
        parameter = new Parameter(
                TUNE_LOW,
                existingParameter.getGroup(),
                spChannel6Min.getValue(),
                existingParameter.getDefaultValue(),
                existingParameter.getUnit(),
//                MAV_PARAM_TYPE_INT16,
                existingParameter.getType(),
                existingParameter.isReadOnly(),
                existingParameter.getTitle(),
                existingParameter.getDescription()
        );
        drone.getParameters().sendParameter(parameter);

        Integer simple = 0;
        Integer superSimple = 0;
        for (int i = 1 ; i <= checkBoxSimpleModeList.size() && checkBoxSimpleModeList.size() == checkBoxSuperSimpleModeList.size() ; i++ ) {
            int ptr = 0x1 << (i-1);
            if (checkBoxSimpleModeList.get(i-1).isSelected()) {
                LOGGER.debug("Simple on {} is selected", i-1);
                simple |= ptr;
            }

            if (checkBoxSuperSimpleModeList.get(i-1).isSelected())
                superSimple |= ptr;
        }

        existingParameter = params.getParameter(SIMPLE_MODE);
        parameter = new Parameter(
                SIMPLE_MODE,
                existingParameter.getGroup(),
                simple,
                existingParameter.getDefaultValue(),
                existingParameter.getUnit(),
//                MAV_PARAM_TYPE_INT8,
                existingParameter.getType(),
                existingParameter.isReadOnly(),
                existingParameter.getTitle(),
                existingParameter.getDescription()
        );
        drone.getParameters().sendParameter(parameter);

        existingParameter = params.getParameter(SUPER_SIMPLE_MODE);
        parameter = new Parameter(
                SUPER_SIMPLE_MODE,
                existingParameter.getGroup(),
                superSimple,
                existingParameter.getDefaultValue(),
                existingParameter.getUnit(),
//                MAV_PARAM_TYPE_INT8,
                existingParameter.getType(),
                existingParameter.isReadOnly(),
                existingParameter.getTitle(),
                existingParameter.getDescription()
        );
        drone.getParameters().sendParameter(parameter);
    }

    @Override
    public void onBeginReceivingParameters() {}

    @Override
    public void onParameterReceived(Parameter parameter, int i, int i1) {
        Platform.runLater(() -> {
            if (comboBoxFltModeMap != null && comboBoxFltModeMap.keySet().contains(parameter.getName())) {
                ApmModes mode = getMode(parameter, drone.getType().getDroneType());
                LOGGER.debug("Received flight mode = {}", mode);
                ((ComboBox<ApmModes>) comboBoxFltModeMap.get(parameter.getName())).setValue(mode);
                return;
            }

            if (comboBoxCommandsMap != null && comboBoxCommandsMap.keySet().contains(parameter.getName())) {
                ApmCommands cmd = getCommand(parameter);
                LOGGER.debug("Received command = {}", cmd);
                comboBoxCommandsMap.get(parameter.getName()).setValue(cmd);
                return;
            }

            if (comboBoxTuningMap != null && comboBoxTuningMap.keySet().contains(parameter.getName())) {
                ApmTuning tune = getTuning(parameter);
                LOGGER.debug("Received tune = {}", tune);
                comboBoxTuningMap.get(parameter.getName()).setValue(tune);
                return;
            }

            if (spChannel6Min != null && parameter.getName().equals(TUNE_LOW)) {
                Double tuneLow = parameter.getValue().doubleValue();
                LOGGER.debug("Received tune low = {}", tuneLow);
                spChannel6Min.getValueFactory().valueProperty().setValue(tuneLow);
                return;
            }

            if (spChannel6Max != null && parameter.getName().equals(TUNE_HIGH)) {
                Double tuneHigh = parameter.getValue().doubleValue();
                LOGGER.debug("Received tune high = {}", tuneHigh);
                spChannel6Max.getValueFactory().valueProperty().setValue(tuneHigh);
                return;
            }

            if (checkBoxSimpleModeList != null && parameter.getName().equals(SIMPLE_MODE)) {
                Byte simple = parameter.getValue().byteValue();
                LOGGER.debug("Received simple = {}", simple);
                loadSimplesModes(simple, checkBoxSimpleModeList);
                return;
            }

            if (checkBoxSuperSimpleModeList != null && parameter.getName().equals(SUPER_SIMPLE_MODE)) {
                Byte superSimple = parameter.getValue().byteValue();
                LOGGER.debug("Received superSimple = {}", superSimple);
                loadSimplesModes(superSimple, checkBoxSuperSimpleModeList);
                return;
            }
        });
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> list) {}

    public void resetAll() {
        // Verify GUI components were initialized
        if (comboBoxFltModeMap == null)
            return;

        for (ComboBox comboBox : comboBoxFltModeMap.values())
            comboBox.setValue(null);

        for (ComboBox comboBox : comboBoxCommandsMap.values())
            comboBox.setValue(null);

        for (ComboBox comboBox : comboBoxTuningMap.values())
            comboBox.setValue(null);

        for (CheckBox checkBox : checkBoxSimpleModeList)
            checkBox.setSelected(false);

        for (CheckBox checkBox : checkBoxSuperSimpleModeList)
            checkBox.setSelected(false);

        spChannel6Min.getValueFactory().setValue(new Double(0.0));
        spChannel6Max.getValueFactory().setValue(new Double(1.0));
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType droneEventsType, Drone drone) {
        Platform.runLater(() -> {
            switch (droneEventsType) {
                case DISCONNECTED: {
                    LOGGER.debug("Drone  disconnected reset combo boxes");
                    resetAll();
                    break;
                }
                case RC_IN: {
                    // Verify that the GUI components was initialized
                    if (fltLabels == null)
                        break;
                    String mark = "> ";
                    for (Label label : fltLabels) {
                        if (label.getText().startsWith(mark))
                            label.setText(label.getText().substring(mark.length()));
                    }
                    Label lbl = getCurrentActiveLabel();
                    if (lbl != null)
                        lbl.setText(mark + lbl.getText());
                    break;
                }
            }
        });
    }

    private Label getCurrentActiveLabel() {
        int[] rcin = drone.getRC().in;
        int ch5 = rcin[4];
        if (checkBound(ch5, cbFltMode1Min, cbFltMode1Max))
            return cbFltMode1Label;
        if (checkBound(ch5, cbFltMode2Min, cbFltMode2Max))
            return cbFltMode2Label;
        if (checkBound(ch5, cbFltMode3Min, cbFltMode3Max))
            return cbFltMode3Label;
        if (checkBound(ch5, cbFltMode4Min, cbFltMode4Max))
            return cbFltMode4Label;
        if (checkBound(ch5, cbFltMode5Min, cbFltMode5Max))
            return cbFltMode5Label;
        if (checkBound(ch5, cbFltMode6Min, cbFltMode6Max))
            return cbFltMode6Label;

        return null;
    }

    private boolean checkBound(int val, int min, int max) {
        if (min == val || max == val)
            return true;

        if (min < val && val < max)
            return true;

        return false;
    }
}
