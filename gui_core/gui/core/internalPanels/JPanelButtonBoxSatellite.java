package gui.core.internalPanels;

import java.awt.LayoutManager;

import flight_controlers.KeyBoardControl;
import gui.core.dashboard.Dashboard;
import gui.core.operations.internal.ArmQuad;
import gui.core.operations.internal.TakeoffQuad;
import gui.is.services.LoggerDisplayerManager;
import gui.is.services.NotificationsManager;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import logger.Logger;
import mavlink.core.connection.helper.GCSLocationData;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.protocol.msgbuilder.MavLinkArm;
import mavlink.is.protocol.msgbuilder.MavLinkModes;
import mavlink.is.protocol.msgbuilder.MavLinkRC;

public class JPanelButtonBoxSatellite extends JToolBar implements OnDroneListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2419085692095415348L;
	
	private JPanel pnl;
	
	private JButton btnExit;
	private JButton btnSyncDrone;
	private JButton btnFly;
	private JToggleButton btnArm;
	private JButton btnLandRTL;
	private JButton btnTakeoff;
	private JButton btnGCSShow;
	private AbstractButton btnStopFollow;
	private JButton btnHoldPosition;
	private JButton btnStartMission;
	
	private boolean takeOffThreadRunning = false;
    private SwingWorker<Void, Void> takeOffThread = null;
    public JToggleButton btnFollowBeaconStart;
    private SwingWorker<Void, Void> FollowBeaconStartThread = null;
    private AbstractButton btnFollowBeaconShow;
	
	private boolean motorArmed = false;
	
	private Drone drone;

	public JPanelButtonBoxSatellite (LayoutManager gridLayout) {
		pnl = new JPanel();
		pnl.setLayout(gridLayout);
		
		btnExit = new JButton("Exit");
        btnExit.addActionListener( e -> {
        	if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Are you sure you wand to exit?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
        		System.out.println("Bye Bye");
        		Logger.LogGeneralMessege("");
        		Logger.LogGeneralMessege("Summary:");
        		Logger.LogGeneralMessege("--------");
        		Logger.LogGeneralMessege("Traveled distance: " + drone.getGps().getDistanceTraveled() + "m");
        		Logger.LogGeneralMessege("Flight time: " + drone.getState().getFlightTime() + "");
				Logger.close();
				System.exit(0);
        	}
		});
        pnl.add(btnExit);
        
        btnSyncDrone = new JButton("Sync Drone");
        btnSyncDrone.addActionListener( e -> {
        	System.out.println("Sync");
        	LoggerDisplayerManager.addGeneralMessegeToDisplay("Syncing Drone parameters");
    		drone.getParameters().refreshParameters();
        });
        pnl.add(btnSyncDrone);
        
        btnFly = new JButton("Controler: RC");
        //btnFly.addKeyListener(KeyBoardControl.get());
        btnFly.addActionListener( e -> {
        	KeyBoardControl.get().HoldIfNeeded();
        	Object[] options = {"KeyBoard", "RC Controller"};
        	int n = JOptionPane.showOptionDialog(null, "Choose Controler", "",
        		    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
        		    options, options[1]); //default button title
        	if (n == 0) {
        		KeyBoardControl.get().ReleaseIfNeeded();
        		KeyBoardControl.get().Activate();
        		int eAvg = drone.getRC().getAverageThrust();
        		LoggerDisplayerManager.addGeneralMessegeToDisplay("Setting Keyboard Thrust starting value to " + eAvg);
        		KeyBoardControl.get().SetThrust(eAvg);
        		btnFly.setText("Controler: Keyboard");
        	}
        	else {
        		KeyBoardControl.get().ReleaseIfNeeded();
        		KeyBoardControl.get().Deactivate();
        		int[] rcOutputs = {0, 0, 0, 0, 0, 0, 0, 0};
        		MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
        		try {
					Thread.sleep(200);
					MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
            		Thread.sleep(200);
            		MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
        		btnFly.setText("Controler: RC");
        	}
        	LoggerDisplayerManager.addGeneralMessegeToDisplay("Start Fly '" + options[n] + "'");
        });
        pnl.add(btnFly);        
        
        btnArm = new JToggleButton("Arm Motors");
        btnArm.addActionListener( e -> {
        	if (btnArm.isSelected()) {
        		motorArmed = true;
        		LoggerDisplayerManager.addOutgoingMessegeToDisplay("arm");
        		MavLinkArm.sendArmMessage(drone, true);
        	}
        	else {
        		// Not selected
        		if (drone.getState().isFlying()) {
        			Object[] options = {"Land", "RTL", "Cancel"};
                	int n = JOptionPane.showOptionDialog(null, "Drone is flying, dis-arming motor is dangeures, what what you like to do?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                		    null, options, options[0]);
                	if (n == 0) {
                		MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_LAND);
                		LoggerDisplayerManager.addGeneralMessegeToDisplay("Landing");
                		NotificationsManager.add("Landing");
                	}
                	else if (n == 1) {
                		MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_RTL);
                		LoggerDisplayerManager.addGeneralMessegeToDisplay("RTL");
                		NotificationsManager.add("RTL");
                	}
                	else 
                		btnArm.setSelected(true);
        		}
        		else {
        			motorArmed = false;
        			LoggerDisplayerManager.addOutgoingMessegeToDisplay("disarm");
        			MavLinkArm.sendArmMessage(drone, false);
        		}
        	}
        });
        pnl.add(btnArm);
        
        btnLandRTL = new JButton("Land/RTL");
        btnLandRTL.addActionListener( e -> {
    		KeyBoardControl.get().HoldIfNeeded();
    		Object[] options = {"Land", "RTL", "Cancel"};
        	int n = JOptionPane.showOptionDialog(null, "Choose Land Option", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
        		    null, options, drone.getGps().isPositionValid() ? options[1] : options[0]);
        	if (n == 0) {
        		MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_LAND);
        		LoggerDisplayerManager.addGeneralMessegeToDisplay("Landing");
        		NotificationsManager.add("Landing");
        	}
        	else if(n == 1) {
        		MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_RTL);
        		LoggerDisplayerManager.addGeneralMessegeToDisplay("Comming back to lunch position");
        		NotificationsManager.add("Return To Lunch");
        	}
        	KeyBoardControl.get().ReleaseIfNeeded();
        });
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
    				
	        		if (!Dashboard.drone.getState().isArmed()) {
						JOptionPane.showMessageDialog(null, "Quad will automatically be armed");
					}
	        		
	        		Object val = JOptionPane.showInputDialog(null, "Choose altitude", "", JOptionPane.OK_CANCEL_OPTION, null, null, 5);
	        		if (val == null) {
	        			System.out.println(getClass().getName() + " Takeoff canceled");
	        			return null;
	        		}
	        		
	        		try {
	        			double real_value = Double.parseDouble((String) val);
	        			System.out.println(getClass().getName() + " Required hight is " + real_value);
	        			if (real_value < 0 || real_value > 15) {
	        				JOptionPane.showMessageDialog(null, "Altitude limition during takeoff is limited to 15");
	        				return null;
	        			}
	        			
	        			ArmQuad armQuad = new ArmQuad(drone);
						TakeoffQuad toff = new TakeoffQuad(drone, real_value);
						armQuad.setNext(toff);
	        			armQuad.go();
	        		}
	        		catch (NumberFormatException e) {
	        			JOptionPane.showMessageDialog(null, "Failed to get required height for value '" + val + "'");
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
		        		if (!Dashboard.drone.getState().isArmed()) {
							JOptionPane.showMessageDialog(null, "Quad will automatically be armed");
						}
		        		
		        		if (!Dashboard.drone.getState().isFlying()) {
			        		Object val = JOptionPane.showInputDialog(null, "Choose altitude", "", JOptionPane.OK_CANCEL_OPTION, null, null, 5);
			        		if (val == null) {
			        			System.out.println(getClass().getName() + " Takeoff canceled");
			        			btnFollowBeaconStart.setSelected(false);				        			
			        			return null;
			        		}
		        		
			        		try {
			        			double real_value = Double.parseDouble((String) val);
			        			System.out.println(getClass().getName() + " Required height is " + real_value);
			        			if (real_value < 0 || real_value > 15) {
			        				JOptionPane.showMessageDialog(null, "Altitude limition during takeoff is limited to 15");
			        				btnFollowBeaconStart.setSelected(false);
			        				return null;
			        			}
			        			
			        			ArmQuad armQuad = new ArmQuad(drone);
	        					if (!armQuad.go()) {
	        						btnFollowBeaconStart.setSelected(false);
	        						return null;
	        					}
			        			
			        			TakeoffQuad toff = new TakeoffQuad(drone, real_value);
			        			if (!toff.go()) {
        							btnFollowBeaconStart.setSelected(false);
        							return null;
        						}
    		        			
    		        			//ActivateBeacon();
			        			drone.getFollow().toggleFollowMeState();
			        		}
			        		catch (NumberFormatException e) {
			        			JOptionPane.showMessageDialog(null, "Failed to get required height for value '" + val + "'");
			        		}
			        		catch (InterruptedException e) {
			        			JOptionPane.showMessageDialog(null, "Beacon lock operation was cancel");
			        		}
			        		catch (Exception e) {
			        			JOptionPane.showMessageDialog(null, "Failed to get required height for value '" + val + "'\n" + e.getMessage());
			        		}
		        		}
		        		else {
							//ActivateBeacon();
		        			drone.getFollow().toggleFollowMeState();
		        		}
						return null;
    				}
    			};
    			FollowBeaconStartThread.execute();
    		}
    		else {
    			// Not selected
    			//DeactivateBeacon();
    			drone.getFollow().toggleFollowMeState();
    			LoggerDisplayerManager.addErrorMessegeToDisplay("Not Selected");
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
						LoggerDisplayerManager.addErrorMessegeToDisplay("Failed to get beacon point from the web");
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
        
        btnStopFollow = new JButton("Kill Follow");
        btnStopFollow.addActionListener( e -> {        		
    		SwingWorker<Void, Void> w = new SwingWorker<Void, Void>(){
				
       			@Override
       			protected Void doInBackground() throws Exception {
       				//DeactivateBeacon();
					return null;
				}
			};
   		
			w.execute();
        });
        pnl.add(btnStopFollow);
        
        btnHoldPosition = new JButton("Hold Position");
        btnHoldPosition.addActionListener( e -> {
    		if (!drone.getState().isArmed()) {
    			LoggerDisplayerManager.addErrorMessegeToDisplay("Quad must be armed in order to change this mode");
    			return;
    		}
    		
    		if (drone.getGps().isPositionValid()) {
    			drone.getState().changeFlightMode(ApmModes.ROTOR_POSHOLD);
    			LoggerDisplayerManager.addGeneralMessegeToDisplay("Flight Mode set to 'Position Hold' - GPS");
    		}
    		else {
    			drone.getState().changeFlightMode(ApmModes.ROTOR_ALT_HOLD);
    			LoggerDisplayerManager.addGeneralMessegeToDisplay("Flight Mode set to 'Altitude Hold' - Barometer");
    		}
        });
        btnHoldPosition.setEnabled(false);
        pnl.add(btnHoldPosition);
        
        btnStartMission = new JButton("Start Mission");
        btnStartMission.addActionListener( e -> drone.getState().changeFlightMode(ApmModes.ROTOR_AUTO));
        btnStartMission.setEnabled(false);
        pnl.add(btnStartMission);
        
        add(pnl);
	}
	
	public void setButtonControl(boolean val) {
		btnArm.setEnabled(val);
		btnFly.setEnabled(val);
		btnLandRTL.setEnabled(val);
		btnTakeoff.setEnabled(val);
		btnHoldPosition.setEnabled(val);
		btnStartMission.setEnabled(val);
		btnFollowBeaconShow.setEnabled(val);
		btnFollowBeaconStart.setEnabled(val);
		btnGCSShow.setEnabled(val);
		btnStopFollow.setEnabled(val);
	}

	public void setDrone(Drone drone) {
		this.drone = drone;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
			case ARMING:
				motorArmed = drone.getState().isArmed();
				btnArm.setSelected(motorArmed);
				return;
			case FOLLOW_STOP:
				btnFollowBeaconStart.setSelected(false);
				return;
		}
	}

}
