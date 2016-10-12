package gui.core.internalFrames;

import gui.core.internalPanels.JPanelMissionBox;
import gui.core.mapObjects.Layer;
import gui.core.mapObjects.LayerMission;
import gui.core.mapObjects.LayerPerimeter;
import gui.core.mapObjects.MapLineImpl;
import gui.core.mapObjects.MapMarkerCircle;
import gui.core.mapObjects.MapMarkerDot;
import gui.core.mapObjects.MapObjectImpl;
import gui.core.mapObjects.MapPathImpl;
import gui.core.mapTileSources.BingAerialTileSource;
import gui.core.mapTileSources.MapQuestOpenAerialTileSource;
import gui.core.mapTileSources.MapQuestOsmTileSource;
import gui.core.mapTileSources.OsmTileSource;
import gui.core.mapTree.JMapViewerTree;
import gui.core.mapViewer.JMapViewer;
import gui.core.springConfig.AppConfig;
import gui.is.Coordinate;
import gui.is.classes.MyStroke;
import gui.is.classes.Style;
import gui.is.events.JMVCommandEvent;
import gui.is.interfaces.ICoordinate;
import gui.is.interfaces.KeyBoardControler;
import gui.is.interfaces.MapLine;
import gui.is.interfaces.TileLoader;
import gui.is.interfaces.TileSource;
import gui.is.services.LoggerDisplayerSvc;


import gui.is.services.TextNotificationPublisher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;






import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;






import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;






import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.commands.ReturnToHome;
import mavlink.is.drone.mission.commands.Takeoff;
import mavlink.is.drone.mission.waypoints.Circle;
import mavlink.is.drone.mission.waypoints.Land;
import mavlink.is.drone.mission.waypoints.Waypoint;
import mavlink.is.drone.variables.GuidedPoint;
import mavlink.is.drone.variables.Home;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.coordinates.Coord3D;
import mavlink.is.utils.geoTools.GeoTools;
import mavlink.is.utils.units.Altitude;

