// License: GPL. For details, see Readme.txt file.
package gui.core.mapTree;

import gui.core.internalPanels.JPanelConfigurationBox;
import gui.core.internalPanels.JPanelMissionBox;
import gui.core.mapObjects.Layer;
import gui.core.mapObjects.LayerGroup;
import gui.core.mapObjects.LayerMission;
import gui.core.mapObjects.LayerPerimeter;
import gui.core.springConfig.AppConfig;
import gui.is.interfaces.AbstractLayer;
import gui.is.interfaces.MapObject;
import gui.is.services.LoggerDisplayerSvc;
import gui.is.services.TextNotificationPublisher;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.OnWaypointManagerListener;
import mavlink.is.protocol.msgbuilder.WaypointManager.WaypointEvent_Type;

/**
 * Operational mapview tree
 * @author talma
 */
@ComponentScan("gui.core.mapViewer")
@ComponentScan("gui.is.services")
@Component("treeMap")
public class OperationalJMapViewerTree extends JMapViewerTree implements OnWaypointManagerListener {

	private static final long serialVersionUID = 8147167694828591897L;

	private static final CharSequence UPLOADED_PREFIX = "(CURR) ";

	private static final String LAYERGROUP_TIILE_MISSIONS = "Missions";
	private static final String LAYERGROUP_TIILE_PERIMETERS = "Perimeters";
	private static final String LAYERGROUP_TIILE_GENERAL = "General Drawings";

	private LayerMission uploadedLayerMissionCandidate = null;
	private LayerMission uploadedLayerMission = null;
	private LayerPerimeter uploadedLayerPerimeterCandidate = null;
	private LayerPerimeter uploadedLayerPerimeter = null;

	@Resource(name = "areaMission")
	private JPanelMissionBox areaMission;

	@Resource(name = "areaConfiguration")
	private JPanelConfigurationBox areaConfiguration;

	@Resource(name = "drone")
	private Drone drone;

	@Resource(name = "textNotificationPublisher")
	private TextNotificationPublisher textNotificationPublisher;

	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	private int GroupsAmount;

	private LayerGroup missionsGroup = null;
	private LayerGroup perimetersGroup = null;
	private LayerGroup generalGroup = null;

	@Autowired
	private OperationalJMapViewerTree(@Value("Map Views") String name) {
		this(name, false);
	}

	private OperationalJMapViewerTree(String name, boolean treeVisible) {
		super(name, treeVisible);
	}


	private static int called;    
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");

