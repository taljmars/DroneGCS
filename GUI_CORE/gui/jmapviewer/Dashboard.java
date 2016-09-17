package gui.jmapviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import flight_controlers.KeyBoardControl;
import gui.jmapviewer.InternalFrames.JInternalFrameActualPWM;
import gui.jmapviewer.InternalFrames.JInternalFrameMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLEditorKit;

import logger.Logger;

import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.MAVLink.MavLinkModes;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.MAVLink.MavLinkRC;
import org.droidplanner.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.core.MAVLink.connection.RadioConnection;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.MyDroneImpl;
import org.droidplanner.core.gcs.GCSHeartbeat;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.Drone;
import org.json.simple.JSONObject;

import desktop.logic.*;

import javax.swing.JProgressBar;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import com.MAVLink.Messages.ApmModes;

import javax.swing.JDesktopPane;

import java.awt.Font;

import javax.swing.SwingConstants;

import json.JSONHelper;

public class Dashboard implements OnDroneListener{

	private JFrame frame;
	
	private static RadioConnection radConn;
	public static Drone drone;

    @SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private static final int LOG_BOX_MAX_LINES = 50;//7;

        
    private static JTextPane logBox;
    public JToggleButton areaLogLockTop;
    
    private JLabel keepAliveLabel;
    private JLabel lblRoll;
    private JLabel lblPitch;
    private JLabel lblThrust;
    private JLabel lblYaw;
    private JLabel lblSignal;
    private JLabel lblCriticalMsg;
    
    private JPanel telemetryPanel;
    
    private static JLabel lblEngine1;
    private static JLabel lblEngine2;    
    private static JLabel lblEngine3;
    private static JLabel lblEngine4;

    private JButton btnExit;
    private JButton btnSyncDrone;
    private JButton btnFly;
    private JToggleButton btnArm;
    private JButton btnLandRTL;
    private JButton btnTakeoff;
    private boolean takeOffThreadRunning = false;
    private SwingWorker<Void, Void> takeOffThread = null;
    public JToggleButton btnFollowBeaconStart;
    private SwingWorker<Void, Void> FollowBeaconStartThread = null;
    private JButton btnFollowBeaconShow;
    private JButton btnGCSShow;
    private JButton btnStopFollow;
    private JButton btnHoldPosition;
    private JButton btnStartMission;
    public static JButton btnMap;
    public static JButton btnActualPWM;
    
    private JToolBar tbTelemtry;
	private JToolBar tbContorlButton;
	private JTabbedPane tbSouth;
	private JPanel pnlConfiguration;
	private JToolBar toolBar;
    
	private JProgressBar progressBar;
	
	private JLabel lblHeight;
	private JLabel lblHeightVal;
	private JLabel lblBattery;
    private JLabel lblBatteryVal;
    private JLabel lblFlightMode;
    private JLabel lblFlightModeVal;
    private JLabel lblFlightTime;
    private JLabel lblFlightTimeVal;
    private JLabel lblFlightDistance;
    private JLabel lblFlightDistanceVal;
    
    private boolean motorArmed = false;
    public JScrollPane areaMission = null;
    
    JCheckBox cbActiveGeofencePerimeterEnforce = null;
    JCheckBox cbActiveGeofencePerimeterAlert = null;
	
	public static Dashboard window = null;
	
	protected static MavLinkMsgHandler mavlinkHandler;
	
    public static JDesktopPane desktopPane;
	
	/**
	 * Launch the application.
	 */
	
