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

    private static String FLTMODE_PREFIX = "FLTMODE"; // Example: FLTMODE1

    private List<ComboBox> comboBoxList = null;

    private static Integer TYPE = 2;

    @PostConstruct
    private void init() {
        drone.getParameters().addParameterListener(this);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        comboBoxList = new ArrayList<>();
        comboBoxList.addAll(Arrays.asList(cbFltMode1, cbFltMode2, cbFltMode3, cbFltMode4, cbFltMode5, cbFltMode6));

        Vector<ApmModes> flightModes = new Vector<ApmModes>();
        flightModes.addAll(FXCollections.observableArrayList(ApmModes.getModeList(MAV_TYPE_QUADROTOR)));

        for (ComboBox comboBox : comboBoxList)
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

        for (int i = 0 ; i < comboBoxList.size() ; i++) {
            Parameter param = new Parameter(FLTMODE_PREFIX + (i+1), ((ApmModes) comboBoxList.get(i).getValue()).getNumber(), TYPE, "");
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
        for (int i = 0 ; i < comboBoxList.size() ; i++)
            comboBoxList.get(i).setValue(getMode(FLTMODE_PREFIX + (i+1)));
    }
}
