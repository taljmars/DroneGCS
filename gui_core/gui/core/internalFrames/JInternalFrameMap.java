package gui.core.internalFrames;

import flight_controlers.KeyBoardControl;
import gui.core.dashboard.Dashboard;
import gui.core.internalPanels.JPanelConfigurationBox;
import gui.core.internalPanels.JPanelMissionBox;
import gui.core.mapObjects.Coordinate;
import gui.core.mapObjects.Layer;
import gui.core.mapObjects.LayerGroup;
import gui.core.mapObjects.LayerMission;
import gui.core.mapObjects.LayerPerimeter;
import gui.core.mapObjects.MapLineImpl;
import gui.core.mapObjects.MapMarkerCircle;
import gui.core.mapObjects.MapMarkerDot;
import gui.core.mapObjects.MapObjectImpl;
import gui.core.mapObjects.MapPathImpl;
import gui.core.mapObjects.MapPolygonImpl;
import gui.core.mapObjects.MapRectangleImpl;
import gui.core.mapTileSources.BingAerialTileSource;
import gui.core.mapTileSources.MapQuestOpenAerialTileSource;
import gui.core.mapTileSources.MapQuestOsmTileSource;
import gui.core.mapTileSources.OsmTileSource;
import gui.core.mapTree.JMapViewerTree;
import gui.core.mapViewer.JMapViewer;
import gui.is.classes.MyStroke;
import gui.is.classes.Style;
import gui.is.events.JMVCommandEvent;
import gui.is.interfaces.ICoordinate;
import gui.is.interfaces.JMapViewerEventListener;
import gui.is.interfaces.MapLine;
import gui.is.interfaces.MapPolygon;
import gui.is.interfaces.TileLoader;
import gui.is.interfaces.TileSource;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent; 
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.drone.DroneInterfaces.OnWaypointManagerListener;
import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.commands.ReturnToHome;
import mavlink.is.drone.mission.commands.Takeoff;
import mavlink.is.drone.mission.waypoints.Circle;
import mavlink.is.drone.mission.waypoints.Land;
import mavlink.is.drone.mission.waypoints.Waypoint;
import mavlink.is.drone.variables.GuidedPoint;
import mavlink.is.drone.variables.Home;
import mavlink.is.protocol.msgbuilder.WaypointManager.WaypointEvent_Type;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.coordinates.Coord3D;
import mavlink.is.utils.geoTools.GeoTools;
import mavlink.is.utils.units.Altitude;

public class JInternalFrameMap extends JInternalFrame implements JMapViewerEventListener, OnDroneListener, OnWaypointManagerListener {
	
	/**
	 * 
	 */
	
	private static Coordinate c(double lat, double lon) {
        return new Coordinate(lat, lon);
    }
	
	private static final long serialVersionUID = 1L;
	static JMapViewerTree treeMap;
	JLabel mperpLabelValue;
	JLabel zoomValue;
	JCheckBox showToolTip;
	boolean lockMapOnMyPosition = true;
	boolean paintTrail = true;
	MapPathImpl myTrailPath = null;
	
	protected MapMarkerDot guidedPoint = null;
	MapMarkerDot myPos = null;
	MapLine bearing = null;
	MapMarkerDot myHome = null;
	MapMarkerDot myBeacon = null;
	MapMarkerDot myGCS = null;
	
	// GeoFence and perimeters
	MapMarkerCircle geoFenceMarker = null;
	boolean setGeoFenceByMouse = false;
	boolean setPerimeterByMouse = false;
		
	// Mission Builder
	LayerMission modifyiedLayerMission = null;
	LayerMission modifyiedLayerMissionOriginal = null;
	
	// Perimeter Builder
	LayerPerimeter modifyiedLayerPerimeter = null;
	LayerPerimeter modifyiedLayerPerimeterOriginal = null;
	
	LayerGroup missionsGroup = null;
    LayerGroup perimetersGroup = null;
    LayerGroup generalGroup = null;
	
	private static JInternalFrameMap instance = null;
	
	Layer spain = null;
	private JPanelMissionBox missionBox;
	private JPanelConfigurationBox conifgurationBox;
	private JDesktopPane container;
	
