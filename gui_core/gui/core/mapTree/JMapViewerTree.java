// License: GPL. For details, see Readme.txt file.
package gui.core.mapTree;

import gui.core.mapObjects.Layer;
import gui.core.mapObjects.LayerGroup;
import gui.core.mapViewer.JMapViewer;
import gui.is.interfaces.AbstractLayer;
import gui.is.interfaces.MapObject;
import gui.is.services.LoggerDisplayerSvc;
import gui.is.services.TextNotificationPublisher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import logger.Logger;

/**
 * Tree of layers for JMapViewer component
 * @author galo
 */
public class JMapViewerTree extends JPanel {

    private static final long serialVersionUID = 3050203054402323972L;
    
    protected CheckBoxTree tree = null;
    private JPanel treePanel = null;
    private JSplitPane splitPane = null;
    
    @Resource(name = "map")
    protected JMapViewer map;
	
	@Resource(name = "textNotificationPublisher")
	private TextNotificationPublisher textNotificationPublisher;
	
	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;

    protected JMapViewerTree(String name, boolean treeVisible) {
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

        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);

        //Provide minimum sizes for the two components in the split pane
       
        setLayout(new BorderLayout());
        //setTreeVisible(treeVisible);
        
        setTree(new CheckBoxTree(name));
    }
    
	
	private static int called;    
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		Dimension minimumSize = new Dimension(100, 50);
		//tree.setMinimumSize(minimumSize);
		map.setMinimumSize(minimumSize);
		setTreeVisible(true);
	}
    
    protected void setTree(CheckBoxTree new_tree) {
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
    	
    	treePanel.add(tree, BorderLayout.CENTER);
    	createRefresh();
    	tree.repaint();
		tree.updateUI();
    }

    protected JPopupMenu createPopupMenu(final AbstractLayer layer) {
        JMenuItem menuItemShow = new JMenuItem("show texts");
        JMenuItem menuItemHide = new JMenuItem("hide texts");
        JMenuItem menuItemEdit = new JMenuItem("Edit");
        JMenuItem menuItemRename = new JMenuItem("Rename");
        JMenuItem menuItemDelete = new JMenuItem("Delete");

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

        menuItemShow.addActionListener( e -> {
                setVisibleTexts(layer, true);
                if (layer.getParent() != null) layer.getParent().calculateVisibleTexts();
                map.repaint();
        });
        
        menuItemHide.addActionListener( e -> {
                setVisibleTexts(layer, false);
                if (layer.getParent() != null) layer.getParent().calculateVisibleTexts();
                map.repaint();
        });
        
        menuItemDelete.addActionListener( e -> removeLayer(layer));
        
        menuItemEdit.addActionListener( e -> map.LayerEditorStart(layer));
        
        menuItemRename.addActionListener( e -> {
            	String val = (String) JOptionPane.showInputDialog(null, "Please choose a new name for the layer", "Rename layer", JOptionPane.PLAIN_MESSAGE, null, null, layer.getName());
            	if (val.isEmpty()) {
            		JOptionPane.showMessageDialog(null, "Name cannot be empty");
            	}
            	else {
            		layer.setName(val);
            		tree.repaint();
            		tree.updateUI();
            	}
        });

        return popup;
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
    	loggerDisplayerSvc.logGeneral("Saving Map Views");
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
			loggerDisplayerSvc.logError("Failed to save map view to " + fFilePath.toString());
		}
    }
    
    public void LoadLayersTree() {
    	loggerDisplayerSvc.logGeneral("Loading Map Views");
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
			loggerDisplayerSvc.logGeneral("Map View were successfully loaded to " + fFilePath.toString());
			tree.removeAll();
			setTree(tmp);
			loggerDisplayerSvc.logGeneral("Reresh tree");
			
			LayerGroup root = tree.rootLayer();
			reloadLayerGroup(root);
		} 
		catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			loggerDisplayerSvc.logError("Failed to load map view to " + fFilePath.toString());
		}
    }
    
    public AbstractLayer reloadLayerGroup(AbstractLayer layer) {
		LayerGroup lg = (LayerGroup) layer;
		Iterator<AbstractLayer> it = lg.getLayers().iterator();
		while (it.hasNext()) {
			lg.add(reloadLayerGroup(it.next()));
		}
		return lg;
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
