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
    
    private LayerMission uploadedLayerMissionCandidate = null;
    private LayerMission uploadedLayerMission = null;
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
		
		missionsGroup = new LayerGroup("Missions");
		perimetersGroup = new LayerGroup("Perimeters");
		generalGroup = new LayerGroup("General Drawings");
		tree.addLayer(missionsGroup);
		tree.addLayer(perimetersGroup);
		tree.addLayer(generalGroup);
		
		drone.getWaypointManager().addWaypointManagerListener(this);
	}
    
    protected void setTree(CheckBoxTree new_tree) {
    	super.setTree(new_tree);
    	tree.addNodeListener(new MouseAdapter() {
    		
            @Override
            public void mouseClicked(MouseEvent e) {
            	if(e.getButton() == MouseEvent.BUTTON1) {
            		AbstractLayer layer = ((CheckBoxNodePanel) e.getComponent()).getData().getAbstractLayer();
                    if (layer instanceof LayerMission) {
                    	LayerMission lm = (LayerMission) layer;
                    	lm.buildMissionTable(map);
                    	areaMission.setEnabled(false);
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

            //private void maybeShowPopup(MouseEvent e) {
            private void showPopup(MouseEvent e) {
                //if (e.isPopupTrigger()) {
                    AbstractLayer layer = ((CheckBoxNodePanel) e.getComponent()).getData().getAbstractLayer();
                    if (layer != null)
                        OperationalJMapViewerTree.this.createPopupMenu(layer).show(e.getComponent(), e.getX(), e.getY());
                //}
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
            		if (uploadedLayerPerimeter != null) {
            			uploadedLayerPerimeter.setName(uploadedLayerPerimeter.getName().substring("(CURR) ".length(), uploadedLayerPerimeter.getName().length()));
            		}            		
            		
            		uploadedLayerPerimeter = (LayerPerimeter) layer;        			
        			if (uploadedLayerPerimeter.getPerimeter() != null) {
            			loggerDisplayerSvc.logOutgoing("Uploading Perimeter To APM");
            			uploadedLayerPerimeter.setName("(CURR) " + uploadedLayerPerimeter.getName());
            			drone.getPerimeter().setPolygon(uploadedLayerPerimeter.getPerimeter());
            			textNotificationPublisher.publish("Uploading Perimeter");
            		}
            		           		
            		tree.repaint();
            		tree.updateUI();
            	}
        });        

        return popup;
    }
    
    public AbstractLayer reloadLayerGroup(AbstractLayer layer) {
    	if (layer instanceof LayerGroup) {
    		return super.reloadLayerGroup(layer);
    	}
    	else {
    		Layer l = (Layer) layer;
    		if (l.getElements() == null)
    			return l;
    		
    		Iterator<MapObject> it = l.getElements().iterator();
    		while (it.hasNext()) {
    			map.addMapObject(it.next());
    		}
    		if (layer instanceof LayerMission) { // Any type of layer
    			((LayerMission) layer).getMission().setDrone(drone);
    		}
    		return l;
    	}
    }

	public void setCurrentMissionLayer(LayerMission layer) {
		if (layer.equals(uploadedLayerMission)) {
			loggerDisplayerSvc.logGeneral("Current mission layer is updated");
			return;
		}
			
		if (uploadedLayerMission != null) {
			// Means the GUI is updated with old uploaded mission
			CurrentPrefixRemove(uploadedLayerMission);
			loggerDisplayerSvc.logGeneral("Previous mission prefix was removed");
			uploadedLayerMission = layer;
		}
		else {
			// Means we are not aware of any uploaded mission
			uploadedLayerMission = layer;
			getMissionsGroup().add(uploadedLayerMission);
			addLayer(uploadedLayerMission);
			updateUI();
			uploadedLayerMission.repaint(map);
			loggerDisplayerSvc.logGeneral("A new layer was created for current mission");
		}
		
		CurrentPrefixAdd(uploadedLayerMission);
	
		tree.repaint();
		tree.updateUI();
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

		System.out.println("Failed to Start Syncing (" + wpEvent.name() + ")");
		loggerDisplayerSvc.logError("Failed to Start Syncing (" + wpEvent.name() + ")");
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

		System.out.println("Unexpected Syncing Failure (" + wpEvent.name() + ")");
		loggerDisplayerSvc.logError("Unexpected Syncing Failure (" + wpEvent.name() + ")");
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
			setCurrentMissionLayer(lm);

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
			setCurrentMissionLayer(uploadedLayerMissionCandidate);
			uploadedLayerMissionCandidate = null;
			loggerDisplayerSvc.logGeneral("Mission was updated in mission tree");
			textNotificationPublisher.publish("Mission successfully uploaded");
			return;
		}
		
		System.out.println("Failed to Sync Waypoints (" + wpEvent.name() + ")");
		loggerDisplayerSvc.logError("Failed to Sync Waypoints (" + wpEvent.name() + ")");
	}
	
	private void CurrentPrefixRemove(LayerMission layerMission) {
		if (layerMission.getName().contains(UPLOADED_PREFIX))
			layerMission.setName(layerMission.getName().substring(UPLOADED_PREFIX.length(), layerMission.getName().length()));
	}

	private void CurrentPrefixAdd(LayerMission layerMission) {
		if (!layerMission.getName().contains(UPLOADED_PREFIX))
			layerMission.setName(UPLOADED_PREFIX + layerMission.getName());
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