	static // HeartBeat Mechanism
	Runnable hbRunnable = new Runnable() {
		public void run() {
			System.out.println("HeartBeat thread Started");
				
			while (true) {
				try {
					Thread.sleep(1000);
					Dashboard window = Dashboard.window;
					
					String msg = MessegeQueue_Pop();
					if (msg != null) {
						window.lblCriticalMsg.setVisible(true);
						if (window.lblCriticalMsg.getBackground() != Color.BLUE) {
							window.lblCriticalMsg.setBackground(Color.BLUE);
							window.lblCriticalMsg.setForeground(Color.WHITE);
						}
						else {
							window.lblCriticalMsg.setBackground(Color.YELLOW);
							window.lblCriticalMsg.setForeground(Color.BLACK);
						}
						window.lblCriticalMsg.setText("   " + msg + "   ");
						window.lblCriticalMsg.setOpaque(true);
					}
					else {
						window.lblCriticalMsg.setVisible(false);
					}
									
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
		}
	};
		
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					radConn = new RadioConnection();
					radConn.connect();
					
					Handler handler = new desktop.logic.Handler();
					drone = new MyDroneImpl(radConn, new Clock(), handler,FakeFactory.fakePreferences());
					mavlinkHandler = new MavLinkMsgHandler(drone);
					
					window = new Dashboard();
					window.frame.setVisible(true);
					System.out.println("Start HeartBeat mechanism");
					//( new Thread( HeartBeat.get() ) ).start();
					( new Thread( hbRunnable ) ).start();
					
					GCSHeartbeat gcs = new GCSHeartbeat(drone, 1);
					gcs.setActive(true);
					
					System.out.println("Start Outgoing Communication");
					KeyBoardControl.get();
					
					System.out.println("Sign Dashboard as drone listener");
					drone.addDroneListener(window);
					//drone = new DroneImpl(null, null, null, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void SetDistanceToWaypoint(double d) {
		if (drone.getState().getMode().equals(ApmModes.ROTOR_GUIDED)) {
			//if (drone.getGuidedPoint().isIdle()) {
			if (d == 0) {
				MessegeQueue_Add("In Position");
				addGeneralMessegeToDisplay("Guided: In Position");
			}
			else {
				MessegeQueue_Add("Flying to destination");
				addGeneralMessegeToDisplay("Guided: Fly to distination");
			}
		}
	}

	protected void SetFlightModeLabel(String name) {
		lblFlightModeVal.setText(name);
	}

	/**
	 * Create the application.
	 */
	public Dashboard() {
		initialize();
		setButtonControl(false);
		if (drone.isConnectionAlive()) {
			SetHeartBeat(true);
			SetFlightModeLabel(drone.getState().getMode().getName());
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		//frame = new Demo();
		frame.setBounds(100, 100, 450, 300);
		
		//super("Quad Ground Station");
        frame.setSize(400, 400);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel eastPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        desktopPane = new JDesktopPane();
        frame.getContentPane().add(desktopPane, BorderLayout.CENTER);
        
        JInternalFrameMap.Generate();
               
        
        JPanel pnlNorth = new JPanel();
        frame.getContentPane().add(pnlNorth, BorderLayout.NORTH);
        pnlNorth.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        btnMap = new JButton("Map");
        btnMap.setSelected(true);
        pnlNorth.add(btnMap);
        btnMap.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent arg0) {
        		if (arg0.getID() == MouseEvent.MOUSE_CLICKED) {
        			JInternalFrameMap.Generate();
        		}
        	}
        });
        
        btnActualPWM = new JButton("Actual PWM");
        pnlNorth.add(btnActualPWM);
        