		loadLayerGroups();
		drone.getWaypointManager().addWaypointManagerListener(this);
	}
	
	public void loadLayerGroups() {
		missionsGroup = new LayerGroup(LAYERGROUP_TIILE_MISSIONS);
		tree.addLayer(missionsGroup);
		GroupsAmount++;
		
		perimetersGroup = new LayerGroup(LAYERGROUP_TIILE_PERIMETERS);
		tree.addLayer(perimetersGroup);
		GroupsAmount++;
		
		generalGroup = new LayerGroup(LAYERGROUP_TIILE_GENERAL);
		tree.addLayer(generalGroup);
		GroupsAmount++;		
	}

	protected void setTree(CheckBoxTree new_tree) {
		super.setTree(new_tree);
		new_tree.addNodeListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {				
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
					AbstractLayer layer = ((CheckBoxNodePanel) e.getComponent()).getData().getAbstractLayer();
					if (layer instanceof LayerMission) {
						loggerDisplayerSvc.logGeneral("Loading Mission Table");
						LayerMission lm = (LayerMission) layer;
						lm.buildMissionTable(map, false);
					}
					else {
						areaMission.clear();
					}
					e.consume();
					return;
				}

				if(e.getButton() == MouseEvent.BUTTON3) {
					showPopup(e);
					e.consume();
					return;
				}
			}

			private void showPopup(MouseEvent e) {
				AbstractLayer layer = ((CheckBoxNodePanel) e.getComponent()).getData().getAbstractLayer();
				if (layer != null)
					OperationalJMapViewerTree.this.createPopupMenu(layer).show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	protected JPopupMenu createPopupMenu(final AbstractLayer layer) {
		JPopupMenu popup = super.createPopupMenu(layer);

		JMenuItem menuItemUploadMission = new JMenuItem("Upload Mission");
		JMenuItem menuItemUploadPerimeter = new JMenuItem("Upload Perimeter");

		if (layer instanceof LayerMission && drone.isConnectionAlive()) {
			popup.add(menuItemUploadMission);
		}
		if (layer instanceof LayerPerimeter && drone.isConnectionAlive()) {
			popup.add(menuItemUploadPerimeter);
		}

		menuItemUploadMission.addActionListener( e -> {
			if (layer instanceof LayerMission) {
				uploadedLayerMissionCandidate = (LayerMission) layer;
				if (uploadedLayerMissionCandidate.getMission() != null) {
					loggerDisplayerSvc.logOutgoing("Uploading Mission To APM");
					uploadedLayerMissionCandidate.getMission().sendMissionToAPM();
					textNotificationPublisher.publish("Uploading Mission");
				}

				tree.repaint();
				tree.updateUI();
			}
		});

		menuItemUploadPerimeter.addActionListener( e -> {
			if (layer instanceof LayerPerimeter) {
				uploadedLayerPerimeterCandidate = (LayerPerimeter) layer;
				if (uploadedLayerPerimeterCandidate.getPerimeter() != null) {
					loggerDisplayerSvc.logOutgoing("Uploading Perimeter To APM");
					drone.getPerimeter().setPolygon(uploadedLayerPerimeterCandidate.getPerimeter());
					uploadedLayerPerimeter = (LayerPerimeter) switchCurrentLayer(getPerimetersGroup(), uploadedLayerPerimeter, uploadedLayerPerimeterCandidate);
					uploadedLayerPerimeterCandidate = null;
					textNotificationPublisher.publish("Perimeter Uploaded Successfully");
				}

				tree.repaint();
				tree.updateUI();
			}
		});        

		return popup;
	}
	
	public void reloadRoot(LayerGroup root) {
		super.reloadRoot(root);
		List<AbstractLayer> lst = root.getLayers();
		int groupsFound = 0;
		for (AbstractLayer layer : lst) {
			if (layer instanceof LayerGroup) {
				LayerGroup layerGroup = (LayerGroup) layer;
				if (layerGroup.getName().equals(LAYERGROUP_TIILE_GENERAL)) {
					generalGroup = layerGroup;
					groupsFound++;
				}
				else if (layerGroup.getName().equals(LAYERGROUP_TIILE_MISSIONS)) {
					missionsGroup = layerGroup;
					groupsFound++;
				}
				else if (layerGroup.getName().equals(LAYERGROUP_TIILE_PERIMETERS)) {
					perimetersGroup = layerGroup;
					groupsFound++;
				}
			}
		}
		if (groupsFound != GroupsAmount)
			loggerDisplayerSvc.logError("Failed to load all groups, origin=" + GroupsAmount + " current=" + groupsFound);
		else
			loggerDisplayerSvc.logError("all groups was successfully loaded");

	}

	public AbstractLayer reloadLayerGroup(AbstractLayer layer) {
		if (layer instanceof LayerGroup) {
			if (((LayerGroup) layer).hasLayers())
				return super.reloadLayerGroup(layer);
			return layer;
		}
		
		LoadOnMap((Layer) layer);
		
		if (layer instanceof LayerMission) {
			LayerMission layerMission = (LayerMission) layer;
			layerMission.getMission().setDrone(drone);
			layerMission.setMissionBox(areaMission);
			layerMission.setLoggerDisplayer(loggerDisplayerSvc);
			return layerMission;
		}
		
		return layer;		
	}
	
	private void LoadOnMap(Layer layer) {
		Iterator<MapObject> it = layer.getElements().iterator();
		while (it.hasNext()) {
			map.addMapObject(it.next());
		}
	}

	public Layer switchCurrentLayer(LayerGroup layerGroup, Layer fromLayer, Layer toLayer) {
		Layer finalLayer = null;

		if (toLayer.equals(fromLayer)) {
			loggerDisplayerSvc.logGeneral("Current mission layer is updated");
			return finalLayer;
		}

		finalLayer = toLayer;

		if (fromLayer != null) {
			// Means the GUI is updated with old uploaded mission
			CurrentPrefixRemove(fromLayer);
			loggerDisplayerSvc.logGeneral("Previous mission prefix was removed");
		}
		else {
			// Means we are not aware of any uploaded mission
			layerGroup.add(finalLayer);
			addLayer(finalLayer);
			updateUI();
			finalLayer.repaint(map);
			loggerDisplayerSvc.logGeneral("A new layer was created for current mission");
		}

		CurrentPrefixAdd(finalLayer);

		tree.repaint();
		tree.updateUI();

		return finalLayer;
	}

	@Override
	public void onBeginWaypointEvent(WaypointEvent_Type wpEvent) {		
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			loggerDisplayerSvc.logIncoming("Start Downloading Waypoints");
			return;
		}
		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			loggerDisplayerSvc.logIncoming("Start Updloading Waypoints");
			return;
		}

		loggerDisplayerSvc.logError("Failed to Start Syncing (" + wpEvent.name() + ")");
		textNotificationPublisher.publish("Mission Sync failed");
	}

	@Override
	public void onWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			loggerDisplayerSvc.logIncoming("Downloading Waypoint " + index + "/" + count);
			return;
		}

		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			loggerDisplayerSvc.logIncoming("Uploading Waypoint " + index + "/" + count);
			return;
		}

		loggerDisplayerSvc.logError("Unexpected Syncing Failure (" + wpEvent.name() + ")");
		textNotificationPublisher.publish("Mission Sync failed");
	}

	@Override
	public void onEndWaypointEvent(WaypointEvent_Type wpEvent) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			loggerDisplayerSvc.logIncoming("Waypoints downloaded");
			if (drone.getMission() == null) {
				loggerDisplayerSvc.logError("Failed to find mission");
				return;
			}

			LayerMission lm = (LayerMission) AppConfig.context.getBean("layerMission");
			lm.setName("UnnamedMission");
			lm.setMission(drone.getMission());
			lm.initialize();
			uploadedLayerMission = (LayerMission) switchCurrentLayer(getMissionsGroup(), uploadedLayerMission, lm);

			loggerDisplayerSvc.logGeneral("Mission was updated in mission tree");
			textNotificationPublisher.publish("Mission successfully downloaded");
			return;
		}

		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			loggerDisplayerSvc.logIncoming("Waypoints uploaded");
			if (drone.getMission() == null) {
				loggerDisplayerSvc.logError("Failed to find mission");
				return;
			}
			uploadedLayerMission = (LayerMission) switchCurrentLayer(getMissionsGroup(), uploadedLayerMission, uploadedLayerMissionCandidate);
			uploadedLayerMissionCandidate = null;
			loggerDisplayerSvc.logGeneral("Mission was updated in mission tree");
			textNotificationPublisher.publish("Mission successfully uploaded");
			return;
		}

		loggerDisplayerSvc.logError("Failed to Sync Waypoints (" + wpEvent.name() + ")");
		textNotificationPublisher.publish("Mission Sync failed");
	}

	private void CurrentPrefixRemove(AbstractLayer old_layer) {
		if (old_layer.getName().contains(UPLOADED_PREFIX))
			old_layer.setName(old_layer.getName().substring(UPLOADED_PREFIX.length(), old_layer.getName().length()));
	}

	private void CurrentPrefixAdd(AbstractLayer finalLayer) {
		if (!finalLayer.getName().contains(UPLOADED_PREFIX))
			finalLayer.setName(UPLOADED_PREFIX + finalLayer.getName());
	}

	public LayerGroup getMissionsGroup() {
		return missionsGroup;
	}

	public LayerGroup getPerimetersGroup() {
		return perimetersGroup;
	}

	public LayerGroup getGeneralGroup() {
		return generalGroup;
	}
}
