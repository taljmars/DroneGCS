package gui.core.internalPanels;

import java.awt.GridLayout;
import java.util.List;


import gui.core.operations.internal.OpArmQuad;
import gui.core.operations.internal.OpChangeFlightControllerQuad;
import gui.core.operations.internal.OpTakeoffQuad;
import gui.core.operations.internal.OpStartMissionQuad;
import gui.is.services.LoggerDisplayerSvc;
import gui.is.services.TextNotificationPublisher;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;


import tools.logger.Logger;
import mavlink.core.connection.helper.GCSLocationData;
import mavlink.core.flightControlers.FlightControler;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.drone.DroneInterfaces.OnParameterManagerListener;
import mavlink.is.drone.parameters.Parameter;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.protocol.msgbuilder.MavLinkArm;
import mavlink.is.protocol.msgbuilder.MavLinkModes;

@ComponentScan("mavlink.core.drone")
@ComponentScan("gui.core.operations.internal")
@ComponentScan("mavlink.core.drone")
@ComponentScan("gui.is.services")
@Component("buttonBoxSatellite")
public class JPanelButtonBoxSatellite extends JToolBar implements OnDroneListener, OnParameterManagerListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2419085692095415348L;
	
	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	private JPanel pnl;
	
	private JButton btnConnect;
	private JButton btnExit;
	private JButton btnSyncDrone;
	private JButton btnFly;
	private JToggleButton btnArm;
	private JButton btnLandRTL;
	private JButton btnTakeoff;
	private JButton btnGCSShow;
	private JButton btnHoldPosition;
	private JToggleButton btnStartMission;
	private JToggleButton btnStartPerimeter;
	
	private boolean connected = false;
	private boolean takeOffThreadRunning = false;
    private SwingWorker<Void, Void> takeOffThread = null;
    private JToggleButton btnFollowBeaconStart;
    private SwingWorker<Void, Void> FollowBeaconStartThread = null;
    private AbstractButton btnFollowBeaconShow;

	@Resource(name = "drone")
	private Drone drone;
	
	@Resource(name = "textNotificationPublisher")
	private TextNotificationPublisher textNotificationPublisher;
	
	@Resource(name = "opArmQuad")
	private OpArmQuad opArmQuad;
	
	@Resource(name = "opTakeoffQuad")
	private OpTakeoffQuad opTakeoffQuad;
	
	@Resource(name = "opStartMissionQuad")
	private OpStartMissionQuad opStartMissionQuad;
	
	@Resource(name = "opChangeFlightControllerQuad")
	private OpChangeFlightControllerQuad opChangeFlightControllerQuad;
	
	public JPanelButtonBoxSatellite () {		
		pnl = new JPanel();
		pnl.setLayout(new GridLayout(0, 2, 1, 1));
		
		btnExit = new JButton("Exit");
        btnExit.addActionListener( e -> {
        	if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Are you sure you wand to exit?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
        		System.out.println("Bye Bye");
        		Logger.LogGeneralMessege("");
        		Logger.LogGeneralMessege("Summary:");
        		Logger.LogGeneralMessege("--------");
        		Logger.LogGeneralMessege("Traveled distance: " + drone.getGps().getDistanceTraveled() + "m");
        		Logger.LogGeneralMessege("Max Height: " + drone.getAltitude().getMaxAltitude() + "m");
        		Logger.LogGeneralMessege("Max Speed: " + drone.getSpeed().getMaxAirSpeed().valueInMetersPerSecond() + "m/s (" + ((int) (drone.getSpeed().getMaxAirSpeed().valueInMetersPerSecond()*3.6)) + "km/h)");
        		Logger.LogGeneralMessege("Flight time: " + drone.getState().getFlightTime() + "");
				Logger.close();
				System.exit(0);
        	}
		});
        pnl.add(btnExit);
        
        btnConnect = new JButton("Connect");
        btnConnect.addActionListener( e -> {
        	if (connected) {
        		loggerDisplayerSvc.logGeneral("Close Connection");
        		drone.getMavClient().disconnect();
        	}
        	else {
		    	loggerDisplayerSvc.logGeneral("Open Connection");
		    	drone.getMavClient().connect();
        	}
		});
        pnl.add(btnConnect);
        
        btnSyncDrone = new JButton("Sync Drone");
        btnSyncDrone.setEnabled(false);
        btnSyncDrone.addActionListener( e -> {
        	System.out.println("Sync");
        	loggerDisplayerSvc.logGeneral("Syncing Drone parameters");
    		drone.getParameters().refreshParameters();
        });
        pnl.add(btnSyncDrone);
        
        btnFly = new JButton("Controler: " + FlightControler.REMOTE.name());
        btnFly.addActionListener( e -> {
        	try {
        		String btnFlyText = btnFly.getText();
        		opChangeFlightControllerQuad.setNext(null);
        		Object[] options = {FlightControler.KEYBOARD.name(), FlightControler.REMOTE.name()};
            	int n = JOptionPane.showOptionDialog(null, "Choose Controler", "",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,options, options[1]);
            	if (n == 0) {
            		opChangeFlightControllerQuad.setFlightMode(FlightControler.KEYBOARD);
            		btnFlyText = "Controler: " + FlightControler.KEYBOARD.name();
            	}
            	if (n == 1) {
            		opChangeFlightControllerQuad.setFlightMode(FlightControler.REMOTE);
            		btnFlyText = "Controler: " + FlightControler.REMOTE.name();
            	}
				if (opChangeFlightControllerQuad.go()) {
					loggerDisplayerSvc.logGeneral("Start Fly '" + options[n] + "'");
					btnFly.setText(btnFlyText);
				}
				
			} catch (Exception e1) {
				Logger.LogErrorMessege(e1.getMessage());
			}

        });
        pnl.add(btnFly);        
        
        btnArm = new JToggleButton("Arm Motors");
        btnArm.addActionListener( e -> {
        	if (btnArm.isSelected()) {
        		loggerDisplayerSvc.logOutgoing("arm");
        		MavLinkArm.sendArmMessage(drone, true);
        	}
        	else {
        		// Not selected
        		if (drone.getState().isFlying()) {
                	JOptionPane.showMessageDialog(null, "Drone is flying, dis-arming motor is dangerous");
                	if (!TryLand())
                		btnArm.setSelected(true);
        		}
        		else {
        			loggerDisplayerSvc.logOutgoing("disarm");
        			MavLinkArm.sendArmMessage(drone, false);
        		}
        	}
        });
        pnl.add(btnArm);
        
        btnLandRTL = new JButton("Land/RTL");
        btnLandRTL.addActionListener( e -> TryLand());
        pnl.add(btnLandRTL);
        
        btnTakeoff = new JButton("Takeoff");
        btnTakeoff.addActionListener( e -> {
    		if (takeOffThreadRunning) {
    			JOptionPane.showMessageDialog(null, "Takeoff procedure was already started");
    			return;
    		}
    		takeOffThreadRunning = true;
    		takeOffThread = new SwingWorker<Void, Void>(){
    			
    			@Override
       			protected Void doInBackground() throws Exception {
    				Logger.LogGeneralMessege("Takeoff thread Stated!");
    				
	        		if (!drone.getState().isArmed()) {
						JOptionPane.showMessageDialog(null, "Quad will automatically be armed");
					}
	        		
	        		Object val = JOptionPane.showInputDialog(null, "Choose altitude", "", JOptionPane.OK_CANCEL_OPTION, null, null, 5);
	        		if (val == null) {
	        			System.out.println(getClass().getName() + " Takeoff canceled");
	        			return null;
	        		}
	        		
	        		try {
	        			double real_value = Double.parseDouble((String) val);
	        			opTakeoffQuad.setTargetHeight(real_value);
						opArmQuad.setNext(opTakeoffQuad);
						opTakeoffQuad.setNext(null);
						opArmQuad.go();
	        		}
	        		catch (Exception e) {
	        			JOptionPane.showMessageDialog(null, "Failed to get required height for value '" + val + "'\n" + e.getMessage());
	        		}
	        		return null;
    			}
    			

       			@Override
                protected void done() {
       				takeOffThreadRunning = false;
       				Logger.LogGeneralMessege("Takeoff thread Done!");
                }
    		};
    		takeOffThread.execute();
        });
        pnl.add(btnTakeoff);
        
        btnFollowBeaconShow = new JButton("Show Beacon");
        btnFollowBeaconShow.addActionListener( e -> {        		
    		SwingWorker<Void, Void> w = new SwingWorker<Void, Void>(){
				
       			@Override
       			protected Void doInBackground() throws Exception {	       				
					drone.getBeacon().syncBeacon();
					return null;
				}
			};
   		
			w.execute();
        });
        pnl.add(btnFollowBeaconShow);
        
        btnFollowBeaconStart = new JToggleButton("Lock on Beacon");
        btnFollowBeaconStart.addActionListener( e -> {
    		if (btnFollowBeaconStart.isSelected()) {        			
    			FollowBeaconStartThread = new SwingWorker<Void, Void>(){
    				@Override
	       			protected Void doInBackground() throws Exception {
		        		if (!drone.getState().isArmed())
							JOptionPane.showMessageDialog(null, "Quad will automatically be armed");
		        		
		        		if (drone.getState().isFlying()) {
		        			drone.getFollow().toggleFollowMeState();
		        			return null;
		        		}
		        		
		        		Object val = JOptionPane.showInputDialog(null, "Choose altitude", "", JOptionPane.OK_CANCEL_OPTION, null, null, 5);
		        		if (val == null) {
		        			loggerDisplayerSvc.logError("Takeoff canceled");
		        			btnFollowBeaconStart.setSelected(false);				        			
		        			return null;
		        		}
	        		
		        		try {
		        			double real_value = Double.parseDouble((String) val);
		        			opArmQuad.setNext(opTakeoffQuad);
		        			opTakeoffQuad.setTargetHeight(real_value);
		        			opTakeoffQuad.setNext(null);
		        			
        					if (!opArmQuad.go()) {
        						btnFollowBeaconStart.setSelected(false);
        						return null;
        					}
		        			
		        			drone.getFollow().toggleFollowMeState();
		        		}
		        		catch (InterruptedException e) {
		        			JOptionPane.showMessageDialog(null, "Beacon lock operation was canceled");
		        		}
		        		catch (Exception e) {
		        			JOptionPane.showMessageDialog(null, "Failed to get required height for value '" + val + "'\n" + e.getMessage());
		        		}
						return null;
    				}
    			};
    			FollowBeaconStartThread.execute();
    		}
    		else {
    			// Not selected
    			drone.getFollow().toggleFollowMeState();
    			loggerDisplayerSvc.logError("Lock On Beacon is not selected");
        		if (FollowBeaconStartThread != null)
        			FollowBeaconStartThread.cancel(true);
        		
        		FollowBeaconStartThread = null;
        	}
        });
        pnl.add(btnFollowBeaconStart);
        
        btnGCSShow = new JButton("Get GCS Position");
        btnGCSShow.addActionListener( e -> {
    		SwingWorker<Void, Void> w = new SwingWorker<Void, Void>(){
				
       			@Override
       			protected Void doInBackground() throws Exception {
					GCSLocationData gcslocation = GCSLocationData.fetch();
					if (gcslocation == null) {
						loggerDisplayerSvc.logError("Failed to get beacon point from the web");
						return null;
					}
					drone.getGCS().setPosition(gcslocation.getCoordinate());
					drone.getGCS().UpdateAll();
					return null;
				}
			};
   		
			w.execute();
        });
        pnl.add(btnGCSShow);
        
        btnHoldPosition = new JButton("Hold Position");
        btnHoldPosition.addActionListener( e -> TryPoshold());
        btnHoldPosition.setEnabled(false);
        pnl.add(btnHoldPosition);
        
        btnStartMission = new JToggleButton("Start Mission");
        btnStartMission.addActionListener( e -> {
        	if (btnStartMission.isSelected()) {
        		try {
        			opStartMissionQuad.setMission(drone.getMission());
        			opStartMissionQuad.setNext(null);
					if (!opStartMissionQuad.go())
						btnStartMission.setSelected(false);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Failed to start mission, please resolve issue and try again");
					btnStartMission.setSelected(false);
				}
        	}
        	else {
        		TryPoshold();
        	}
        });
        btnStartMission.setEnabled(false);
        pnl.add(btnStartMission);
        
        btnStartPerimeter = new JToggleButton("Start Perimeter");
        btnStartPerimeter.addActionListener( e -> drone.getPerimeter().setEnforce(btnStartPerimeter.isSelected()));
        btnStartPerimeter.setEnabled(false);
        pnl.add(btnStartPerimeter);
        
        add(pnl);
	}
	
	@PostConstruct
	public void init() {
		setButtonControl(false);
		drone.addDroneListener(this);
		drone.getParameters().addParameterListener(this);
	}

	private boolean TryLand() {
		boolean result = true;
		Object[] options = {"Land", "RTL", "Cancel"};
		int n = JOptionPane.showOptionDialog(null, "Choose Land Option", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
			    null, options, drone.getGps().isPositionValid() ? options[1] : options[0]);
		if (n == 0) {
			MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_LAND);
			loggerDisplayerSvc.logGeneral("Landing");
			textNotificationPublisher.publish("Landing");
		}
		else if(n == 1) {
			MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_RTL);
			loggerDisplayerSvc.logGeneral("Comming back to lunch position");
			textNotificationPublisher.publish("Return To Lunch");
		}
		else
			result = false;
		
		return result;
	}

	private void TryPoshold() {
		if (drone.getGps().isPositionValid()) {
			drone.getState().changeFlightMode(ApmModes.ROTOR_POSHOLD);
			loggerDisplayerSvc.logGeneral("Flight Mode set to 'Position Hold' - GPS");
		}
		else {
			drone.getState().changeFlightMode(ApmModes.ROTOR_ALT_HOLD);
			loggerDisplayerSvc.logGeneral("Flight Mode set to 'Altitude Hold' - Barometer");
		}
	}
	
	private void setButtonControl(boolean val) {
		btnArm.setEnabled(val);
		btnFly.setEnabled(val);
		btnLandRTL.setEnabled(val);
		btnTakeoff.setEnabled(val);
		btnStartMission.setEnabled(val);
		btnStartPerimeter.setEnabled(val);
		btnFollowBeaconShow.setEnabled(val);
		btnFollowBeaconStart.setEnabled(val);
		btnGCSShow.setEnabled(val);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
			case ARMING:
				boolean motorArmed = drone.getState().isArmed();
				btnArm.setSelected(motorArmed);
				btnHoldPosition.setEnabled(motorArmed);
				return;
			case FOLLOW_STOP:
				btnFollowBeaconStart.setSelected(false);
				return;
			case MODE:
				btnStartMission.setSelected(drone.getState().getMode().equals(ApmModes.ROTOR_AUTO));
				return;
			case CONNECTED:
				loggerDisplayerSvc.logGeneral("Connected");
				connected = true;
				btnConnect.setText("Disconnect");
				btnSyncDrone.setEnabled(connected);
				return;
			case DISCONNECTED:
				loggerDisplayerSvc.logGeneral("Disonnected");
				connected = false;
				btnConnect.setText("Connect");
				btnSyncDrone.setEnabled(connected);
				setButtonControl(connected);
				return;
		}
	}

	@Override
	public void onBeginReceivingParameters() {
		System.out.println(getClass() + " Start receiving parameters");
	}

	@Override
	public void onParameterReceived(Parameter parameter, int index, int count) {
		int prc = drone.getParameters().getPrecentageComplete();
		if (prc > 95)
			setButtonControl(true);
	}

	@Override
	public void onEndReceivingParameters(List<Parameter> parameter) {
		System.out.println(getClass() + " Finish receiving parameters");
		setButtonControl(true);
	}

}