	private JInternalFrameMap(String name, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
		super(name, resizable, closable, maximizable, iconifiable);
        
        treeMap = new JMapViewerTree("Map Views", missionBox, conifgurationBox);
        
        getContentPane().add(treeMap, BorderLayout.CENTER);
        
        JPanel panelTop = new JPanel();
        JPanel panelBottom = new JPanel();
        getContentPane().add(panelTop, BorderLayout.NORTH);
        getContentPane().add(panelBottom, BorderLayout.SOUTH);
        
        JLabel mperpLabelName = new JLabel("Meters/Pixels: ");
        mperpLabelValue = new JLabel(String.format("%s", map().getMeterPerPixel()));
                
        JLabel zoomLabel = new JLabel("Zoom: ");
        zoomValue = new JLabel(String.format("%s", map().getZoom()));
                        
        JButton btnSetDisplayToFitMarkers = new JButton("setDisplayToFitMapMarkers");
        btnSetDisplayToFitMarkers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map().setDisplayToFitMapMarkers();
            }
        });
        JCheckBox cbLockMyPos = new JCheckBox("Lock On My Position");
        JCheckBox cbFollowTrail = new JCheckBox("Paint Trail");
        cbLockMyPos.setSelected(true);
        cbFollowTrail.setSelected(true);
        panelBottom.add(cbLockMyPos);
        panelBottom.add(cbFollowTrail);
        
        JCheckBox showMapMarker = new JCheckBox("Map markers visible");
        showMapMarker.setSelected(map().getMapMarkersVisible());
        showMapMarker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map().setMapMarkerVisible(showMapMarker.isSelected());
            }
        });
        panelBottom.add(showMapMarker);
        ///
        JCheckBox showTreeLayers = new JCheckBox("Show Zones");
        showTreeLayers.setSelected(true);
        treeMap.setTreeVisible(showTreeLayers.isSelected());
        showTreeLayers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                treeMap.setTreeVisible(showTreeLayers.isSelected());
            }
        });
        panelBottom.add(showTreeLayers);
        ///
        showToolTip = new JCheckBox("ToolTip visible");
        showToolTip.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map().setToolTipText(null);
            }
        });
        panelBottom.add(showToolTip);
        ///
        JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
        showTileGrid.setSelected(map().isTileGridVisible());
        showTileGrid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map().setTileGridVisible(showTileGrid.isSelected());
                System.out.println(getClass().getName() + " Is show grid selected " + showTileGrid.isSelected());
            }
        });
        panelBottom.add(showTileGrid);
        final JCheckBox showZoomControls = new JCheckBox("Show zoom controls");
        showZoomControls.setSelected(map().getZoomControlsVisible());
        showZoomControls.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map().setZoomContolsVisible(showZoomControls.isSelected());
            }
        });
        panelBottom.add(showZoomControls);
        final JCheckBox scrollWrapEnabled = new JCheckBox("Scrollwrap enabled");
        scrollWrapEnabled.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map().setScrollWrapEnabled(scrollWrapEnabled.isSelected());
            }
        });
        panelBottom.add(scrollWrapEnabled);
                        
        panelTop.add(btnSetDisplayToFitMarkers);
        panelTop.add(zoomLabel);
        panelTop.add(zoomValue);
        panelTop.add(mperpLabelName);
        panelTop.add(mperpLabelValue);
                                
        
        // Listen to the map viewer for user operations so components will
        // receive events and update
        map().addJMVListener(this);
        map().addJMVListener(KeyBoardControl.get());
        
        missionsGroup = new LayerGroup("Missions");
        perimetersGroup = new LayerGroup("Perimeters");
        generalGroup = new LayerGroup("General Drawings");
        
        Layer franceLayer = treeMap.addLayer("France");
        spain = treeMap.addLayer("Spain");
        Layer wales = treeMap.addLayer("UK");
        //layerPerimeterGeofence = treeMap.addLayer("Perimeters and GeoFences");
        
        LayerGroup germanyGroup = new LayerGroup("Germany");
        Layer germanyWestLayer = germanyGroup.addLayer("Germany West");
        Layer germanyEastLayer = germanyGroup.addLayer("Germany East");
        treeMap.addLayer(germanyWestLayer);
        treeMap.addLayer(germanyEastLayer);
        MapMarkerDot eberstadt = new MapMarkerDot(germanyEastLayer, "Eberstadt", 49.814284999, 8.642065999);
        MapMarkerDot ebersheim = new MapMarkerDot(germanyWestLayer, "Ebersheim", 49.91, 8.24);
        MapMarkerDot empty = new MapMarkerDot(germanyEastLayer, 49.71, 8.64);
        MapMarkerDot darmstadt = new MapMarkerDot(germanyEastLayer, "Darmstadt", 49.8588, 8.643);
        
        
        map().addMapMarker(eberstadt);
        map().addMapMarker(ebersheim);
        map().addMapMarker(empty);
        map().addMapMarker(new MapMarkerDot(franceLayer, "La Gallerie", 48.71, -1));
        map().addMapMarker(new MapMarkerDot(43.604, 1.444));
        map().addMapMarker(new MapMarkerCircle(53.343, -6.267, 0.666));
        map().addMapRectangle(new MapRectangleImpl(new Coordinate(53.343, -6.267), new Coordinate(43.604, 1.444)));
        map().addMapMarker(darmstadt);

        MapPolygon bermudas = new MapPolygonImpl(c(49, 1), c(45, 10), c(40, 5));
        map().addMapPolygon(bermudas);
        map().addMapPolygon(new MapPolygonImpl(germanyEastLayer, "Riedstadt", ebersheim, darmstadt, eberstadt, empty));

        map().addMapMarker(new MapMarkerCircle(germanyWestLayer, "North of Suisse", new Coordinate(48, 7), .5));
        map().addMapMarker(new MapMarkerCircle(spain, "La Garena", new Coordinate(40.4838, -3.39), .002));
        spain.setVisible(Boolean.FALSE);
        map().addMapRectangle(new MapRectangleImpl(wales, "Wales", c(53.35, -4.57), c(51.64, -2.63)));

        //map().setDisplayPosition(new Coordinate(31.918, 35.0244), 5);
        map().setDisplayPosition(new Coordinate(32.0684, 34.8248), 8);
        // map.setTileGridVisible(true);

        map().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    map().getAttribution().handleAttribution(e.getPoint(), true);
                }
            }
        });

        map().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean cursorHand = map().getAttribution().handleAttributionCursor(p);
                if (cursorHand) {
                    map().setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    map().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                if (showToolTip.isSelected()) map().setToolTipText(map().getPosition(p).toString());
            }
        });
        
		JComboBox<TileSource> tileSourceSelector = new JComboBox<>(new TileSource[] {
				new BingAerialTileSource(),
                new OsmTileSource.CycleMap(),
                new MapQuestOsmTileSource(),
                new OsmTileSource.Mapnik(),
                new MapQuestOpenAerialTileSource() });
        tileSourceSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                map().setTileSource((TileSource) e.getItem());
            }
        });
        //tileSourceSelector.addKeyListener(mapKeyListener);
        
        
        JComboBox<TileLoader> tileLoaderSelector;
        tileLoaderSelector = new JComboBox<>(new TileLoader[] {new OsmTileLoader(map())});
        tileLoaderSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                map().setTileLoader((TileLoader) e.getItem());
            }
        });
        map().setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());
        panelTop.add(tileSourceSelector);
        panelTop.add(tileLoaderSelector);
        
        cbLockMyPos.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (cbLockMyPos.isSelected()) {
					Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Lock on my position");
					lockMapOnMyPosition = true;
				}
				else {
					Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Release lock on my position");
					lockMapOnMyPosition = false;
				}
			}
		});
        
        cbFollowTrail.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (cbFollowTrail.isSelected()) {
					Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Paint My Trail");
					myTrailPath = null;
					paintTrail = true;
				}
				else {
					Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Stop Paint My Trail");
					paintTrail = false;
					map().removeMapPath(myTrailPath);
					myTrailPath = null;
				}
			}
		});
        
        //autoMissionLayer = treeMap.addLayer("Mission");
        //editLayer = treeMap.addLayer("Edit Layer");
        
		pack();
	}
	
	public void SetLastKnownPosition() {
		if (myPos == null || myPos.getColor() == Color.RED)
			return;
		
		MapMarkerDot tmp = new MapMarkerDot(null, null, myPos.getCoordinate(), new Style(Color.BLACK, Color.RED, null, MapMarkerDot.getDefaultFont()));
		map().removeMapMarker(myPos);
		
		myPos = tmp;
		
		map().addMapMarker(myPos);
	}

	public static void Close() {
		// TODO Auto-generated method stub
		if (instance != null)
			instance.dispose();
		
		instance = null;			
	}
	
	public static JInternalFrameMap get() {
		return instance;
	}

	public static void Generate(JDesktopPane container, JPanelMissionBox missionBox, JPanelConfigurationBox conifgurationBox) {		
		if (instance != null) {
			instance.missionBox = missionBox;
			instance.conifgurationBox = conifgurationBox;
			instance.container = container;
			instance.moveToFront();
			return;
		}
		
		JInternalFrameMap ifrm = new JInternalFrameMap("Map View");
		Dashboard.drone.addDroneListener(ifrm);
		ifrm.setBounds(25, 25, 800, 400);
        instance = ifrm;
        instance.missionBox = missionBox;
        instance.conifgurationBox = conifgurationBox;
        instance.container = container;
        
        instance.container.add(ifrm);
        instance.setVisible(true);
        try {
        	instance.setMaximum(true);
    	} catch (PropertyVetoException e) {
    	  // Vetoed by internalFrame
    	  // ... possibly add some handling for this case
    	}
	}
	
	private static JMapViewer map() {
        return treeMap.getViewer();
    }
	
	private void updateZoomParameters() {
        if (mperpLabelValue != null)
            mperpLabelValue.setText(String.format("%s", map().getMeterPerPixel()));
        if (zoomValue != null)
            zoomValue.setText(String.format("%s", map().getZoom()));
    }
	
	public void SetMyPositionMarker(Coord2D coord) {
		Coordinate c = new Coordinate(coord.getLat(), coord.getLng());
		if (myPos != null && myPos.getCoordinate().getLat() == c.getLat() && myPos.getCoordinate().getLon() == c.getLon()) {
			//System.out.println(getClass().getName() + " Same position");
			return;
		}
		
		//MapMarkerDot tmp = new MapMarkerDot(coord.getLat(), coord.getLng());
		MapMarkerDot tmp = new MapMarkerDot(null, null, c, new Style(Color.BLACK, Color.GREEN, null, MapMarkerDot.getDefaultFont()));
		
		if (myPos != null) {
			map().removeMapMarker(myPos);			
		}
		else {
			// During the first time we have a GPS lock
			map().setDisplayPosition(c, 17);
		}
		map().addMapMarker(tmp);
		myPos = tmp;
		
		if (lockMapOnMyPosition)
			map().setDisplayPosition(myPos, map().getZoom());
	}
	
	public void SetMyPositionTrail(Coord2D coord) {
		if (!paintTrail)
			return;
		
		// Handling trail
		if (myTrailPath == null)
			myTrailPath = new MapPathImpl();
		
		int trailSize = myTrailPath.getPoints().size();
		if (trailSize >= 1 && myTrailPath.getPoints().get(trailSize - 1) == coord)
			return;
		
		myTrailPath.AddPoint(coord.convertToCoordinate());
		
		if (trailSize >= 2) {
			map().removeMapPath(myTrailPath);
			map().addMapPath(myTrailPath);
		}
	}
	
	public void SetMyPosition(Coord2D coord) {
		if (coord == null) {
			System.err.println("No Positions");
			return;
		}
		
		SetMyPositionMarker(coord);
		SetMyPositionTrail(coord);
	}
	
	JInternalFrameMap(String name) {
		this(name, true, true, true, true);
	}
	
	void updateCycleGeoFence(double radi, Coordinate iCoord) {
		if (geoFenceMarker != null)
    		map().removeMapMarker(geoFenceMarker);
    	
    	
    	
    	double radius = GeoTools.metersTolat(radi);
    	geoFenceMarker = new MapMarkerCircle("GeoFence: " + radi + "m", iCoord, radius);
    	geoFenceMarker.setStyle(new Style(Color.magenta, new Color(200, 200, 200, 50), new MyStroke(9), MapObjectImpl.getDefaultFont()));
    	map().addMapMarker(geoFenceMarker);
	}
	
	Socket socket;
	PrintWriter out;
	Scanner sc;
	private boolean isPerimeterBuildMode = false;
	private boolean isMissionBuildMode = false;
	public JPopupMenu createPopupMenu(MouseEvent e) {		
		JMenuItem menuItemFlyTo = new JMenuItem("Fly to Position");
		JMenuItem menuItemMissionBuild = new JMenuItem("Build Mission");
	    JMenuItem menuItemMissionAddWayPoint = new JMenuItem("Add Way Point");
	    JMenuItem menuItemMissionAddCircle = new JMenuItem("Add Circle");
	    JMenuItem menuItemMissionSetHome = new JMenuItem("Set Home");
	    JMenuItem menuItemMissionSetLandPoint = new JMenuItem("Set Land Point");
	    JMenuItem menuItemMissionSetRTL = new JMenuItem("Set RTL");
	    JMenuItem menuItemMissionSetTakeOff = new JMenuItem("Set Takeoff");
	    JMenuItem menuItemDist = new JMenuItem("Distance -m"); 
	    JMenuItem menuItemGeoFence = new JMenuItem("GeoFence Here");
	    JMenuItem menuItemGeoFenceAddPoint = new JMenuItem("Add Point");
	    JMenuItem menuItemSyncMission = new JMenuItem("Sync Mission");
	    JMenuItem menuItemFindClosest = new JMenuItem("Find closest Here");

        menuItemFlyTo.setEnabled(Dashboard.drone.getGps().isPositionValid());
        menuItemDist.setEnabled(Dashboard.drone.getGps().isPositionValid());
        menuItemMissionAddWayPoint.setVisible(isMissionBuildMode);
        menuItemMissionAddCircle.setVisible(isMissionBuildMode);
        menuItemMissionSetHome.setVisible(Dashboard.drone.getGps().isPositionValid());
        menuItemMissionSetLandPoint.setVisible(isMissionBuildMode);
        menuItemMissionSetRTL.setVisible(isMissionBuildMode);
        menuItemMissionSetTakeOff.setVisible(isMissionBuildMode);
        menuItemMissionBuild.setEnabled(!isMissionBuildMode && !isPerimeterBuildMode);
        menuItemGeoFence.setEnabled(!isMissionBuildMode && !isPerimeterBuildMode);
        menuItemSyncMission.setEnabled(!isMissionBuildMode && !isPerimeterBuildMode);
        menuItemGeoFenceAddPoint.setVisible(isPerimeterBuildMode);
        
        if (Dashboard.drone.getGps().isPositionValid()) {
        	ICoordinate iCoord = map().getPosition(e.getPoint());
    		Coord2D to = new Coord2D(iCoord.getLat(), iCoord.getLon());
    		Coord2D from = Dashboard.drone.getGps().getPosition();
    		int dist = (int) GeoTools.getDistance(from, to).valueInMeters();
        	menuItemDist.setText("Distance " + dist + "m");
        }
        
        //Create the popup menu.
        JPopupMenu popup = new JPopupMenu();
        popup.add(menuItemFlyTo); 
        popup.add(menuItemDist);
        popup.addSeparator();
        popup.add(menuItemMissionBuild);
        popup.add(menuItemGeoFence);        
        popup.addSeparator();
        popup.add(menuItemMissionAddWayPoint);
        popup.add(menuItemMissionAddCircle);
        popup.add(menuItemMissionSetLandPoint);
        popup.add(menuItemMissionSetRTL);
        popup.add(menuItemMissionSetHome);
        popup.add(menuItemMissionSetTakeOff);
        popup.add(menuItemGeoFenceAddPoint);
        popup.addSeparator();
        popup.add(menuItemSyncMission);
        popup.add(menuItemFindClosest);

        menuItemFlyTo.addActionListener(new ActionListener() {        	
			@Override
            public void actionPerformed(ActionEvent arg0) {
				if (!Dashboard.drone.getGps().isPositionValid()) {
					JOptionPane.showMessageDialog(null, "Drone must have a GPS connection to use guideness");
					return;
				}
                if (!GuidedPoint.isGuidedMode(Dashboard.drone)) {
                	int n = JOptionPane.showConfirmDialog(null, "Drone Mode must be changed to GUIDED inorder to set point.\n" + 
                									"Would you like to change mode?" , "", JOptionPane.YES_NO_OPTION);
                	if (n == JOptionPane.YES_OPTION) {
                		//GuidedPoint.changeToGuidedMode(Dashboard.drone);
                		//Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Flight Mode changed to '" + Dashboard.drone.getState().getMode().getName() + "'");
                	}
                	else {
                		return;
                	}
                }
                
                try {
                	if (guidedPoint != null) {
                    	guidedPoint.setBackColor(Color.GRAY);
                    }
                    
                    ICoordinate iCoord = map().getPosition(e.getPoint());
                    Coord2D coord = new Coord2D(iCoord.getLat(), iCoord.getLon());
                    
					Dashboard.drone.getGuidedPoint().forcedGuidedCoordinate(coord);
					//Dashboard.drone.getGuidedPoint().newGuidedCoord(coord);
					
					guidedPoint = new MapMarkerDot(iCoord.getLat(), iCoord.getLon());
                	map().addMapMarker(guidedPoint);
                	Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Flying to guided point " + guidedPoint.getCoordinate().toString());
                } 
                catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
        
        menuItemSyncMission.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(getClass().getName() + " Start Sync Mission");
				Dashboard.window.resetProgressBar();
            	Dashboard.drone.getWaypointManager().setWaypointManagerListener(instance);
            	Dashboard.drone.getWaypointManager().getWaypoints();
            	Dashboard.loggerDisplayerManager.addOutgoingMessegeToDisplay("Send Sync Request");
			}
		});
        
        menuItemFindClosest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
        		// run the adb bridge
            	Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						Process p;
						try {
							p = Runtime.getRuntime().exec("C:\\Program Files (x86)\\Android\\android-sdk\\platform-tools\\adb.exe forward tcp:38300 tcp:38300");						
		        			@SuppressWarnings("resource")
							Scanner sc = new Scanner(p.getErrorStream());
		        			if (sc.hasNext()) {
		        				while (sc.hasNext()) 
		        					System.out.println(sc.next());
		        			}
						} 
						catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
    			t.start();
    			
				System.out.println(getClass().getName() + " Open Socket");
				try {
					socket = new Socket("localhost", 38300);
					out = new PrintWriter(socket.getOutputStream(), true);
					//in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					sc=new Scanner(socket.getInputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
        
        menuItemGeoFence.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	System.out.println(getClass().getName() + " Start GeoFence");
            	Coordinate iCoord = new Coordinate(map().getPosition(e.getPoint()).getLat(), map().getPosition(e.getPoint()).getLon());
            	int radi = 50;
            	String[] options = {"Cycle-Specific" , "Cycle-Manuel", "Polygon", "Cancel" };
            	int n = JOptionPane.showOptionDialog(null, "Choose a way to create perimeter.", "Perimeter Limitation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
            	switch (n) {
            		case 0:
            			KeyBoardControl.get().HoldIfNeeded();
            			String val = (String) JOptionPane.showInputDialog(null, "Please choose radius", "Cyclic Perimeter", JOptionPane.PLAIN_MESSAGE, null, null,"10");
            			radi = Integer.parseInt(val);
            			KeyBoardControl.get().ReleaseIfNeeded();
            			updateCycleGeoFence(radi, iCoord);
            			Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Start GeoFence of manual circle type");
            			break;
            		case 1:
            			setGeoFenceByMouse = true;
            			Dashboard.notificationManager.add("Use Ctrl Key and mouse roller to set radius");
            			Dashboard.notificationManager.add("Use Ctrl Key and mouse roller to set radius");
            			Dashboard.notificationManager.add("Use Ctrl Key and mouse roller to set radius");
            			updateCycleGeoFence(radi, iCoord);
            			Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Start GeoFence of fixed circle type");
            			break;
            		case 2:
            			setPerimeterByMouse = true;
            			Dashboard.notificationManager.add("Use Ctrl Key and left mouse key to add point");
            			Dashboard.notificationManager.add("Use Ctrl Key and left mouse key to add point");
            			Dashboard.notificationManager.add("Use Ctrl Key and left mouse key to add point");
            			map().removeMapMarker(perimeterBreachPointMarker);
            			perimeterBreachPointMarker = null;
            			Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Start GeoFence of perimeter type");
            			
            			map().SetEditModeGUI(true);
                		if (modifyiedLayerPerimeter == null) {
                			modifyiedLayerPerimeter = new LayerPerimeter("New Perimeter*");
                			perimetersGroup.add(modifyiedLayerPerimeter);
                			treeMap.addLayer(modifyiedLayerPerimeter);
                			treeMap.updateUI();
                		}
                		
            			break;
            		case 3:
            			return;
            	}
            	isPerimeterBuildMode = true;            	
            }
        });
        
        menuItemMissionBuild.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent arg0) {
        		map().SetEditModeGUI(true);
        		if (modifyiedLayerMission == null) {
        			modifyiedLayerMission = new LayerMission("New Mission*", missionBox);
        			modifyiedLayerMission.initialize();
        			missionsGroup.add(modifyiedLayerMission);
        			treeMap.addLayer(modifyiedLayerMission);
        			treeMap.updateUI();
        			modifyiedLayerMission.setMission(new Mission(Dashboard.drone));
        		}
        		isMissionBuildMode = true;
        	}
        });
        
        menuItemMissionAddWayPoint.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent arg0) {
        		Coordinate iCoord = new Coordinate(map().getPosition(e.getPoint()).getLat(), map().getPosition(e.getPoint()).getLon());
        		
        		Coord3D c3 = new Coord3D(iCoord.ConvertToCoord2D(), new Altitude(20));
        		Mission m = modifyiedLayerMission.getMission();
        		if (m.isLastItemLandOrRTL()) {
        			JOptionPane.showMessageDialog(null, "Waypoints cannot be added to once there is a Land/RTL point");
					return;
        		}
        		Waypoint wp = new Waypoint(m, c3);
        		m.addMissionItem(wp);
        		modifyiedLayerMission.repaint(map());
        	}
        });
        
        menuItemMissionAddCircle.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent arg0) {
        		Coordinate iCoord = new Coordinate(map().getPosition(e.getPoint()).getLat(), map().getPosition(e.getPoint()).getLon());
        		
        		Coord3D c3 = new Coord3D(iCoord.ConvertToCoord2D(), new Altitude(20));
        		Mission m = modifyiedLayerMission.getMission();
        		if (m.isLastItemLandOrRTL()) {
        			JOptionPane.showMessageDialog(null, "Waypoints cannot be added to once there is a Land/RTL point");
					return;
        		}
        		Circle wp = new Circle(m, c3);
        		m.addMissionItem(wp);
        		modifyiedLayerMission.repaint(map());
        	}
        });
        
        menuItemMissionSetLandPoint.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent arg0) {
        		Coordinate iCoord = new Coordinate(map().getPosition(e.getPoint()).getLat(), map().getPosition(e.getPoint()).getLon());
        		
        		Coord3D c3 = new Coord3D(iCoord.ConvertToCoord2D(), new Altitude(20));
        		Mission m = modifyiedLayerMission.getMission();
        		if (m.isLastItemLandOrRTL()) {
        			JOptionPane.showMessageDialog(null, "RTL/Land point was already defined");
					return;
        		}
        		Land lnd = new Land(m, c3);
        		m.addMissionItem(lnd);
        		modifyiedLayerMission.repaint(map());
        	}
        });
        
        menuItemMissionSetRTL.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent arg0) {
        		Mission m = modifyiedLayerMission.getMission();
        		if (m.isLastItemLandOrRTL()) {
        			JOptionPane.showMessageDialog(null, "RTL/Land point was already defined");
					return;
        		}
        		ReturnToHome lnd = new ReturnToHome(m);
        		m.addMissionItem(lnd);
        		modifyiedLayerMission.repaint(map());
        	}
        });
        
        menuItemMissionSetTakeOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg1) {
				
				Mission m = modifyiedLayerMission.getMission();
        		if (m.isFirstItemTakeoff()) {
        			JOptionPane.showMessageDialog(null, "Takeoff point was already defined");
					return;
        		}
        		
        		Object val = JOptionPane.showInputDialog(null, "Choose altitude", "", JOptionPane.OK_CANCEL_OPTION, null, null, 5);
        		if (val == null) {
        			System.out.println(getClass().getName() + " Takeoff canceled");
        			JOptionPane.showMessageDialog(null, "Takeoff must be defined with height");
        			return;
        		}
        		double altitude = Double.parseDouble((String) val);
        		
        		Takeoff toff = new Takeoff(m, new Altitude(altitude));
        		m.addMissionItem(toff);
        		modifyiedLayerMission.repaint(map());
			}
		});
        
        menuItemGeoFenceAddPoint.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent arg0) {
        		modifyiedLayerPerimeter.add(map().getPosition(e.getPoint()));
    			modifyiedLayerPerimeter.repaint(map());      		
        	}
        });
        
        return popup;
    }

	public void SetBearing(double deg) {
		if (!Dashboard.drone.getGps().isPositionValid())
			return;
		
        if (bearing != null && bearing.getBearing() == deg)
        	return;
        
		if (bearing != null)
			map().removeMapLine(bearing);
		
        Coord2D origin = Dashboard.drone.getGps().getPosition();
        Coord2D target = GeoTools.newCoordFromBearingAndDistance(origin, deg /*+ 180*/, 300);
        
        bearing = new MapLineImpl(new Coordinate(origin.getLat(), origin.getLng()), new Coordinate(target.getLat(), target.getLng()));
        map().addMapLine(bearing);
	}

	void updateGeoFence(MouseWheelEvent e) {
		int rotation = JMapViewer.zoomReverseWheel ? e.getWheelRotation() : -e.getWheelRotation();
		
		int radi = GeoTools.latToMeters(geoFenceMarker.getRadius()).intValue();
		radi+= rotation;
		if (radi < 1) radi = 1;
		double radius = GeoTools.metersTolat(radi);
		
		MapMarkerCircle tmp = new MapMarkerCircle("GeoFence: " + radi + "m", geoFenceMarker.getCoordinate(), radius);
		tmp.setStyle(geoFenceMarker.getStyle());
		
    	map().removeMapMarker(geoFenceMarker);
		geoFenceMarker = tmp;
		map().addMapMarker(geoFenceMarker);
	}
	
	void updatePerimeter(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {			
			modifyiedLayerPerimeter.add(map().getPosition(e.getPoint()));
			modifyiedLayerPerimeter.repaint(map());
		}
	}

	public void removeBearing() {
		if (bearing != null) {
			map().removeMapLine(bearing);
			bearing = null;
		}
	}

	MapMarkerCircle myMapCircle25 = null;
	MapMarkerCircle myMapCircle50 = null;
	MapMarkerCircle myMapCircle75 = null;
	MapMarkerCircle myMapCircle100 = null;
	public void SetHome(Home home) {
		if (home == null || !home.isValid())
			return;
		
		if (myHome != null && myHome.getCoordinate().equals(home.getCoord().convertToCoordinate()))
			return;
		
		if (myHome != null)
			map().removeMapMarker(myHome);
		
		if (myMapCircle25 != null)
			map().removeMapMarker(myMapCircle25);
		
		if (myMapCircle50 != null)
			map().removeMapMarker(myMapCircle50);
		
		if (myMapCircle75 != null)
			map().removeMapMarker(myMapCircle75);
		
		if (myMapCircle100 != null)
			map().removeMapMarker(myMapCircle100);
		
		myHome = new MapMarkerDot(home.getCoord().convertToCoordinate());
		myHome.setBackColor(Color.BLUE);
		map().addMapMarker(myHome);
		
		myMapCircle25 = new MapMarkerCircle(myHome.getCoordinate(), GeoTools.metersTolat(25));
		myMapCircle50 = new MapMarkerCircle(myHome.getCoordinate(), GeoTools.metersTolat(50));
		myMapCircle75 = new MapMarkerCircle(myHome.getCoordinate(), GeoTools.metersTolat(75));
		myMapCircle100 = new MapMarkerCircle(myHome.getCoordinate(), GeoTools.metersTolat(100));
		Style s = myMapCircle50.getStyle();
		s.setBackColor(new Color(0, 0, 0, 0));
		myMapCircle25.setStyle(s);
		myMapCircle50.setStyle(s);
		myMapCircle75.setStyle(s);
		myMapCircle100.setStyle(s);
		map().addMapMarker(myMapCircle25);
		map().addMapMarker(myMapCircle50);
		map().addMapMarker(myMapCircle75);
		map().addMapMarker(myMapCircle100);
		
		Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Setting new Home position");
	}
	
	@Override
    public void processCommand(JMVCommandEvent command) {
		switch (command.getCommand()) {
			case ZOOM:
			case MOVE:
				updateZoomParameters();
				break;
			case FLIGHT:
				break;
			case CONTORL_KEYBOARD:
				break;
			case EDITMODE_EXISTING_LAYER_START:
				EditModeOn();
				Layer layer = (Layer) command.getSource();
				if (layer instanceof LayerMission) {
					System.out.println("Working on Mission Layer");
					modifyiedLayerMission = (LayerMission) layer;
					modifyiedLayerMissionOriginal = new LayerMission(modifyiedLayerMission, missionBox);
					modifyiedLayerMissionOriginal.initialize();
					modifyiedLayerMission.buildMissionTable(map());
					isMissionBuildMode = true;
					modifyiedLayerMission.setName(modifyiedLayerMission.getName() + "*");
				}
				else if (layer instanceof LayerPerimeter) {
					System.out.println("Working on Perimeter Layer");
					modifyiedLayerPerimeter = (LayerPerimeter) layer;
					modifyiedLayerPerimeterOriginal = new LayerPerimeter(modifyiedLayerPerimeter);
					modifyiedLayerPerimeterOriginal.initialize();
					isPerimeterBuildMode = true;
					modifyiedLayerPerimeter.setName(modifyiedLayerPerimeter.getName() + "*");
				}
				else {
					EditModeOff();
					return;
				}
				
				treeMap.getViewer().repaint();
				treeMap.getViewer().updateUI();
				treeMap.getTree().repaint();
				treeMap.getTree().updateUI();
				
				break;
			case EDITMODE_PUBLISH:
				EditModeSaveChanges();
				break;
			case EDITMODE_DISCARD:
				EditModeUndoChanges();
				break;
			case CONTORL_MAP:
				if (setGeoFenceByMouse) updateGeoFence((MouseWheelEvent) command.getSource());
				else if (setPerimeterByMouse) updatePerimeter((MouseEvent) command.getSource());
				break;
		}
    }
	
	private void EditModeUndoChanges() {
		// Missions
		if (modifyiedLayerMission != null) {
			treeMap.removeLayer(modifyiedLayerMission);
			modifyiedLayerMission = null;
		}
		if (modifyiedLayerMissionOriginal != null) {
			modifyiedLayerMissionOriginal.loadToMap(map());
			modifyiedLayerMissionOriginal.setName(modifyiedLayerMissionOriginal.getName().substring(0, modifyiedLayerMissionOriginal.getName().length()));
			treeMap.addLayer(modifyiedLayerMissionOriginal);
			modifyiedLayerMissionOriginal = null;
		}
		
		// Perimeters
		if (modifyiedLayerPerimeter != null) {
			treeMap.removeLayer(modifyiedLayerPerimeter);
			modifyiedLayerPerimeter = null;
		}
		if (modifyiedLayerPerimeterOriginal != null) {
			modifyiedLayerPerimeterOriginal.loadToMap(map());
			modifyiedLayerPerimeterOriginal.setName(modifyiedLayerPerimeterOriginal.getName().substring(0, modifyiedLayerPerimeterOriginal.getName().length() ));
			treeMap.addLayer(modifyiedLayerPerimeterOriginal);
			modifyiedLayerPerimeterOriginal = null;
		}
		treeMap.repaint();
		treeMap.updateUI();
		treeMap.getViewer().repaint();
		treeMap.getViewer().updateUI();
		treeMap.getTree().repaint();
		treeMap.getTree().updateUI();
		
		isMissionBuildMode = false;
		isPerimeterBuildMode = false;
		
		missionBox.clear();
	}

	private void EditModeSaveChanges() {
		if (modifyiedLayerMission != null)
			modifyiedLayerMission.setName(modifyiedLayerMission.getName().substring(0, modifyiedLayerMission.getName().length() - 1));
		
		if (modifyiedLayerPerimeter != null)
			modifyiedLayerPerimeter.setName(modifyiedLayerPerimeter.getName().substring(0, modifyiedLayerPerimeter.getName().length() - 1));
		
		modifyiedLayerMissionOriginal = null;
		modifyiedLayerMission = null;
		
		modifyiedLayerPerimeterOriginal = null;
		modifyiedLayerPerimeter = null;
		
		isMissionBuildMode = false;
		isPerimeterBuildMode = false;
		
		missionBox.clear();
	}

	private void EditModeOff() {
		isMissionBuildMode = false;
		isPerimeterBuildMode = false;
		
		setGeoFenceByMouse = false;
		setPerimeterByMouse = false;
		
		Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Edit mode is off");
	}
	
	private void EditModeOn() {
		Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Edit mode is on");
	}

	@Override
	public void dispose() {
		System.out.println(getClass().getName() + " In Finilize");
		instance = null;
		super.dispose();
	}
	
	static Coordinate perimeterBreachPoint = null;
	static MapMarkerDot perimeterBreachPointMarker = null;
	private void SetPerimeterBreachPoint() {
		if (perimeterBreachPointMarker == null) {
			perimeterBreachPoint = Dashboard.drone.getPerimeter().getClosestPointOnPerimeterBorder().convertToCoordinate();
			perimeterBreachPointMarker = new MapMarkerDot(perimeterBreachPoint.getLat(), perimeterBreachPoint.getLon());
			map().addMapMarker(perimeterBreachPointMarker);
		}
	}
	
	@Override
	public void onBeginWaypointEvent(WaypointEvent_Type wpEvent) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			Dashboard.loggerDisplayerManager.addIncommingMessegeToDisplay("Start Syncing");
			return;
		}
		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			Dashboard.loggerDisplayerManager.addIncommingMessegeToDisplay("Start Updloading Waypoints");
			return;
		}
		
		Dashboard.loggerDisplayerManager.addIncommingMessegeToDisplay("Failed to Start Syncing (" + wpEvent.name() + ")");
	}

	@Override
	public void onWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			Dashboard.loggerDisplayerManager.addIncommingMessegeToDisplay("Downloading Waypoint " + index + "/" + count);
			Dashboard.window.setProgressBar(0, index, count);
			return;
		}
		
		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			Dashboard.loggerDisplayerManager.addIncommingMessegeToDisplay("Uploading Waypoint " + index + "/" + count);
			Dashboard.window.setProgressBar(0, index, count);
			return;
		}
		
		Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("Unexpected Syncing Failure (" + wpEvent.name() + ")");
		Dashboard.window.setProgressBar(count, count);
	}

	@Override
	public void onEndWaypointEvent(WaypointEvent_Type wpEvent) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			Dashboard.loggerDisplayerManager.addIncommingMessegeToDisplay("Waypoints Synced");
			if (Dashboard.drone.getMission() == null) {
				Dashboard.loggerDisplayerManager.addIncommingMessegeToDisplay("Failed to find mission");
				return;
			}
			
			LayerMission instMission = new LayerMission("Current Installed Mission", missionBox);
			instMission.initialize();
			missionsGroup.add(instMission);
			treeMap.addLayer(instMission);
			treeMap.updateUI();
			instMission.setMission(Dashboard.drone.getMission());
			instMission.repaint(map());
			
			Dashboard.loggerDisplayerManager.addIncommingMessegeToDisplay("Current mission was loaded to a new view");
			return;
		}
		
		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			Dashboard.loggerDisplayerManager.addIncommingMessegeToDisplay("Waypoints Synced");
			return;
		}
		
		Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("Failed to Sync Waypoints (" + wpEvent.name() + ")");
	}
		

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		// TODO Auto-generated method stub
		switch (event) {
			case GPS:
				SetMyPosition(drone.getGps().getPosition());	
				return;
			case HOME:
				SetHome(drone.getHome());
				return;
			case HEARTBEAT_TIMEOUT:
				SetLastKnownPosition();
				removeBearing();
				return;
			case ORIENTATION:
				SetBearing(drone.getNavigation().getNavBearing());
				return;
			case LEFT_PERIMETER:
				SetPerimeterBreachPoint();
				return;
			case BEACON_BEEP:
				UpdateBeaconOnMap(drone.getBeacon().getPosition().convertToCoordinate());
				return;
			case GCS_LOCATION:
				if (drone.getGCS().getPosition() == null) {
					Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("GCS location doesn't exist");
					return;
				}
				UpdateGCSOnMap(drone.getGCS().getPosition().dot(1));
				SetBearing(drone.getNavigation().getNavBearing());
				return;
		}
	}

	private void UpdateGCSOnMap(Coord2D coord) {
		if (myGCS == null) {
			myGCS = new MapMarkerDot(Color.magenta, coord.getLat(), coord.getLng());
		}
		else if (myGCS.getCoordinate() == coord.convertToCoordinate()) {
			return;
		}
		else {
			map().removeMapMarker(myGCS);
			myGCS = new MapMarkerDot(Color.magenta, coord.getLat(), coord.getLng());
		}
		
		map().addMapMarker(myGCS);
		Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("GCS was updated");
	}

	private void UpdateBeaconOnMap(Coordinate coord) {
		if (myBeacon == null) {
			myBeacon = new MapMarkerDot(Color.magenta, coord.getLat(), coord.getLon());
		}
		else if (myBeacon.getCoordinate() == coord) {
			return;
		}
		else {
			map().removeMapMarker(myBeacon);
			myBeacon = new MapMarkerDot(Color.magenta, coord.getLat(), coord.getLon());
		}

		map().addMapMarker(myBeacon);
		Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Beacon was updated");
	}
	
}