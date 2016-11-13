package gui.core.internalPanels;

import gui.is.services.LoggerDisplayerSvc;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;

@Component("telemetrySatellite")
public class PanelTelemetrySatellite extends VBox implements OnDroneListener {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 486044738229582782L;
	
	private Label lblHeightVal;
	private Label lblSignalVal;
	private Label lblBatteryVal;
	private Label lblFlightModeVal;

	private Label lblEngine1;
	private Label lblEngine2;
	private Label lblEngine3;
	private Label lblEngine4;

	private Label lblThrust;
	private Label lblYaw;
	private Label lblPitch;
	private Label lblRoll;
	
	private Label lblFlightTimeVal;
	private Label lblFlightDistanceVal;

	private Label keepAliveLabel;
	
	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "drone")
	public Drone drone;
	
	
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		Font headlineFont = Font.font(Font.getDefault().getName(), FontWeight.BOLD, Font.getDefault().getSize());
		
		TilePane firstSection = new TilePane();
		firstSection.setPrefColumns(3);
		firstSection.setHgap(1);
		firstSection.setAlignment(Pos.CENTER);
		
		VBox pnlConnection = new VBox();
		pnlConnection.setAlignment(Pos.CENTER);
		firstSection.getChildren().add(pnlConnection);
		
		VBox pnlMode = new VBox();
		pnlMode.setAlignment(Pos.CENTER);
		firstSection.getChildren().add(pnlMode);
		
		VBox pnlBattery = new VBox();
		pnlBattery.setAlignment(Pos.CENTER);
		firstSection.getChildren().add(pnlBattery);
		
		VBox pnlSignal = new VBox();
		pnlSignal.setAlignment(Pos.CENTER);
		firstSection.getChildren().add(pnlSignal);
		
		VBox pnlHeight = new VBox();
		pnlHeight.setAlignment(Pos.CENTER);
		firstSection.getChildren().add(pnlHeight);
		
		VBox pnlStatisticsTime = new VBox();
		pnlStatisticsTime.setAlignment(Pos.CENTER);
		firstSection.getChildren().add(pnlStatisticsTime);
		
		VBox pnlStatisticsDistanceTraveled = new VBox();
		pnlStatisticsDistanceTraveled.setAlignment(Pos.CENTER);
		firstSection.getChildren().add(pnlStatisticsDistanceTraveled);
		
		TilePane secondSection = new TilePane();
		secondSection.setPrefColumns(2);
		secondSection.setHgap(5);
		secondSection.setAlignment(Pos.CENTER);
		
		VBox pnlRCSend = new VBox();
		pnlRCSend.setAlignment(Pos.CENTER);
		secondSection.getChildren().add(pnlRCSend);
		
		VBox pnlRCActual = new VBox();
		pnlRCActual.setAlignment(Pos.CENTER);
		secondSection.getChildren().add(pnlRCActual);
		
		getChildren().add(firstSection);
		getChildren().add(secondSection);
		
		Label lblStatus = new Label("Status");
		lblStatus.setFont(headlineFont);
		pnlConnection.getChildren().add(lblStatus);
        keepAliveLabel = new Label("Disonnected");
        keepAliveLabel.setTextFill(Color.web("#FF0000"));
        keepAliveLabel.setFont(headlineFont);
        pnlConnection.getChildren().add(keepAliveLabel);
        
        Label lblFlightMode = new Label("Mode");
        lblFlightMode.setFont(headlineFont);
        pnlMode.getChildren().add(lblFlightMode);
        lblFlightModeVal = new Label("Unknown");
        pnlMode.getChildren().add(lblFlightModeVal);        
        
        Label lblBattery = new Label("Battery");
        lblBattery.setFont(headlineFont);
        pnlBattery.getChildren().add(lblBattery);
        lblBatteryVal = new Label("0%");
        pnlBattery.getChildren().add(lblBatteryVal);
        
        Label lblSignal = new Label("Signal");
        lblSignal.setFont(headlineFont);
        pnlSignal.getChildren().add(lblSignal);
        lblSignalVal = new Label("0%");
        pnlSignal.getChildren().add(lblSignalVal);
        
        Label lblHeight = new Label("Height");
        lblHeight.setFont(headlineFont);
        pnlHeight.getChildren().add(lblHeight);
        lblHeightVal = new Label("0m");
        pnlHeight.getChildren().add(lblHeightVal);
        
        Label lblFlightTime = new Label("F.Time");
        lblFlightTime.setFont(headlineFont);
        lblFlightTimeVal = new Label("0 sec (0 min)");
        pnlStatisticsTime.getChildren().add(lblFlightTime);
        pnlStatisticsTime.getChildren().add(lblFlightTimeVal);
        
        Label lblFlightDistance = new Label("F.Distance");
        lblFlightDistance.setFont(headlineFont);
        lblFlightDistanceVal = new Label("0m");
        pnlStatisticsDistanceTraveled.getChildren().add(lblFlightDistance);
        pnlStatisticsDistanceTraveled.getChildren().add(lblFlightDistanceVal);
        
        Label lblAxis = new Label("RC Send:");
        lblAxis.setFont(headlineFont);
        pnlRCSend.getChildren().add(lblAxis);
        lblRoll = new Label("---");
        pnlRCSend.getChildren().add(lblRoll);
        lblPitch = new Label("---");
        pnlRCSend.getChildren().add(lblPitch);
        lblThrust = new Label("---");
        pnlRCSend.getChildren().add(lblThrust);
        lblYaw = new Label("---");
        pnlRCSend.getChildren().add(lblYaw);
        
        Label lblRcActual = new Label("RC Actual:");
        lblRcActual.setFont(headlineFont);
        pnlRCActual.getChildren().add(lblRcActual);
        lblEngine1 = new Label("---");
        pnlRCActual.getChildren().add(lblEngine1);
        lblEngine2 = new Label("---");
        pnlRCActual.getChildren().add(lblEngine2);
        lblEngine3 = new Label("---");
        pnlRCActual.getChildren().add(lblEngine3);
        lblEngine4 = new Label("---");
        pnlRCActual.getChildren().add(lblEngine4);
        
		drone.addDroneListener(this);
	}
	
	protected void SetFlightModeLabel(String name) {
		lblFlightModeVal.setText(name);
	}
	
	public void SetLblHeight(double ht) {
		lblHeightVal.setText(String.format("%.1f", ht) + "m");
	}
	
	public void SetSignal(int signalStrength) {
		lblSignalVal.setText(signalStrength + "%");
	}
	
	private void setFlightTime(long flightTime) {
		lblFlightTimeVal.setText(flightTime + " sec (" + flightTime/60 + " min)");
	}

	private void setDistanceTraveled(double distanceTraveled) {
		lblFlightDistanceVal.setText(String.format("%.1f", distanceTraveled) + "m");
	}
	
	public void SetLblBattery(double bat) {
		lblBatteryVal.setText((bat < 0 ? 0 : bat) + "%");
	}
	
	public void setRCActual(int e1, int e2, int e3, int e4) {
		lblEngine1.setText(e1 + "");
		lblEngine2.setText(e2 + "");    
		lblEngine3.setText(e3 + "");
		lblEngine4.setText(e4 + "");
	}
	
	public void setRCSend(int roll, int pitch, int thrust, int yaw) {
		lblRoll.setText("Roll: " + roll);
		lblPitch.setText("Pitch: " + pitch);
		lblThrust.setText("Thrust: " + thrust);
		lblYaw.setText("Yaw: " + yaw);
	}
	
	public void SetHeartBeat(boolean on) {
		if (on) {
			keepAliveLabel.setText("Connected");
			keepAliveLabel.setTextFill(Color.web("#008000"));
			return;
		}
		
		keepAliveLabel.setText("Disconnected");
		keepAliveLabel.setTextFill(Color.web("#FF0000"));
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		Platform.runLater( () -> {
			switch (event) {
				case ORIENTATION:
					SetLblHeight(drone.getAltitude().getAltitude());
					return;
				case HEARTBEAT_FIRST:
					loggerDisplayerSvc.logGeneral("Quad Connected");
					SetHeartBeat(true);
					return;
				case HEARTBEAT_RESTORED:
					loggerDisplayerSvc.logGeneral("Quad Connection Restored");
					SetHeartBeat(true);
					return;
				case DISCONNECTED:
				case HEARTBEAT_TIMEOUT:
					loggerDisplayerSvc.logError("Quad Disconnected");
					SetLblHeight(0);
					SetSignal(0);
					SetLblBattery(0);
					SetFlightModeLabel("Unknown");
					SetHeartBeat(false);
					return;
				case RADIO:
					SetSignal(drone.getRadio().getSignalStrength());
					return;
				case RC_OUT:
					setRCActual(drone.getRC().out[0], drone.getRC().out[1], drone.getRC().out[2], drone.getRC().out[3]);
					return;
				case RC_IN:
					setRCSend(drone.getRC().in[0], drone.getRC().in[1], drone.getRC().in[2], drone.getRC().in[3]);
					return;
				case GPS:
					setDistanceTraveled(drone.getGps().getDistanceTraveled());
					return;
				case STATE:
					setFlightTime(drone.getState().getFlightTime());
					return;
				case BATTERY:
					SetLblBattery(drone.getBattery().getBattRemain());
					return;
				case MODE:
					SetFlightModeLabel(drone.getState().getMode().getName());
					return;
			}
		});
	}
}
