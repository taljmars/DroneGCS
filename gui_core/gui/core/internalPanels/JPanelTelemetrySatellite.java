package gui.core.internalPanels;

import gui.core.dashboard.LoggerDisplayerManager;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;

public class JPanelTelemetrySatellite extends JToolBar implements OnDroneListener {
	
	private static final long serialVersionUID = 486044738229582782L;
	
	private JPanel pnl;
	
	private JLabel lblHeightVal;
	private JLabel lblSignalVal;
	private JLabel lblBatteryVal;
	private JLabel lblFlightModeVal;

	private JLabel lblEngine1;
	private JLabel lblEngine2;
	private JLabel lblEngine3;
	private JLabel lblEngine4;

	private JLabel lblThrust;
	private JLabel lblYaw;
	private JLabel lblPitch;
	private JLabel lblRoll;
	
	private JLabel lblFlightTimeVal;
	private JLabel lblFlightDistanceVal;

	private JLabel keepAliveLabel;

	private LoggerDisplayerManager loggerDisplayerManager; 
	
	public JPanelTelemetrySatellite() {
		pnl = new JPanel();
		pnl.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.PAGE_AXIS));
		
        JPanel pnlStatus = new JPanel();
        JPanel pnlSignal = new JPanel();
		JPanel pnlRCSend = new JPanel();
		JPanel pnlRCActual = new JPanel();
		JPanel pnlStatisticsTime = new JPanel();
		JPanel pnlStatisticsDistanceTraveled = new JPanel();
		
		pnl.add(pnlStatus);
		pnl.add(pnlSignal);
		pnl.add(pnlRCSend);
		pnl.add(pnlRCActual);
		pnl.add(pnlStatisticsTime);
		pnl.add(pnlStatisticsDistanceTraveled);
        
        JLabel lblStatus = new JLabel("Status:");
        pnlStatus.add(lblStatus);
        
        keepAliveLabel = new JLabel("Disonnected");
        pnlStatus.add(keepAliveLabel);
        keepAliveLabel.setForeground(Color.RED);
        JLabel lblFlightMode = new JLabel("Mode:");
        pnlStatus.add(lblFlightMode);
        lblFlightModeVal = new JLabel("Unknown");
        lblFlightModeVal.setFont(new Font("Tahoma", Font.BOLD, 11));
        pnlStatus.add(lblFlightModeVal);
        JLabel lblBattery = new JLabel("Battery:");
        pnlSignal.add(lblBattery);
        lblBatteryVal = new JLabel("0%");
        pnlSignal.add(lblBatteryVal);
        JLabel lblSignal = new JLabel("Signal:");
        pnlSignal.add(lblSignal);
        lblSignalVal = new JLabel("0%");
        pnlSignal.add(lblSignalVal);
        JLabel lblHeight = new JLabel("Height:");
        pnlSignal.add(lblHeight);
        lblHeightVal = new JLabel("0m");
        pnlSignal.add(lblHeightVal);
        
        JLabel lblAxis = new JLabel("RC Send:");
        pnlRCSend.add(lblAxis);
        lblRoll = new JLabel("---");
        pnlRCSend.add(lblRoll);
        lblPitch = new JLabel("---");
        pnlRCSend.add(lblPitch);
        lblThrust = new JLabel("---");
        pnlRCSend.add(lblThrust);
        lblYaw = new JLabel("---");
        pnlRCSend.add(lblYaw);
        
        JLabel lblRcActual = new JLabel("RC Actual:");
        pnlRCActual.add(lblRcActual);
        lblEngine1 = new JLabel("---");
        pnlRCActual.add(lblEngine1);
        lblEngine2 = new JLabel("---");
        pnlRCActual.add(lblEngine2);
        lblEngine3 = new JLabel("---");
        pnlRCActual.add(lblEngine3);
        lblEngine4 = new JLabel("---");
        pnlRCActual.add(lblEngine4);
        
        
        JLabel lblFlightTime = new JLabel("Flight Time:");
        lblFlightTimeVal = new JLabel("-");
        pnlStatisticsTime.add(lblFlightTime);
        pnlStatisticsTime.add(lblFlightTimeVal);        
        JLabel lblFlightDistance = new JLabel("Flight Distance:");
        lblFlightDistanceVal = new JLabel("0m");
        pnlStatisticsDistanceTraveled.add(lblFlightDistance);
        pnlStatisticsDistanceTraveled.add(lblFlightDistanceVal);
        
        add(pnl);
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
		lblFlightTimeVal.setText(flightTime + "");
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
			keepAliveLabel.setForeground(Color.GREEN);
			return;
		}
		
		keepAliveLabel.setText("Disconnected");
		keepAliveLabel.setForeground(Color.RED);
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
			case ORIENTATION:
				SetLblHeight(drone.getAltitude().getAltitude());
				return;
			case HEARTBEAT_FIRST:
				loggerDisplayerManager.addErrorMessegeToDisplay("Quad Connected");
				SetHeartBeat(true);
				return;
			case HEARTBEAT_RESTORED:
				loggerDisplayerManager.addErrorMessegeToDisplay("Quad Connected");
				SetHeartBeat(true);
				return;
			case HEARTBEAT_TIMEOUT:
				loggerDisplayerManager.addErrorMessegeToDisplay("Quad Disconnected");
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
	}

	public void setLoggerDisplayerManager(LoggerDisplayerManager loggerDisplayerManager) {
		this.loggerDisplayerManager = loggerDisplayerManager;
	}
}
