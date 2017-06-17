package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.dronegcs.mavlink.is.drone.parameters.Parameter;
import com.dronegcs.mavlink.is.drone.profiles.Parameters;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmModes;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.*;

import static com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_TYPE.MAV_TYPE_QUADROTOR;

/**
 * Created by taljmars on 6/17/2017.
 */
@Component
public class FlightModes implements Initializable, DroneInterfaces.OnParameterManagerListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(FlightModes.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get drone")
    private Drone drone;

    @NotNull @FXML private ComboBox cbFltMode1;
    @NotNull @FXML private ComboBox cbFltMode2;
    @NotNull @FXML private ComboBox cbFltMode3;
    @NotNull @FXML private ComboBox cbFltMode4;
    @NotNull @FXML private ComboBox cbFltMode5;
    @NotNull @FXML private ComboBox cbFltMode6;
    @NotNull @FXML private ComboBox cbFltMode7;
    @NotNull @FXML private ComboBox cbFltMode8;

    private static String FLTMODE_FORMAT = "FLTMODE{}"; // Example: FLTMODE1
    private static String CHA_FORMAT = "CHA{}_OPT"; // Example: CHA7_OPT

    private List<ComboBox> comboBoxFltModeList = null;
    private List<ComboBox> comboBoxChaOptList = null;

    private static Integer TYPE = 2;

    @PostConstruct
    private void init() {
        drone.getParameters().addParameterListener(this);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        comboBoxFltModeList = new ArrayList<>();
        comboBoxFltModeList.addAll(Arrays.asList(cbFltMode1, cbFltMode2, cbFltMode3, cbFltMode4, cbFltMode5, cbFltMode6));

        comboBoxChaOptList = new ArrayList<>();
        comboBoxChaOptList.addAll(Arrays.asList(cbFltMode7, cbFltMode8));

        Vector<ApmModes> flightModes = new Vector<ApmModes>();
        flightModes.addAll(FXCollections.observableArrayList(ApmModes.getModeList(MAV_TYPE_QUADROTOR)));

        for (ComboBox comboBox : comboBoxChaOptList)
            comboBox.getItems().addAll(flightModes);

        for (ComboBox comboBox : comboBoxChaOptList)
            comboBox.getItems().addAll(flightModes);

        setValues();
    }

    private ApmModes getMode(String flightMode) {
        Parameters params = drone.getParameters();
        if (!drone.isConnectionAlive() || params == null) {
            LOGGER.debug("Drone is not connected, cannot get flight modes");
            return null;
        }

        return ApmModes.getMode(Integer.parseInt(params.getParameter(flightMode).getValue()), MAV_TYPE_QUADROTOR);
    }

    public void updateFlightMode(ActionEvent actionEvent) {
        Parameters params = drone.getParameters();
        if (!drone.isConnectionAlive() || params == null) {
            LOGGER.debug("Drone is not connected, cannot get flight modes");
            return;
        }

        for (int i = 0 ; i < comboBoxFltModeList.size() ; i++) {
            Parameter param = new Parameter(String.format(FLTMODE_FORMAT, (i+1)), ((ApmModes) comboBoxFltModeList.get(i).getValue()).getNumber(), TYPE, "");
            drone.getParameters().sendParameter(param);
        }

        for (int i = 0 ; i < comboBoxChaOptList.size() ; i++) {
            Parameter param = new Parameter(String.format(CHA_FORMAT, (i+7)), ((ApmModes) comboBoxChaOptList.get(i).getValue()).getNumber(), TYPE, "");
            drone.getParameters().sendParameter(param);
        }
    }

    @Override
    public void onBeginReceivingParameters() {}

    @Override
    public void onParameterReceived(Parameter parameter, int i, int i1) {}

    @Override
    public void onEndReceivingParameters(List<Parameter> list) {
        Platform.runLater(() -> setValues());
    }

    private void setValues() {
        for (int i = 0 ; i < comboBoxFltModeList.size() ; i++)
            comboBoxFltModeList.get(i).setValue(getMode(String.format(FLTMODE_FORMAT, (i+1))));

        for (int i = 0 ; i < comboBoxChaOptList.size() ; i++)
            comboBoxChaOptList.get(i).setValue(getMode(String.format(CHA_FORMAT, (i+7))));
    }
}