        lblCriticalMsg = new JLabel("MSG");
        lblCriticalMsg.setHorizontalAlignment(SwingConstants.TRAILING);
        pnlNorth.add(lblCriticalMsg);
        btnActualPWM.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent arg0) {
        		if (arg0.getID() == MouseEvent.MOUSE_CLICKED) {
        			JInternalFrameActualPWM.Generate();
        		}
        	}
        });
        
        JPanel southPanel = new JPanel(new BorderLayout());
        frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        southPanel.add(progressBar, BorderLayout.SOUTH);
        
        toolBar = new JToolBar();
        southPanel.add(toolBar, BorderLayout.CENTER);
        
        tbSouth = new JTabbedPane(JTabbedPane.TOP);
        toolBar.add(tbSouth);
        
        JPanel pnlLogBox = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        logBox = new JTextPane();
        logBox.setLayout(new GridLayout(1,1,0,0));
        JPanel pnlLogbox = new JPanel(new GridLayout(1,1));
        JScrollPane logbox = new JScrollPane(logBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension southPanelDimension = new Dimension(1200, 150);
        
        logbox.setPreferredSize(southPanelDimension);
        logBox.setEditorKit(new HTMLEditorKit());
        logBox.setEditable(false);
        pnlLogbox.add(logbox);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 1.0;	// request any extra vertical space
        pnlLogBox.add(pnlLogbox, c);
        
        JPanel pnlLogToolBox = new JPanel(new GridLayout(0,1,0,0));     
        areaLogLockTop = new JToggleButton("Top");
        areaLogLockTop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (areaLogLockTop.isSelected()) {
					logbox.getVerticalScrollBar().setValue(0);
				}
			}
		});
        pnlLogToolBox.add(areaLogLockTop);
        
        JButton areaLogClear = new JButton("CLR");
        areaLogClear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				logBox.setText("");
			}
		});
        pnlLogToolBox.add(areaLogClear);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 1.0;   //request any extra vertical space
        pnlLogBox.add(pnlLogToolBox, c);
        
        tbSouth.addTab("Log Book", null, pnlLogBox, null);
        
        pnlConfiguration = new JPanel();
        JScrollPane areaConfiguration = new JScrollPane(pnlConfiguration, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        areaConfiguration.setPreferredSize(southPanelDimension);
        
        cbActiveGeofencePerimeterAlert = new JCheckBox("Active GeoFence/Perimeter Alert");
        cbActiveGeofencePerimeterAlert.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cbActiveGeofencePerimeterAlert.isSelected()) {
					addGeneralMessegeToDisplay("Enable perimeter alert");
					drone.getPerimeter().setAlert(true);
				}
				else {
					addGeneralMessegeToDisplay("Disable perimeter alert");
					drone.getPerimeter().setAlert(false);
				}
			}
		});
        
        cbActiveGeofencePerimeterEnforce = new JCheckBox("Active GeoFence/Perimeter Enforcement");
        cbActiveGeofencePerimeterEnforce.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cbActiveGeofencePerimeterEnforce.isSelected()) {
					addGeneralMessegeToDisplay("Enable Perimeter enforcement");
					drone.getPerimeter().setEnforce(true);
				}
				else {
					addGeneralMessegeToDisplay("Disable Perimeter enforcement");
					drone.getPerimeter().setEnforce(false);
				}
			}
		});
        cbActiveGeofencePerimeterAlert.setSelected(false);
        cbActiveGeofencePerimeterEnforce.setSelected(false);
        pnlConfiguration.add(cbActiveGeofencePerimeterAlert);
        pnlConfiguration.add(cbActiveGeofencePerimeterEnforce);
        
        /*FlowLayout flowLayout = (FlowLayout) pnlConfiguration.getLayout();
        flowLayout.setAlignOnBaseline(true);
        flowLayout.setAlignment(FlowLayout.LEFT);*/
        tbSouth.addTab("Configuration", null, areaConfiguration, null);
        
        areaMission = new JScrollPane(null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        areaMission.setPreferredSize(southPanelDimension);
        tbSouth.addTab("Mission", null, areaMission, null);
        
        frame.getContentPane().add(eastPanel, BorderLayout.EAST);
        //for (int i =0 ; i < LOG_BOX_MAX_LINES ; i++) addMessegeToDisplay("-", Type.GENERAL, true);
       
        ////////////////////////////////////////////////////
        ////////////////////////////////////////////////////
        /////////////////  Control Panel  //////////////////
        
        /*JComboBox<String> algo_combo = new JComboBox<>(new String[] {
        		KeyBoardControl.ModesTitles.get(Modes.ARDUCOPTER), KeyBoardControl.ModesTitles.get(Modes.WIFI), "NONE"});
        algo_combo.addItemListener(new ItemListener() {
        	@Override
            public void itemStateChanged(ItemEvent e) {
        		if (e.getStateChange() == e.SELECTED) {
        			JComboBox cb = (JComboBox) e.getSource();
        			String text_sel = (String)cb.getSelectedItem();
        			KeyBoardControl.get().SetModeByString(text_sel);
        			System.err.println("SELECTED " + text_sel);
        		}
            }
        });*/
        
        tbContorlButton = new JToolBar();
        eastPanel.add(tbContorlButton);
        
        JPanel eastPanel_buttons = new JPanel(new GridLayout(0, 2, 1, 1));
        tbContorlButton.add(eastPanel_buttons);
        
        btnExit = new JButton("Exit");
        btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { 
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
			}
		});
        eastPanel_buttons.add(btnExit);
        
        btnSyncDrone = new JButton("Sync Drone");
        btnSyncDrone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
        		addGeneralMessegeToDisplay("Syncing Drone parameters");
        		resetProgressBar();
        		drone.getParameters().refreshParameters();
            }
        });
        eastPanel_buttons.add(btnSyncDrone);
        
        btnFly = new JButton("Controler: RC");
        //btnFly.addKeyListener(KeyBoardControl.get());
        btnFly.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	KeyBoardControl.get().HoldIfNeeded();
            	Object[] options = {"KeyBoard", "RC Controller"};
            	int n = JOptionPane.showOptionDialog(null, "Choose Controler", "",
            		    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
            		    options, options[1]); //default button title
            	if (n == 0) {
            		KeyBoardControl.get().ReleaseIfNeeded();
            		KeyBoardControl.get().Activate();
            		int e1 = Integer.parseInt(lblEngine1.getText());
            		int e2 = Integer.parseInt(lblEngine2.getText());
            		int e3 = Integer.parseInt(lblEngine3.getText());
            		int e4 = Integer.parseInt(lblEngine4.getText());
            		int eAvg = (e1 + e2 + e3 + e4) / 4;
            		addGeneralMessegeToDisplay("Setting Keyboard Thrust starting value to " + eAvg);
            		KeyBoardControl.get().SetThrust(eAvg);
            		btnFly.setText("Controler: Keyboard");
            	}
            	else {
            		KeyBoardControl.get().ReleaseIfNeeded();
            		KeyBoardControl.get().Deactivate();
            		lblRoll.setText("---");
            		lblPitch.setText("---");
            		lblThrust.setText("---");
            		lblYaw.setText("---");
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
            	addGeneralMessegeToDisplay("Start Fly '" + options[n] + "'");
            }
        });
        eastPanel_buttons.add(btnFly);        
        
        btnArm = new JToggleButton("Arm Motors");
        btnArm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (btnArm.isSelected()) {
            		motorArmed = true;
            		addOutgoingMessegeToDisplay("arm");
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
                    		addGeneralMessegeToDisplay("Landing");
                    		MessegeQueue_Add("Landing");
                    	}
                    	else if (n == 1) {
                    		MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_RTL);
                    		addGeneralMessegeToDisplay("RTL");
                    		MessegeQueue_Add("RTL");
                    	}
                    	else 
                    		btnArm.setSelected(true);
            		}
            		else {
            			motorArmed = false;
            			addOutgoingMessegeToDisplay("disarm");
            			MavLinkArm.sendArmMessage(drone, false);
            		}
            	}
            }
        });
        eastPanel_buttons.add(btnArm);
        
        btnLandRTL = new JButton("Land/RTL");
        btnLandRTL.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		KeyBoardControl.get().HoldIfNeeded();
        		Object[] options = {"Land", "RTL", "Cancel"};
            	int n = JOptionPane.showOptionDialog(null, "Choose Land Option", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
            		    null, options, drone.getGps().isPositionValid() ? options[1] : options[0]);
            	if (n == 0) {
            		MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_LAND);
            		addGeneralMessegeToDisplay("Landing");
            		MessegeQueue_Add("Landing");
            	}
            	else if(n == 1) {
            		MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_RTL);
            		addGeneralMessegeToDisplay("Comming back to lunch position");
            		MessegeQueue_Add("Return To Lunch");
            	}
            	KeyBoardControl.get().ReleaseIfNeeded();
        	}
        });
        eastPanel_buttons.add(btnLandRTL);
        
        btnTakeoff = new JButton("Takeoff");
        btnTakeoff.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
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
		        			
    						if (!drone.getState().isArmed() && !ArmQuad())
    							return null;
		        			
		        			if (!TakeoffQuad(real_value))
		        				return null;
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
        	}
        });
        eastPanel_buttons.add(btnTakeoff);
        
        btnFollowBeaconShow = new JButton("Show Beacon");
        btnFollowBeaconShow.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {        		
        		SwingWorker<Void, Void> w = new SwingWorker<Void, Void>(){
					
	       			@Override
	       			protected Void doInBackground() throws Exception {	       				
						drone.getBeacon().syncBeacon();
						return null;
					}
				};
       		
				w.execute();
        	}
        });
        eastPanel_buttons.add(btnFollowBeaconShow);
        
        btnFollowBeaconStart = new JToggleButton("Lock on Beacon");
        btnFollowBeaconStart.addActionListener(new ActionListener() {        	
        	public void actionPerformed(ActionEvent arg0) {
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
				        			
				        			if (!drone.getState().isArmed()) {
		        						if (!ArmQuad()) {
		        							btnFollowBeaconStart.setSelected(false);
		        							return null;
		        						}
				        			}
				        			
				        			if (!TakeoffQuad(real_value)) {
	        							btnFollowBeaconStart.setSelected(false);
	        							return null;
	        						}
	    		        			
	    		        			ActivateBeacon();
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
								ActivateBeacon();
			        		}
							return null;
        				}
        			};
        			FollowBeaconStartThread.execute();
        		}
        		else {
        			// Not selected
        			DeactivateBeacon();
            		addErrorMessegeToDisplay("Not Selected");
            		if (FollowBeaconStartThread != null)
            			FollowBeaconStartThread.cancel(true);
            		
            		FollowBeaconStartThread = null;
            	}
        	}
        });
        eastPanel_buttons.add(btnFollowBeaconStart);
        
        btnGCSShow = new JButton("Get GCS Position");
        btnGCSShow.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {        		
        		SwingWorker<Void, Void> w = new SwingWorker<Void, Void>(){
					
	       			@Override
	       			protected Void doInBackground() throws Exception {
						JSONObject obj = JSONHelper.makeHttpPostRequest("http://www.sparksapp.eu/public_scripts/QuadGetHomePosition.php");
						if (obj == null) {
							addErrorMessegeToDisplay("Failed to get beacon point from the web");
							return null;
						}
						double lat = Double.parseDouble((String) obj.get("Lat"));
						double lon = Double.parseDouble((String) obj.get("Lng"));
						double alt = Double.parseDouble((String) obj.get("Z"));
						drone.getGCS().setPosition(new Coord3D(new Coord2D(lat, lon), new Altitude(alt)));
						drone.getGCS().UpdateAll();
						return null;
					}
				};
       		
				w.execute();
        	}
        });
        eastPanel_buttons.add(btnGCSShow);
        
        btnStopFollow = new JButton("Kill Follow");
        btnStopFollow.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {        		
        		SwingWorker<Void, Void> w = new SwingWorker<Void, Void>(){
					
	       			@Override
	       			protected Void doInBackground() throws Exception {
	       				DeactivateBeacon();
						return null;
					}
				};
       		
				w.execute();
        	}
        });
        eastPanel_buttons.add(btnStopFollow);
        
        btnHoldPosition = new JButton("Hold Position");
        btnHoldPosition.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if (!drone.getState().isArmed()) {
        			addErrorMessegeToDisplay("Quad must be armed in order to change this mode");
        			return;
        		}
        		
        		if (drone.getGps().isPositionValid()) {
        			drone.getState().changeFlightMode(ApmModes.ROTOR_POSHOLD);
        			addGeneralMessegeToDisplay("Flight Mode set to 'Position Hold' - GPS");
        		}
        		else {
        			drone.getState().changeFlightMode(ApmModes.ROTOR_ALT_HOLD);
        			addGeneralMessegeToDisplay("Flight Mode set to 'Altitude Hold' - Barometer");
        		}
        	}
        });
        btnHoldPosition.setEnabled(false);
        eastPanel_buttons.add(btnHoldPosition);
        
        btnStartMission = new JButton("Start Mission");
        btnStartMission.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		addGeneralMessegeToDisplay("Start Mission - Change to " + ApmModes.ROTOR_AUTO.getName());
        		drone.getState().changeFlightMode(ApmModes.ROTOR_AUTO);
        	}
        });
        
        btnStartMission.setEnabled(false);
        eastPanel_buttons.add(btnStartMission);
        
        tbTelemtry = new JToolBar();
        eastPanel.add(tbTelemtry);
        
        telemetryPanel = new JPanel();
        tbTelemtry.add(telemetryPanel);
        telemetryPanel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        telemetryPanel.setLayout(new BoxLayout(telemetryPanel, BoxLayout.PAGE_AXIS));
        
        JPanel pnlStatus = new JPanel();
        telemetryPanel.add(pnlStatus);
        
        JLabel lblStatus = new JLabel("Status:");
        pnlStatus.add(lblStatus);
        
        //eastPanel_buttons.add(algo_combo);
        
        keepAliveLabel = new JLabel("Disonnected");
        pnlStatus.add(keepAliveLabel);
        keepAliveLabel.setForeground(Color.RED);
        
        lblFlightMode = new JLabel("Mode:");
        pnlStatus.add(lblFlightMode);
        
        lblFlightModeVal = new JLabel("Unknown");
        lblFlightModeVal.setFont(new Font("Tahoma", Font.BOLD, 11));
        pnlStatus.add(lblFlightModeVal);
        
        JPanel pnlSignal = new JPanel();
        telemetryPanel.add(pnlSignal);
        
        lblBattery = new JLabel("Battery:");
        pnlSignal.add(lblBattery);
        
        lblBatteryVal = new JLabel("0");
        pnlSignal.add(lblBatteryVal);
        
        JLabel lblSignal_1 = new JLabel("Signal:");
        pnlSignal.add(lblSignal_1);
        
        lblSignal = new JLabel("0%");
        pnlSignal.add(lblSignal);
        
        lblHeight = new JLabel("Height:");
        pnlSignal.add(lblHeight);
        
        lblHeightVal = new JLabel("0m");
        pnlSignal.add(lblHeightVal);
        
        JPanel pnlRCSend = new JPanel();
        telemetryPanel.add(pnlRCSend);
        
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
        
        JPanel pnlRCActual = new JPanel();
        telemetryPanel.add(pnlRCActual);
        
        JPanel pnlStatisticsTime = new JPanel();
        lblFlightTime = new JLabel("Flight Time:");
        lblFlightTimeVal = new JLabel("-");
        pnlStatisticsTime.add(lblFlightTime);
        pnlStatisticsTime.add(lblFlightTimeVal);
        telemetryPanel.add(pnlStatisticsTime);
        
        JPanel pnlStatisticsDistanceTraveled = new JPanel();
        lblFlightDistance = new JLabel("Flight Distance:");
        lblFlightDistanceVal = new JLabel("0m");
        pnlStatisticsDistanceTraveled.add(lblFlightDistance);
        pnlStatisticsDistanceTraveled.add(lblFlightDistanceVal);
        telemetryPanel.add(pnlStatisticsDistanceTraveled);
        
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
	}
	
	private boolean ArmQuad() throws InterruptedException {
		addGeneralMessegeToDisplay("Arming Quad");
		MavLinkArm.sendArmMessage(Dashboard.drone, true);
		int armed_waiting_time = 5000; // 5 seconds
		long sleep_time = 1000;
		int retry = (int) (armed_waiting_time / sleep_time);
		while (retry > 0) {
			if (drone.getState().isArmed())
				break;
			System.out.println("Sleeps for " + sleep_time + " ms (retries " + retry + ")");
			addGeneralMessegeToDisplay("Waiting for arming approval (" + retry + ")");
			Thread.sleep(sleep_time);
			retry--;
		}
		
		if (retry <= 0) {
			JOptionPane.showMessageDialog(null, "Failed to arm quadcopter, taking off was canceled");
			System.out.println(getClass().getName() + "Failed to arm quadcopter, taking off was canceled");
			addErrorMessegeToDisplay("Failed to arm quad");
			return false;
		}
		
		return true;
	}
	
	private boolean TakeoffQuad(double real_value) throws InterruptedException {
		addGeneralMessegeToDisplay("Starting Takeoff");
		drone.getState().doTakeoff(new Altitude(real_value));
		int takeoff_waiting_time = 15000; // 15 seconds
		long sleep_time = 1000;
		int retry = (int) (takeoff_waiting_time / sleep_time);
		while (retry > 0) {
			double alt = drone.getAltitude().getAltitude();
			if (alt >= real_value * 0.95 && alt <= real_value * 1.05 )
				break;
			System.out.println("Sleeps for " + sleep_time + " ms (retries " + retry + ")");
			addGeneralMessegeToDisplay("Waiting for takeoff to finish (" + retry + ")");
			addGeneralMessegeToDisplay("Current height: " + drone.getAltitude().getAltitude() + ", Target height: " + real_value);
			Thread.sleep(sleep_time);
			retry--;
		}
		
		if (retry <= 0) {
			JOptionPane.showMessageDialog(null, "Failed to lift quadcopter, taking off was canceled");
			System.out.println(getClass().getName() + "Failed to lift quadcopter, taking off was canceled");
			addErrorMessegeToDisplay("Failed to lift quad");
			return false;
		}
		
		addGeneralMessegeToDisplay("Takeoff done! Quad height is " + drone.getAltitude().getAltitude() + "m");
		
		return true;
	}
	
	private void ActivateBeacon() throws InterruptedException {
		int delay = 5;
		addGeneralMessegeToDisplay("Start following beacon in ...");
		while (delay > 0) {
			addGeneralMessegeToDisplay("" + delay);
			Thread.sleep(1000);
			delay--;
		}
		addGeneralMessegeToDisplay("Go");
		drone.getBeacon().setActive(true);
	}
	
	private void DeactivateBeacon() {
		addGeneralMessegeToDisplay("Stopping Follow");
		drone.getBeacon().setActive(false);
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
	
	public void ShowEngine(int roll, int pitch, int thrust, int yaw) {
		lblRoll.setText("Roll: " + roll);
		lblPitch.setText("Pitch: " + pitch);
		lblThrust.setText("Thrust: " + thrust);
		lblYaw.setText("Yaw: " + yaw);
	}
	
	public static enum Type {
		GENERAL,
		ERROR,
		INCOMING,
		OUTGOING
	};
	
	public static void addGeneralMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.GENERAL);
	}
	
	public static void addErrorMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.ERROR);
	}
	
	public static void addOutgoingMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.OUTGOING);
	}
	
	public static void addIncommingMessegeToDisplay(String cmd) {
		addMessegeToDisplay(cmd, Type.INCOMING);
	}
	
	public static void addMessegeToDisplay(String cmd, Type t) {
		addMessegeToDisplay(cmd, t, false);
	}
	
	public static String generateDesignedMessege(String cmd, Type t, boolean no_date)
	{
		String newcontent = "";
		
		String ts_string = "";
		if (!no_date) { 
			Date date = new Date();
			Timestamp ts = new Timestamp(date.getTime());
			ts_string = "[" + ts.toString() + "]";
		}
		/*
		 * Currently i am converting NL char to space and comma sep.
		 */
		cmd = cmd.replace("\n", ",");
		cmd = cmd.trim();
		String[] lines = cmd.split("\n");
		for (int i = 0 ; i < lines.length ; i++ ){
			if (lines[i].length() == 0)
				continue;

			switch (t) {
				case GENERAL:
					newcontent = ("<font color=\"black\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case OUTGOING:
					newcontent = ("<font color=\"blue\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case INCOMING:
					newcontent = ("<font color=\"green\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case ERROR:
					newcontent = ("<font color=\"red\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				default:
					newcontent = ("<font color=\"red\">" + ts_string + " Unrecognized: " + lines[i] + "</font>" + "<br/>");
					break;
			}
		}
		
		return newcontent;
	}
	
	public static synchronized void addMessegeToDisplay(String cmd, Type t, boolean no_date) {		
		if (logBox == null) {
			System.err.println("LogBox was not created");
			return;
		}

		String alltext = logBox.getText();
		String content = "";
		if (!alltext.isEmpty())
			content = alltext.substring(alltext.indexOf("<body>") + "<body>".length(), alltext.indexOf("</body>"));
		int idx = content.indexOf("<font");
		if (idx == -1) {
			content = "";
		}
		else
			content = content.substring(idx);
		
		String futureText = "<html>";
		
		String newcontent = generateDesignedMessege(cmd, t, no_date);
		
		Logger.LogDesignedMessege(newcontent);
		
		content = (newcontent + content);

		// To Screen
		String[] sz = content.split("</font>", LOG_BOX_MAX_LINES);
		for (int i = 0 ; i < Math.min(LOG_BOX_MAX_LINES - 1, sz.length) ; i++) {
			futureText += (sz[i] + "</font>");
		}
		
		futureText += "</html>";
		logBox.setText(futureText);
	}
	
	@Override
	protected void finalize() {
		System.out.println("in Demo Finilize");
	}

	public void SetSignal(int signalStrength) {
		window.lblSignal.setText(signalStrength + "%");
	}
	
	
	private void setButtonControl(boolean val) {
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

	public void SetLblHeight(double ht) {
		lblHeightVal.setText(String.format("%.1f", ht) + "m");
	}
	
	public void SetLblBattery(double bat) {
		//final Color orig_color = lblBattery.getBackground();
		if (drone.getState().isFlying() && bat < 100) {
			java.awt.Toolkit.getDefaultToolkit().beep();
			//PaintAllWindow(Color.RED);
			MessegeQueue_Add("Low Battery");
		}
		else {
			lblBattery.setForeground(Color.BLACK);
			//Color c = new Color(238, 238 ,238);
			//PaintAllWindow(orig_color);
		}
		lblBattery.setText((bat < 0 ? 0 : bat) + "%");
	}
	
	public void PaintAllWindow(Color c) {
		if (c == telemetryPanel.getBackground()) {
			System.out.println("Same Color");
			return;
		}
		telemetryPanel.setBackground(c);
		int cnt = telemetryPanel.getComponentCount();
		for (int i = 0 ; i < cnt ; i++) {
			telemetryPanel.getComponent(i).setBackground(c);
			telemetryPanel.getComponent(i).repaint();
		}
		telemetryPanel.repaint();
		
	}
	
	/* Adding messeges to warining queue */
	private static Vector<String> m_messegeQueue = new Vector<String>();
	private static synchronized  String MessegeQueue_Pop() {
		if (m_messegeQueue.isEmpty())
			return null;
		
		String msg = m_messegeQueue.get(0);
		m_messegeQueue.remove(0);
		return msg;
	}
    public static synchronized void MessegeQueue_Add(String string) {
    	m_messegeQueue.addElement(string);
	}
    
	public static void setRCActual(int e1, int e2, int e3, int e4) {
		lblEngine1.setText(e1 + "");
		lblEngine2.setText(e2 + "");    
		lblEngine3.setText(e3 + "");
		lblEngine4.setText(e4 + "");
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
			case LEFT_PERIMETER:
				MessegeQueue_Add("Outside Perimeter");
				addErrorMessegeToDisplay("Quad left the perimeter");
				java.awt.Toolkit.getDefaultToolkit().beep();
				return;
			case ENFORCING_PERIMETER:
				MessegeQueue_Add("Enforcing Perimeter");
				addErrorMessegeToDisplay("Enforcing Perimeter");
				return;
			case ORIENTATION:
				SetLblHeight(drone.getAltitude().getAltitude());
				SetDistanceToWaypoint(drone.getMissionStats().getDistanceToWP().valueInMeters());
				return;
			case BATTERY:
				SetLblBattery(drone.getBattery().getBattRemain());
				return;
			case HEARTBEAT_FIRST:
				addErrorMessegeToDisplay("Quad Connected");
				SetHeartBeat(true);
				return;
			case HEARTBEAT_RESTORED:
				addErrorMessegeToDisplay("Quad Connected");
				SetHeartBeat(true);
				return;
			case HEARTBEAT_TIMEOUT:
				addErrorMessegeToDisplay("Quad Disconnected");
				SetHeartBeat(false);
				SetSignal(0);
				SetLblHeight(0);
				SetLblBattery(0);
				SetFlightModeLabel("Unknown");
				return;
			case MODE:
				SetFlightModeLabel(drone.getState().getMode().getName());
				return;
			case PARAMETER:
				LoadParameter(drone.getParameters().getExpectedParameterAmount());
				return;
			case PARAMETERS_DOWNLOADED:
				addGeneralMessegeToDisplay("Parameters Downloaded succussfully");
				return;
			case RC_OUT:
				setRCActual(drone.getRC().out[0], drone.getRC().out[1], drone.getRC().out[2], drone.getRC().out[3]);
				return;
			case TEXT_MESSEGE:
				addIncommingMessegeToDisplay(drone.getMessegeQueue().pop());
				return;
			case ARMING:
				motorArmed = drone.getState().isArmed();
				btnArm.setSelected(motorArmed);
				return;
			case RADIO:
				SetSignal(drone.getRadio().getSignalStrength());
				return;
			case WARNING_SIGNAL_WEAK:
				addErrorMessegeToDisplay("Warning: Weak signal");
				addErrorMessegeToDisplay("Warning: Weak signal");
				addErrorMessegeToDisplay("Warning: Weak signal");
				java.awt.Toolkit.getDefaultToolkit().beep();
				java.awt.Toolkit.getDefaultToolkit().beep();
				java.awt.Toolkit.getDefaultToolkit().beep();
				return;
			case GPS:
				setDistanceTraveled(drone.getGps().getDistanceTraveled());
				return;
			case STATE:
				setFlightTime(drone.getState().getFlightTime());
				return;
				
		}
	}

	private void setFlightTime(long flightTime) {
		lblFlightTimeVal.setText(flightTime + "");
	}

	private void setDistanceTraveled(double distanceTraveled) {
		lblFlightDistanceVal.setText(String.format("%.1f", distanceTraveled) + "m");
	}

	private void LoadParameter(int expectedParameterAmount) {
		setProgressBar(0, drone.getParameters().getLoadedDownloadedParameters(), drone.getParameters().getExpectedParameterAmount());
		int prc = (int) (((double) (drone.getParameters().getLoadedDownloadedParameters()) / drone.getParameters().getExpectedParameterAmount()) * 100);
		if (prc > 95) {
			System.out.println(getClass().getName() + " Setup stream rate");
			//MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 1, 1, 1, 1, 1, 1, 1, 1);
			MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 1, 1, 1, 1, 4, 1, 1, 1);
			setButtonControl(true);
			System.out.println(getClass().getName() + " " + drone.getParameters().getParameter("MOT_SPIN_ARMED"));
			if (drone.isConnectionAlive()) {
				SetHeartBeat(true);
				SetFlightModeLabel(drone.getState().getMode().getName());
			}
		}
	}

	public void setProgressBar(int min, int current, int max) {
		if (!progressBar.isVisible() || progressBar.getMaximum() != max) {
			progressBar.setMinimum(min);
			progressBar.setValue(current);
			progressBar.setMaximum(max);
			progressBar.setVisible(true);
		}
		progressBar.setValue(current);
		
		if (progressBar.getValue() == progressBar.getMaximum()) {
			progressBar.setVisible(false);
			progressBar.setValue(0);
		}
	}
	
	public void setProgressBar(int min, int max) {
		setProgressBar(min, progressBar.getValue()+1, max);
	}

	public void resetProgressBar() {
		progressBar.setValue(0);
	}    
}
