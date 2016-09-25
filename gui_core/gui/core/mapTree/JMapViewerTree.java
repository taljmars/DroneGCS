// License: GPL. For details, see Readme.txt file.
package gui.core.mapTree;

import gui.core.internalFrames.JInternalFrameMap;
import gui.core.internalPanels.JPanelConfigurationBox;
import gui.core.internalPanels.JPanelMissionBox;
import gui.core.mapObjects.Layer;
import gui.core.mapObjects.LayerGroup;
import gui.core.mapObjects.LayerMission;
import gui.core.mapObjects.LayerPerimeter;
import gui.core.mapObjects.MapPolygonImpl;
import gui.core.mapViewer.JMapViewer;
import gui.is.interfaces.AbstractLayer;
import gui.is.interfaces.MapObject;
import gui.is.interfaces.MapPolygon;
import gui.is.services.LoggerDisplayerManager;
import gui.is.services.NotificationsManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import logger.Logger;
import mavlink.is.drone.Drone;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.protocol.msgbuilder.MavLinkModes;

/**
 * Tree of layers for JMapViewer component
 * @author galo
 */
public class JMapViewerTree extends JPanel {
    /** Serial Version UID */
    private static final long serialVersionUID = 3050203054402323972L;

    private JMapViewer map = null;
    private CheckBoxTree tree = null;
    private JPanel treePanel = null;
    private JSplitPane splitPane = null;
    private LayerMission ActiveLayerMission = null;
    private LayerPerimeter ActiveLayerPerimeter = null;
    private JPanelMissionBox areaMission = null;
	private JPanelConfigurationBox areaConfiguration = null;

	private Drone drone = null;

    public JMapViewerTree(String name, JPanelMissionBox areaMission, JPanelConfigurationBox areaConfiguration, JInternalFrameMap jInternalFrameMap) {
        this(name, false, jInternalFrameMap);
        this.areaMission = areaMission;
        this.areaConfiguration = areaConfiguration;
        this.drone = jInternalFrameMap.getDrone();
    }

