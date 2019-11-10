package com.dronegcs.console.controllers.internalFrames;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

public abstract class InternalFrameChart extends Pane implements Initializable {

    protected boolean filterLastMin;

    static final int MIN = 60;

    @FXML
    protected ToggleButton btnLastMin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnLastMin.setText("Last Min");
    }

    protected void clearSeries(XYChart.Series<String, Number> series) {
        if (!filterLastMin)
            return;

        LocalTime now = LocalDateTime.now().toLocalTime();

        int startIdx = -1;
        int endIdx = 0;
        int ptr = 0;
        for (XYChart.Data<String, Number> entry : series.dataProperty().get()) {
            LocalTime timestamp = LocalTime.parse(entry.getXValue());
            if (now.toSecondOfDay() - timestamp.toSecondOfDay() < 1*MIN)
                break;

            if (startIdx == -1) {
                startIdx = ptr;
            }
            endIdx = ptr;
            ptr ++;
        }

        if (startIdx != -1)
            series.dataProperty().get().remove(startIdx, endIdx);
    }

    protected abstract void loadChart();

    protected abstract LineChart<String,Number> getLineChart();

    @FXML
    protected void handleClear(ActionEvent actionEvent) {
        System.out.println("Clear collection");
        getLineChart().dataProperty().get().clear();
        loadChart();
    }

    @FXML
    protected void handleFilteredMinutes(ActionEvent actionEvent) {
        ToggleButton toggleButton = (ToggleButton) actionEvent.getSource();
        this.filterLastMin = toggleButton.selectedProperty().get();

        if (filterLastMin) {
            System.out.println("Activate last minute filter");
        }
        else {
            System.out.println("De-Activate last minute filter");
        }
    }
}