@ComponentScan("gui.core.mapViewer")
@ComponentScan("gui.core.mapTree")
@ComponentScan("gui.is.services")
@Component("internalFrameMap")
public class JInternalFrameMap extends AbstractJInternalFrame implements
	OnDroneListener, ActionListener {

	private static final long serialVersionUID = 1L;
	private static final String frameName = "Map View";
	
	@Resource(name = "drone")
	private Drone drone;
	
	@Resource(name = "keyBoardControler")
	private KeyBoardControler keyboardController;
	
	@Resource(name = "areaMission")
	private JPanelMissionBox missionBox;
	
	@Resource(name = "treeMap")
	private JMapViewerTree treeMap;
	
	@Resource(name = "map")
	private JMapViewer map;
	
	@Resource(name = "textNotificationPublisher")
	private TextNotificationPublisher textNotificationPublisher;
	
	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
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
	
	private JCheckBox cbLockMyPos;
	private JCheckBox cbFollowTrail;
	
	private TileSource[] mapTilesSources = new TileSource[]
			{ 
				new BingAerialTileSource(),
				new OsmTileSource.CycleMap(),
				new MapQuestOsmTileSource(),
				new OsmTileSource.Mapnik(),
				new MapQuestOpenAerialTileSource()
			};
	
	private JInternalFrameMap(String name) {
		this(name, true, true, true, true);
	}
	
	public JInternalFrameMap() {
		this(frameName);
	}
	
	private JInternalFrameMap(String name, boolean resizable, boolean closable,
			boolean maximizable, boolean iconifiable) {
		super(name, resizable, closable, maximizable, iconifiable);	
	}
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		setBounds(25, 25, 800, 400);

		getContentPane().add(treeMap, BorderLayout.CENTER);

		JPanel panelTop = new JPanel();
		JPanel panelBottom = new JPanel();
		getContentPane().add(panelTop, BorderLayout.NORTH);
		getContentPane().add(panelBottom, BorderLayout.SOUTH);

		JLabel mperpLabelName = new JLabel("Meters/Pixels: ");
		mperpLabelValue = new JLabel(String.format("%s", map.getMeterPerPixel()));

		JLabel zoomLabel = new JLabel("Zoom: ");
		zoomValue = new JLabel(String.format("%s", map.getZoom()));

		JButton btnSetDisplayToFitMarkers = new JButton("setDisplayToFitMapMarkers");
		btnSetDisplayToFitMarkers.addActionListener( e -> map.setDisplayToFitMapMarkers());
		
		cbLockMyPos = new JCheckBox("Lock On My Position");
		cbLockMyPos.setSelected(true);
		panelBottom.add(cbLockMyPos);
		cbLockMyPos.addActionListener(this);
		
		cbFollowTrail = new JCheckBox("Paint Trail");
		cbFollowTrail.setSelected(true);
		panelBottom.add(cbFollowTrail);
		cbFollowTrail.addActionListener(this);

		JCheckBox showMapMarker = new JCheckBox("Map markers visible");
		showMapMarker.setSelected(map.getMapMarkersVisible());
		showMapMarker.addActionListener( e -> map.setMapMarkerVisible(showMapMarker.isSelected()));
		panelBottom.add(showMapMarker);
		
		JCheckBox showTreeLayers = new JCheckBox("Show Zones");
		showTreeLayers.setSelected(true);
		treeMap.setTreeVisible(showTreeLayers.isSelected());
		showTreeLayers.addActionListener( e -> treeMap.setTreeVisible(showTreeLayers.isSelected()));
		panelBottom.add(showTreeLayers);

		showToolTip = new JCheckBox("ToolTip visible");
		showToolTip.addActionListener( e -> map.setToolTipText(null));
		panelBottom.add(showToolTip);

		JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
		showTileGrid.setSelected(map.isTileGridVisible());
		showTileGrid.addActionListener( e -> map.setTileGridVisible(showTileGrid.isSelected()));
		panelBottom.add(showTileGrid);
		
		final JCheckBox showZoomControls = new JCheckBox("Show zoom controls");
		showZoomControls.setSelected(map.getZoomControlsVisible());
		showZoomControls.addActionListener( e -> map.setZoomContolsVisible(showZoomControls.isSelected()));
		panelBottom.add(showZoomControls);
		
		final JCheckBox scrollWrapEnabled = new JCheckBox("Scrollwrap enabled");
		scrollWrapEnabled.addActionListener( e -> map.setScrollWrapEnabled(scrollWrapEnabled.isSelected()));
		panelBottom.add(scrollWrapEnabled);

		panelTop.add(btnSetDisplayToFitMarkers);
		panelTop.add(zoomLabel);
		panelTop.add(zoomValue);
		panelTop.add(mperpLabelName);
		panelTop.add(mperpLabelValue);		

		map.setDisplayPosition(new Coordinate(32.0684, 34.8248), 8);

		map.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					map.getAttribution().handleAttribution(e.getPoint(), true);
				}
			}
		});

		map.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				boolean cursorHand = map.getAttribution().handleAttributionCursor(p);
				if (cursorHand) {
					map.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else {
					map.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				if (showToolTip.isSelected())
					map.setToolTipText(map.getPosition(p).toString());
			}
		});

		JComboBox<TileSource> tileSourceSelector = new JComboBox<>(mapTilesSources);
		tileSourceSelector.addItemListener( e -> map.setTileSource((TileSource) e.getItem()));

		JComboBox<TileLoader> tileLoaderSelector;
		tileLoaderSelector = new JComboBox<>(new TileLoader[] { new OsmTileLoader(map) });
		tileLoaderSelector.addItemListener( e -> map.setTileLoader((TileLoader) e.getItem()));
		map.setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());
		panelTop.add(tileSourceSelector);
		panelTop.add(tileLoaderSelector);

		pack();
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		drone.addDroneListener(this);
	}

	public void SetLastKnownPosition() {
		if (myPos == null || myPos.getColor() == Color.RED)
			return;

		MapMarkerDot tmp = new MapMarkerDot(null, null, myPos.getCoordinate(), new Style(Color.BLACK, Color.RED, null, MapMarkerDot.getDefaultFont()));
		map.removeMapMarker(myPos);

		myPos = tmp;

		map.addMapMarker(myPos);
	}

	private void updateZoomParameters() {
		if (mperpLabelValue != null)
			mperpLabelValue.setText(String.format("%s", map.getMeterPerPixel()));
		if (zoomValue != null)
			zoomValue.setText(String.format("%s", map.getZoom()));
	}

	public void SetMyPositionMarker(Coord2D coord) {
		Coordinate c = new Coordinate(coord.getLat(), coord.getLng());
		if (myPos != null && myPos.getCoordinate().getLat() == c.getLat()
				&& myPos.getCoordinate().getLon() == c.getLon()) {
			// System.out.println(getClass().getName() + " Same position");
			return;
		}

		// MapMarkerDot tmp = new MapMarkerDot(coord.getLat(), coord.getLng());
		MapMarkerDot tmp = new MapMarkerDot(null, null, c, new Style(Color.BLACK, Color.GREEN, null, MapMarkerDot.getDefaultFont()));

		if (myPos != null) {
			map.removeMapMarker(myPos);
		} else {
			// During the first time we have a GPS lock
			map.setDisplayPosition(c, 17);
		}
		map.addMapMarker(tmp);
		myPos = tmp;

		if (lockMapOnMyPosition)
			map.setDisplayPosition(myPos, map.getZoom());
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
			map.removeMapPath(myTrailPath);
			map.addMapPath(myTrailPath);
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

	void updateCycleGeoFence(double radi, Coordinate iCoord) {
		if (geoFenceMarker != null)
			map.removeMapMarker(geoFenceMarker);

		double radius = GeoTools.metersTolat(radi);
		geoFenceMarker = new MapMarkerCircle("GeoFence: " + radi + "m", iCoord, radius);
		geoFenceMarker.setStyle(new Style(Color.magenta, new Color(200, 200, 200, 50), new MyStroke(9), MapObjectImpl.getDefaultFont()));
		map.addMapMarker(geoFenceMarker);
	}

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
		JMenuItem menuItemPerimeterBuild = new JMenuItem("Build Perimeter");
		JMenuItem menuItemPerimeterAddPoint = new JMenuItem("Add Point");
		JMenuItem menuItemSyncMission = new JMenuItem("Sync Mission");
		JMenuItem menuItemFindClosest = new JMenuItem("Find closest Here");

		menuItemFlyTo.setEnabled(drone.getGps().isPositionValid());
		menuItemDist.setEnabled(drone.getGps().isPositionValid());
		menuItemMissionAddWayPoint.setVisible(isMissionBuildMode);
		menuItemMissionAddCircle.setVisible(isMissionBuildMode);
		menuItemMissionSetHome.setVisible(drone.getGps().isPositionValid());
		menuItemMissionSetLandPoint.setVisible(isMissionBuildMode);
		menuItemMissionSetRTL.setVisible(isMissionBuildMode);
		menuItemMissionSetTakeOff.setVisible(isMissionBuildMode);
		menuItemMissionBuild.setEnabled(!isMissionBuildMode && !isPerimeterBuildMode);
		menuItemPerimeterBuild.setEnabled(!isMissionBuildMode && !isPerimeterBuildMode);
		menuItemSyncMission.setEnabled(!isMissionBuildMode && !isPerimeterBuildMode);
		menuItemPerimeterAddPoint.setVisible(isPerimeterBuildMode);

		if (drone.getGps().isPositionValid()) {
			ICoordinate iCoord = map.getPosition(e.getPoint());
			Coord2D to = new Coord2D(iCoord.getLat(), iCoord.getLon());
			Coord2D from = drone.getGps().getPosition();
			int dist = (int) GeoTools.getDistance(from, to).valueInMeters();
			menuItemDist.setText("Distance " + dist + "m");
		}

		// Create the popup menu.
		JPopupMenu popup = new JPopupMenu();
		popup.add(menuItemFlyTo);
		popup.add(menuItemDist);
		popup.addSeparator();
		popup.add(menuItemMissionBuild);
		popup.add(menuItemPerimeterBuild);
		popup.addSeparator();
		popup.add(menuItemMissionAddWayPoint);
		popup.add(menuItemMissionAddCircle);
		popup.add(menuItemMissionSetLandPoint);
		popup.add(menuItemMissionSetRTL);
		popup.add(menuItemMissionSetHome);
		popup.add(menuItemMissionSetTakeOff);
		popup.add(menuItemPerimeterAddPoint);
		popup.addSeparator();
		popup.add(menuItemSyncMission);
		popup.add(menuItemFindClosest);

		menuItemFlyTo.addActionListener( arg -> {
				if (!drone.getGps().isPositionValid()) {
					JOptionPane.showMessageDialog(null,"Drone must have a GPS connection to use guideness");
					return;
				}
				if (!GuidedPoint.isGuidedMode(drone)) {
					int n = JOptionPane.showConfirmDialog(null,
							"Drone Mode must be changed to GUIDED inorder to set point.\n"
									+ "Would you like to change mode?", "",
							JOptionPane.YES_NO_OPTION);
					if (n == JOptionPane.YES_OPTION) {
						// GuidedPoint.changeToGuidedMode(drone);
						// Dashboard.loggerDisplayerSvc.logGeneral("Flight Mode changed to '"
						// + drone.getState().getMode().getName() +
						// "'");
					} else {
						return;
					}
				}

				try {
					if (guidedPoint != null) {
						guidedPoint.setBackColor(Color.GRAY);
					}

					ICoordinate iCoord = getMapPointerCoordinates(e);
					Coord2D coord = new Coord2D(iCoord.getLat(), iCoord
							.getLon());

					drone.getGuidedPoint().forcedGuidedCoordinate(
							coord);
					// drone.getGuidedPoint().newGuidedCoord(coord);

					guidedPoint = new MapMarkerDot(iCoord.getLat(), iCoord.getLon());
					map.addMapMarker(guidedPoint);
					loggerDisplayerSvc.logGeneral("Flying to guided point " + guidedPoint.getCoordinate().toString());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		);

		menuItemSyncMission.addActionListener( arg -> {
				System.out.println(getClass().getName() + " Start Sync Mission");
				drone.getWaypointManager().getWaypoints();
				loggerDisplayerSvc.logOutgoing("Send Sync Request");
			}
		);

		menuItemFindClosest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});

		menuItemPerimeterBuild.addActionListener( arg -> {
				System.out.println(getClass().getName() + " Start GeoFence");
				Coordinate iCoord = getMapPointerCoordinates(e);
				int radi = 50;
				String[] options = { "Cycle-Specific", "Cycle-Manuel", "Polygon", "Cancel" };
				int n = JOptionPane
						.showOptionDialog(null,
								"Choose a way to create perimeter.",
								"Perimeter Limitation",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[2]);
				switch (n) {
				case 0:
					keyboardController.HoldIfNeeded();
					String val = (String) JOptionPane.showInputDialog(null,
							"Please choose radius", "Cyclic Perimeter",
							JOptionPane.PLAIN_MESSAGE, null, null, "10");
					radi = Integer.parseInt(val);
					keyboardController.ReleaseIfNeeded();
					updateCycleGeoFence(radi, iCoord);
					loggerDisplayerSvc.logGeneral("Start GeoFence of manual circle type");
					break;
				case 1:
					setGeoFenceByMouse = true;
					textNotificationPublisher.publish("Use Ctrl Key and mouse roller to set radius");
					textNotificationPublisher.publish("Use Ctrl Key and mouse roller to set radius");
					textNotificationPublisher.publish("Use Ctrl Key and mouse roller to set radius");
					updateCycleGeoFence(radi, iCoord);
					loggerDisplayerSvc.logGeneral("Start GeoFence of fixed circle type");
					break;
				case 2:
					setPerimeterByMouse = true;
					textNotificationPublisher.publish("Use Ctrl Key and left mouse key to add point");
					textNotificationPublisher.publish("Use Ctrl Key and left mouse key to add point");
					textNotificationPublisher.publish("Use Ctrl Key and left mouse key to add point");
					map.removeMapMarker(perimeterBreachPointMarker);
					perimeterBreachPointMarker = null;
					loggerDisplayerSvc.logGeneral("Start GeoFence of perimeter type");

					map.SetEditModeGUI(true);
					if (modifyiedLayerPerimeter == null) {
						modifyiedLayerPerimeter = new LayerPerimeter("New Perimeter*");
						treeMap.getPerimetersGroup().add(modifyiedLayerPerimeter);
						treeMap.addLayer(modifyiedLayerPerimeter);
						treeMap.updateUI();
					}

					break;
				case 3:
					return;
				}
				isPerimeterBuildMode = true;
			}
		);

		menuItemMissionBuild.addActionListener( arg -> {
				map.SetEditModeGUI(true);
				if (modifyiedLayerMission == null) {
					//modifyiedLayerMission = new LayerMission("New Mission*");
					modifyiedLayerMission = (LayerMission) AppConfig.context.getBean("layerMission");
					modifyiedLayerMission.setName("New Mission*");
					modifyiedLayerMission.initialize();
					treeMap.getMissionsGroup().add(modifyiedLayerMission);
					treeMap.addLayer(modifyiedLayerMission);
					treeMap.updateUI();
					//Mission msn = new Mission();
					Mission msn = (Mission) AppConfig.context.getBean("mission");
					msn.setDrone(drone);
					modifyiedLayerMission.setMission(msn);
				}
				isMissionBuildMode = true;
			}
		);

		menuItemMissionAddWayPoint.addActionListener( arg -> {
				Coordinate iCoord = getMapPointerCoordinates(e);

				Coord3D c3 = new Coord3D(iCoord.ConvertToCoord2D(),new Altitude(20));
				Mission m = modifyiedLayerMission.getMission();
				if (m.isLastItemLandOrRTL()) {
					JOptionPane.showMessageDialog(null, "Waypoints cannot be added to once there is a Land/RTL point");
					return;
				}
				Waypoint wp = new Waypoint(m, c3);
				m.addMissionItem(wp);
				modifyiedLayerMission.repaint(map);
			}
		);

		menuItemMissionAddCircle.addActionListener( arg -> {
				Coordinate iCoord = getMapPointerCoordinates(e);

				Coord3D c3 = new Coord3D(iCoord.ConvertToCoord2D(),new Altitude(20));
				Mission m = modifyiedLayerMission.getMission();
				if (m.isLastItemLandOrRTL()) {
					JOptionPane.showMessageDialog(null,"Waypoints cannot be added to once there is a Land/RTL point");
					return;
				}
				Circle wp = new Circle(m, c3);
				m.addMissionItem(wp);
				modifyiedLayerMission.repaint(map);
			}
		);

		menuItemMissionSetLandPoint.addActionListener( arg -> {
				Coordinate iCoord = getMapPointerCoordinates(e);

				Coord3D c3 = new Coord3D(iCoord.ConvertToCoord2D(), new Altitude(20));
				Mission m = modifyiedLayerMission.getMission();
				if (m.isLastItemLandOrRTL()) {
					JOptionPane.showMessageDialog(null, "RTL/Land point was already defined");
					return;
				}
				Land lnd = new Land(m, c3);
				m.addMissionItem(lnd);
				modifyiedLayerMission.repaint(map);
			}
		);

		menuItemMissionSetRTL.addActionListener( arg -> {
				Mission m = modifyiedLayerMission.getMission();
				if (m.isLastItemLandOrRTL()) {
					JOptionPane.showMessageDialog(null, "RTL/Land point was already defined");
					return;
				}
				ReturnToHome lnd = new ReturnToHome(m);
				m.addMissionItem(lnd);
				modifyiedLayerMission.repaint(map);
			}
		);

		menuItemMissionSetTakeOff.addActionListener( arg -> {

				Mission m = modifyiedLayerMission.getMission();
				if (m.isFirstItemTakeoff()) {
					JOptionPane.showMessageDialog(null,"Takeoff point was already defined");
					return;
				}

				Object val = JOptionPane.showInputDialog(null,"Choose altitude", "", JOptionPane.OK_CANCEL_OPTION,null, null, 5);
				if (val == null) {
					System.out.println(getClass().getName() + " Takeoff canceled");
					JOptionPane.showMessageDialog(null, "Takeoff must be defined with height");
					return;
				}
				double altitude = Double.parseDouble((String) val);

				Takeoff toff = new Takeoff(m, new Altitude(altitude));
				m.addMissionItem(toff);
				modifyiedLayerMission.repaint(map);
			}
		);

		menuItemPerimeterAddPoint.addActionListener( arg -> {
				modifyiedLayerPerimeter.add(map.getPosition(e.getPoint()));
				modifyiedLayerPerimeter.repaint(map);
			}
		);

		return popup;
	}

	public void SetBearing(double deg) {
		if (!drone.getGps().isPositionValid())
			return;

		if (bearing != null && bearing.getBearing() == deg)
			return;

		if (bearing != null)
			map.removeMapLine(bearing);

		Coord2D origin = drone.getGps().getPosition();
		Coord2D target = GeoTools.newCoordFromBearingAndDistance(origin, deg /* + 180 */, 300);

		bearing = new MapLineImpl(new Coordinate(origin.getLat(),origin.getLng()), new Coordinate(target.getLat(),target.getLng()));
		map.addMapLine(bearing);
	}

	void updateGeoFence(MouseWheelEvent e) {
		int rotation = JMapViewer.zoomReverseWheel ? e.getWheelRotation() : -e.getWheelRotation();

		int radi = GeoTools.latToMeters(geoFenceMarker.getRadius()).intValue();
		radi += rotation;
		if (radi < 1)
			radi = 1;
		double radius = GeoTools.metersTolat(radi);

		MapMarkerCircle tmp = new MapMarkerCircle("GeoFence: " + radi + "m",geoFenceMarker.getCoordinate(), radius);
		tmp.setStyle(geoFenceMarker.getStyle());

		map.removeMapMarker(geoFenceMarker);
		geoFenceMarker = tmp;
		map.addMapMarker(geoFenceMarker);
	}

	void updatePerimeter(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			modifyiedLayerPerimeter.add(map.getPosition(e.getPoint()));
			modifyiedLayerPerimeter.repaint(map);
		}
	}

	public void removeBearing() {
		if (bearing != null) {
			map.removeMapLine(bearing);
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
			map.removeMapMarker(myHome);

		if (myMapCircle25 != null)
			map.removeMapMarker(myMapCircle25);

		if (myMapCircle50 != null)
			map.removeMapMarker(myMapCircle50);

		if (myMapCircle75 != null)
			map.removeMapMarker(myMapCircle75);

		if (myMapCircle100 != null)
			map.removeMapMarker(myMapCircle100);

		myHome = new MapMarkerDot(home.getCoord().convertToCoordinate());
		myHome.setBackColor(Color.BLUE);
		map.addMapMarker(myHome);

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
		map.addMapMarker(myMapCircle25);
		map.addMapMarker(myMapCircle50);
		map.addMapMarker(myMapCircle75);
		map.addMapMarker(myMapCircle100);

		loggerDisplayerSvc.logGeneral("Setting new Home position");
	}

	private void EditModeUndoChanges() {
		// Missions
		if (modifyiedLayerMission != null) {
			treeMap.removeLayer(modifyiedLayerMission);
			modifyiedLayerMission = null;
		}
		if (modifyiedLayerMissionOriginal != null) {
			modifyiedLayerMissionOriginal.loadToMap(map);
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
			modifyiedLayerPerimeterOriginal.loadToMap(map);
			modifyiedLayerPerimeterOriginal.setName(modifyiedLayerPerimeterOriginal.getName().substring(0,modifyiedLayerPerimeterOriginal.getName().length()));
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
			modifyiedLayerMission.setName(modifyiedLayerMission.getName().substring(0,modifyiedLayerMission.getName().length() - 1));

		if (modifyiedLayerPerimeter != null)
			modifyiedLayerPerimeter.setName(modifyiedLayerPerimeter.getName().substring(0,modifyiedLayerPerimeter.getName().length() - 1));

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

		loggerDisplayerSvc.logGeneral("Edit mode is off");
	}

	private void EditModeOn() {
		loggerDisplayerSvc.logGeneral("Edit mode is on");
	}

	/* Get the coordinate value of the mouse pointer */
	private Coordinate getMapPointerCoordinates(MouseEvent e) {
		return new Coordinate(map.getPosition(e.getPoint()).getLat(), map.getPosition(e.getPoint()).getLon());
	}

	static Coordinate perimeterBreachPoint = null;
	static MapMarkerDot perimeterBreachPointMarker = null;

	private void SetPerimeterBreachPoint() {
		if (perimeterBreachPointMarker == null) {
			perimeterBreachPoint = drone.getPerimeter().getClosestPointOnPerimeterBorder().convertToCoordinate();
			perimeterBreachPointMarker = new MapMarkerDot(perimeterBreachPoint.getLat(),perimeterBreachPoint.getLon());
			map.addMapMarker(perimeterBreachPointMarker);
		}
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
				loggerDisplayerSvc.logError("GCS location doesn't exist");
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
			map.removeMapMarker(myGCS);
			myGCS = new MapMarkerDot(Color.magenta, coord.getLat(),coord.getLng());
		}

		map.addMapMarker(myGCS);
		loggerDisplayerSvc.logGeneral("GCS was updated");
	}

	private void UpdateBeaconOnMap(Coordinate coord) {
		if (myBeacon == null) {
			myBeacon = new MapMarkerDot(Color.magenta, coord.getLat(),coord.getLon());
		}
		else if (myBeacon.getCoordinate() == coord) {
			return;
		}
		else {
			map.removeMapMarker(myBeacon);
			myBeacon = new MapMarkerDot(Color.magenta, coord.getLat(),coord.getLon());
		}

		map.addMapMarker(myBeacon);
		loggerDisplayerSvc.logGeneral("Beacon was updated");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(cbLockMyPos)) {
			if (cbLockMyPos.isSelected()) {
				loggerDisplayerSvc.logGeneral("Lock on my position");
				lockMapOnMyPosition = true;
			} else {
				loggerDisplayerSvc.logGeneral("Release lock on my position");
				lockMapOnMyPosition = false;
			}
			return;
		}
		
		if (e.getSource().equals(cbFollowTrail)) {
			if (cbFollowTrail.isSelected()) {
				loggerDisplayerSvc.logGeneral("Paint My Trail");
				myTrailPath = null;
				paintTrail = true;
			} else {
				loggerDisplayerSvc.logGeneral("Stop Paint My Trail");
				paintTrail = false;
				map.removeMapPath(myTrailPath);
				myTrailPath = null;
			}
			return;
		}
	}
	
	@EventListener
	public void onApplicationEvent(JMVCommandEvent command) {
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
				modifyiedLayerMissionOriginal = new LayerMission(modifyiedLayerMission);
				modifyiedLayerMissionOriginal.initialize();
				modifyiedLayerMission.buildMissionTable(map);
				isMissionBuildMode = true;
				modifyiedLayerMission.setName(modifyiedLayerMission.getName() + "*");
			} else if (layer instanceof LayerPerimeter) {
				System.out.println("Working on Perimeter Layer");
				modifyiedLayerPerimeter = (LayerPerimeter) layer;
				modifyiedLayerPerimeterOriginal = new LayerPerimeter(modifyiedLayerPerimeter);
				modifyiedLayerPerimeterOriginal.initialize();
				isPerimeterBuildMode = true;
				modifyiedLayerPerimeter.setName(modifyiedLayerPerimeter.getName() + "*");
			} else {
				EditModeOff();
				return;
			}

			map.repaint();
			map.updateUI();
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
			if (setGeoFenceByMouse)
				updateGeoFence((MouseWheelEvent) command.getSource());
			else if (setPerimeterByMouse)
				updatePerimeter((MouseEvent) command.getSource());
			break;
		case POPUP_MAP:
			MouseEvent e = (MouseEvent) command.getSource();
			createPopupMenu(e).show(e.getComponent(), e.getX(), e.getY());
			break;
		}
	}
}