    public JMapViewerTree(String name, boolean treeVisible, JInternalFrameMap jInternalFrameMap) {
        super();
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SaveLayersTree();
            }
        });
        
        JButton btnLoad = new JButton("Load");
        btnLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoadLayersTree();
            }
        });
        
        JPanel tmpButons = new JPanel(new BorderLayout());
        tmpButons.add(btnSave, BorderLayout.CENTER);
        tmpButons.add(btnLoad, BorderLayout.SOUTH);
        
        treePanel = new JPanel(new BorderLayout());
        treePanel.add(tmpButons, BorderLayout.SOUTH);
        map = new JMapViewer(jInternalFrameMap);

        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);

        //Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(100, 50);
        //tree.setMinimumSize(minimumSize);
        map.setMinimumSize(minimumSize);
        setLayout(new BorderLayout());
        setTreeVisible(treeVisible);
        
        setTree(new CheckBoxTree(name));
    }
    
    private void setTree(CheckBoxTree new_tree) {
    	if (tree != null) {
    		//map.removeAll();
    		map.removeAllMapLines();
    		map.removeAllMapMarkers();
    		map.removeAllMapPaths();
    		map.removeAllMapPolygons();
    		map.removeAllMapRectangles();
    		map.repaint();
    		map.updateUI();
    		treePanel.remove(tree);
    		tree.removeAll();
    		tree.repaint();
    		tree.updateUI();
    	}
    	tree = new_tree;
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
                        JMapViewerTree.this.createPopupMenu(layer).show(e.getComponent(), e.getX(), e.getY());
                //}
            }
        });
    	
    	treePanel.add(tree, BorderLayout.CENTER);
    	createRefresh();
    	tree.repaint();
		tree.updateUI();
    }

    private JPopupMenu createPopupMenu(final AbstractLayer layer) {
        JMenuItem menuItemShow = new JMenuItem("show texts");
        JMenuItem menuItemHide = new JMenuItem("hide texts");
        JMenuItem menuItemEdit = new JMenuItem("Edit");
        JMenuItem menuItemRename = new JMenuItem("Rename");
        JMenuItem menuItemDelete = new JMenuItem("Delete");
        
        JMenuItem menuItemActivateMission = new JMenuItem("Activate Mission");
        JMenuItem menuItemDeactivateMission = new JMenuItem("Deactivate Mission");
        
        JMenuItem menuItemActivatePerimeterAlarm = new JMenuItem("Activate Perimeter Alarm");
        JMenuItem menuItemActivatePerimeterEnforce = new JMenuItem("Activate Perimeter Enforcement");
        JMenuItem menuItemDeactivatePerimeterAlarm = new JMenuItem("Deactivate Perimeter Alarm");
        JMenuItem menuItemDeactivatePerimeterEnforce = new JMenuItem("Deactivate Perimeter Enforcement");

        //Create the popup menu.
        JPopupMenu popup = new JPopupMenu();

        // Create items
        if (layer.isVisibleTexts() == null) {
            popup.add(menuItemShow);
        } 
        else if (layer.isVisibleTexts()) 
        	popup.add(menuItemHide);
        else 
        	popup.add(menuItemShow);
        
        popup.add(menuItemEdit);
        popup.add(menuItemRename);
        popup.add(menuItemDelete);
        if (layer instanceof LayerMission) {
        	if (ActiveLayerMission == layer) {
        		popup.add(menuItemDeactivateMission);
        	}
        	else {
        		popup.add(menuItemActivateMission);
        	}
        }
        if (layer instanceof LayerPerimeter) {
        	if (ActiveLayerPerimeter == layer) {
        		if (drone.getPerimeter().isAlert())
        			popup.add(menuItemDeactivatePerimeterAlarm);
        		else
        			popup.add(menuItemActivatePerimeterAlarm);
        		if (drone.getPerimeter().isEnforce())
        			popup.add(menuItemDeactivatePerimeterEnforce);
        		else
        			popup.add(menuItemActivatePerimeterEnforce);
        	}
        	else {
        		popup.add(menuItemActivatePerimeterAlarm);
           		popup.add(menuItemActivatePerimeterEnforce);
        	}
        }

        menuItemShow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisibleTexts(layer, true);
                if (layer.getParent() != null) layer.getParent().calculateVisibleTexts();
                map.repaint();
            }
        });
        menuItemHide.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisibleTexts(layer, false);
                if (layer.getParent() != null) layer.getParent().calculateVisibleTexts();
                map.repaint();
            }
        });
        
        menuItemDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                //setVisibleTexts(layer, false);
                removeLayer(layer);
            }
        });
        
        menuItemEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Notify JInternalFrameMap that we are requesting to edit our layer
                map.LayerEditorStart(layer);
                //layer.setName(layer.getName() + "*");
                //tree.repaint();
        		//tree.updateUI();
            }
        });
        
        menuItemRename.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Notify JInternalFrameMap that we are requesting to edit our layer
            	String val = (String) JOptionPane.showInputDialog(null, "Please choose a new name for the layer", "Rename layer", JOptionPane.PLAIN_MESSAGE, null, null, layer.getName());
            	if (val.isEmpty()) {
            		JOptionPane.showMessageDialog(null, "Name cannot be empty");
            	}
            	else {
            		layer.setName(val);
            		tree.repaint();
            		tree.updateUI();
            	}
            }
        });
        
        menuItemActivateMission.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	if (layer instanceof LayerMission) {
            		if (ActiveLayerMission != null) {
            			ActiveLayerMission.setName(ActiveLayerMission.getName().substring("(ACTIVE) ".length(), ActiveLayerMission.getName().length()));
            		}
        			ActiveLayerMission = (LayerMission) layer;
            		if (ActiveLayerMission.getMission() != null) {
            			LoggerDisplayerManager.addOutgoingMessegeToDisplay("Sending Mission To APM");
            			ActiveLayerMission.getMission().sendMissionToAPM();
            			ActiveLayerMission.setName("(ACTIVE) " + ActiveLayerMission.getName());
            			LoggerDisplayerManager.addOutgoingMessegeToDisplay("Change mode to " + ApmModes.ROTOR_AUTO.getName());
            			drone.getState().changeFlightMode(ApmModes.ROTOR_AUTO);
            			NotificationsManager.add("Start Mission");
            			NotificationsManager.add("Start Mission");
            			NotificationsManager.add("Start Mission");
            		}
            		
            		tree.repaint();
            		tree.updateUI();
            	}
            }
        });
        
        menuItemDeactivateMission.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	if (layer instanceof LayerMission) {
            		ActiveLayerMission.setName(ActiveLayerMission.getName().substring("(ACTIVE) ".length(), ActiveLayerMission.getName().length()));
            		
            		MavLinkModes.changeFlightMode(drone, ApmModes.ROTOR_RTL);
            		LoggerDisplayerManager.addGeneralMessegeToDisplay("Comming back to lunch position");
            		NotificationsManager.add("Return To Lunch");
            		
            		ActiveLayerMission = null;
            		
            		tree.repaint();
            		tree.updateUI();
            	}
            }
        });
        
        menuItemActivatePerimeterAlarm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	if (layer instanceof LayerPerimeter) {
            		if (ActiveLayerPerimeter != null && ActiveLayerPerimeter != layer) {
            			LoggerDisplayerManager.addGeneralMessegeToDisplay("Stopping current perimeter");
            			DeactivatePerimeter(ActiveLayerPerimeter);
            			ActiveLayerPerimeter = null;
            		}
            		
            		// If we reached here we can be:
            		// 1) new layer, ActiveLayerPerimeter=null
            		// 2) different layer, ActiveLayerPerimeter=null
            		// 3) same layer
            		
            		LayerPerimeter l = (LayerPerimeter) layer;
            		
        			if (l.getElements().size() != 1 || !(l.getElements().get(0) instanceof MapPolygonImpl)) {
        				LoggerDisplayerManager.addErrorMessegeToDisplay("Missing Perimeter in Layer");
        				ActiveLayerPerimeter = null;
        			}
        			
        			LoggerDisplayerManager.addGeneralMessegeToDisplay("Enable Perimeter Alert");
        			
        			if (ActiveLayerPerimeter != l) {
        				// means this is a new layer or this is different layer
        				ActiveLayerPerimeter = l;
        				ActiveLayerPerimeter.setName("(ACTIVE) " + ActiveLayerPerimeter.getName());
        				drone.getPerimeter().setPolygon((MapPolygon)ActiveLayerPerimeter.getElements().get(0));
        			}
        			
            		drone.getPerimeter().setAlert(true);
            		areaConfiguration.setAlertOn(true);
            		           		
            		tree.repaint();
            		tree.updateUI();
            	}
            }
        });
        
        menuItemDeactivatePerimeterAlarm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	if (layer instanceof LayerPerimeter) {
            		if (!drone.getPerimeter().isEnforce()) {
            			ActiveLayerPerimeter.setName(ActiveLayerPerimeter.getName().substring("(ACTIVE) ".length(), ActiveLayerPerimeter.getName().length()));
            			ActiveLayerPerimeter = null;
            			drone.getPerimeter().setPolygon(null);
            		}
            		
        			LoggerDisplayerManager.addGeneralMessegeToDisplay("Disable Perimeter Alert");
            		drone.getPerimeter().setAlert(false);
            		areaConfiguration.setAlertOn(false);
            		           		
            		tree.repaint();
            		tree.updateUI();
            	}
            }
        });
        
        menuItemActivatePerimeterEnforce.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	if (layer instanceof LayerPerimeter) {
            		if (ActiveLayerPerimeter != null && ActiveLayerPerimeter != layer) {
            			DeactivatePerimeter(ActiveLayerPerimeter);
            			ActiveLayerPerimeter = null;
            		}
            		
            		// If we reached here we can be:
            		// 1) new layer, ActiveLayerPerimeter=null
            		// 2) different layer, ActiveLayerPerimeter=null
            		// 3) same layer
            		
            		LayerPerimeter l = (LayerPerimeter) layer;
            		
        			if (l.getElements().size() != 1 || !(l.getElements().get(0) instanceof MapPolygonImpl)) {
        				LoggerDisplayerManager.addErrorMessegeToDisplay("Missing Perimeter in Layer");
        				ActiveLayerPerimeter = null;
        				return;
        			}
        			
        			LoggerDisplayerManager.addGeneralMessegeToDisplay("Enable Perimeter Enforce");

        			if (ActiveLayerPerimeter != l) {
        				ActiveLayerPerimeter = l;
        				drone.getPerimeter().setPolygon((MapPolygon)ActiveLayerPerimeter.getElements().get(0));
        				ActiveLayerPerimeter.setName("(ACTIVE) " + ActiveLayerPerimeter.getName());
        			}
        			
            		drone.getPerimeter().setEnforce(true);
            		areaConfiguration.setEnforceOn(true);            		
            		           		
            		tree.repaint();
            		tree.updateUI();
            	}
            }
        });
        
        menuItemDeactivatePerimeterEnforce.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	if (layer instanceof LayerPerimeter) {
            		if (!drone.getPerimeter().isAlert()) {
            			ActiveLayerPerimeter.setName(ActiveLayerPerimeter.getName().substring("(ACTIVE) ".length(), ActiveLayerPerimeter.getName().length()));
            			ActiveLayerPerimeter = null;
            			drone.getPerimeter().setPolygon(null);
            		}
            		
            		LoggerDisplayerManager.addGeneralMessegeToDisplay("Disable Perimeter Enforce");
                	drone.getPerimeter().setEnforce(false);
                	areaConfiguration.setEnforceOn(false);
            		           		
            		tree.repaint();
            		tree.updateUI();
            	}
            }
        });
        

        return popup;
    }

    protected void DeactivatePerimeter(LayerPerimeter activeLayerPerimeter) {
    	activeLayerPerimeter.setName(activeLayerPerimeter.getName().substring("(ACTIVE) ".length(), activeLayerPerimeter.getName().length()));
		drone.getPerimeter().setPolygon(null);
		drone.getPerimeter().setAlert(false);
		drone.getPerimeter().setEnforce(false);
		areaConfiguration.setAlertOn(false);
		areaConfiguration.setEnforceOn(false);
		
		tree.repaint();
		tree.updateUI();
	}

	private static void setVisibleTexts(AbstractLayer layer, boolean visible) {
        layer.setVisibleTexts(visible);
        if (layer instanceof LayerGroup) {
            LayerGroup group = (LayerGroup) layer;
            if (group.getLayers() != null)
                for (AbstractLayer al: group.getLayers()) {
                    setVisibleTexts(al, visible);
                }
        }
    }

    public Layer addLayer(String name) {
        Layer layer = new Layer(name);
        this.addLayer(layer);
        return layer;
    }

    public JMapViewerTree addLayer(Layer layer) {
        tree.addLayer(layer);
        return this;
    }

    public JMapViewerTree addLayer(MapObject element) {
        //element.getLayer().add(element);
        return addLayer(element.getLayer());
    }

    public Layer removeFromLayer(MapObject element) {
    	Layer l = element.getLayer();
        element.getLayer().getElements().remove(element);
        return l;
    }

    public static int size(List<?> list) {
        return list == null ? 0 : list.size();
    }
    
    public JMapViewer getViewer() {
        return map;
    }

    public CheckBoxTree getTree() {
        return tree;
    }

    public void setTreeVisible(boolean visible) {
        removeAll();
        revalidate();
        if (visible) {
            splitPane.setLeftComponent(treePanel);
            splitPane.setRightComponent(map);
            add(splitPane, BorderLayout.CENTER);
        } else add(map, BorderLayout.CENTER);
        repaint();
    }

    private void createRefresh() {
        tree.getModel().addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(final TreeModelEvent e) {
                repaint();
                updateUI();
            }

            @Override
            public void treeNodesInserted(TreeModelEvent arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void treeStructureChanged(TreeModelEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
    }
    
    public void SaveLayersTree() {
    	LoggerDisplayerManager.addGeneralMessegeToDisplay("Saving Map Views");
    	String settingsFilePath = "LayersData.ini";
    	Path fFilePath = Paths.get(settingsFilePath);
		/*if (fFilePath.toFile().exists() == false) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			fileChooser.setDialogTitle("Choose Configuration File");
			
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
			    File selectedFile = fileChooser.getSelectedFile();
			    settingsFilePath = selectedFile.getAbsolutePath();
			    fFilePath = Paths.get(settingsFilePath);
			}
			else {
				Dashboard.addErrorMessegeToDisplay("Failed to read layers configurations, invalid line");
		    	Logger.close();
		    	System.err.println(getClass().getName() + " Failed to read layers configurations, invalid line");
		    	return;
			}
		}*/
		try {
			FileOutputStream fOut = new FileOutputStream(fFilePath.toFile());
			ObjectOutputStream file = new ObjectOutputStream(fOut);
			file.writeObject(tree);
			file.close();
			fOut.close();
			Logger.LogGeneralMessege("Map View were successfully saved to " + fFilePath.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LoggerDisplayerManager.addErrorMessegeToDisplay("Failed to save map view to " + fFilePath.toString());
		}
    }
    
    public void LoadLayersTree() {
    	LoggerDisplayerManager.addGeneralMessegeToDisplay("Loading Map Views");
    	String settingsFilePath = "LayersData.ini";
    	Path fFilePath = Paths.get(settingsFilePath);
		/*if (fFilePath.toFile().exists() == false) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			fileChooser.setDialogTitle("Choose Configuration File");
			
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
			    File selectedFile = fileChooser.getSelectedFile();
			    settingsFilePath = selectedFile.getAbsolutePath();
			    fFilePath = Paths.get(settingsFilePath);
			}
			else {
				Dashboard.addErrorMessegeToDisplay("Failed to read layers configurations, invalid line");
		    	Logger.close();
		    	System.err.println(getClass().getName() + " Failed to read layers configurations, invalid line");
		    	return;
			}
		}*/
		try {
			FileInputStream fOut = new FileInputStream(fFilePath.toFile());
			ObjectInputStream file = new ObjectInputStream(fOut);
			CheckBoxTree tmp = (CheckBoxTree) file.readObject();
			file.close();
			fOut.close();
			LoggerDisplayerManager.addGeneralMessegeToDisplay("Map View were successfully loaded to " + fFilePath.toString());
			tree.removeAll();
			setTree(tmp);
			LoggerDisplayerManager.addGeneralMessegeToDisplay("Reresh tree");
			
			LayerGroup root = tree.rootLayer();
			reloadLayerGroup(root);
		} 
		catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LoggerDisplayerManager.addErrorMessegeToDisplay("Failed to load map view to " + fFilePath.toString());
		}
    }
    
    public AbstractLayer reloadLayerGroup(AbstractLayer layer) {
    	if (layer instanceof LayerGroup) {
    		LayerGroup lg = (LayerGroup) layer;
    		Iterator<AbstractLayer> it = lg.getLayers().iterator();
    		while (it.hasNext()) {
    			lg.add(reloadLayerGroup(it.next()));
    		}
    		return lg;
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

	public void removeLayer(AbstractLayer layer) {
		if (layer.getParent() != null) layer.getParent().calculateVisibleTexts();
        if (layer instanceof Layer) {
        	int i = 0;
        	Layer l = (Layer) layer;
        	List<MapObject> arr = l.getElements();
        	if (arr == null) {
        		System.out.println(getClass().getName() + " not element exist");
        	}
        	else {
        		Iterator<MapObject> it = arr.iterator();
        		while (it.hasNext()) {
        			i++;
        			MapObject m = it.next();
        			map.removeMapObject(m);
        			it.remove();
        		}
        	}
        	System.out.println(getClass().getName() + " Removed " + i + " elements");
        }
        else if (layer instanceof LayerGroup) {
        	LayerGroup lg = (LayerGroup) layer;
        	int i = lg.getLayers().size();
        	Iterator<AbstractLayer> it = lg.getLayers().iterator();
        	while (it.hasNext()) {
        		removeLayer(it.next());
        		it.remove();
        	}
        	System.out.println(getClass().getName() + " Removed " + i + " layer groups");
        }
        else {
        	System.out.println(getClass().getName() + " Not and instance of layer!! " + layer.getClass().getName());
        }
        
        map.repaint();
        map.updateUI();
        tree.removeLayer(layer);
        tree.repaint();
        //tree.updateUI();
	}
}
