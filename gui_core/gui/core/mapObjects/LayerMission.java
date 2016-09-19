// License: GPL. For details, see Readme.txt file.
package gui.core.mapObjects;

import gui.is.interfaces.ICoordinate;
import gui.is.interfaces.MapObject;
import gui.core.dashboard.Dashboard;
import gui.core.internalFrames.helper.ButtonColumn;
import gui.core.mapViewer.JMapViewer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.MissionItem;
import mavlink.is.drone.mission.commands.ReturnToHome;
import mavlink.is.drone.mission.commands.Takeoff;
import mavlink.is.drone.mission.waypoints.Circle;
import mavlink.is.drone.mission.waypoints.Land;
import mavlink.is.drone.mission.waypoints.Waypoint;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.utils.geoTools.GeoTools;
import mavlink.is.utils.units.Altitude;

public class LayerMission extends Layer implements Serializable /*TALMA add serilizebae*/ {    
    /**
	 * 
	 */
	
	private static final long serialVersionUID = -1287037956711191751L;
	private Mission mission;
	
	enum Column {
		Order ("Order"), 
		Type ("Type"),
		Lat ("Lat"),
		Lon ("Lon"),
		Height ("Height"),
		Delay ("Delay"),
		Radius ("Radius"),
		SetUp ("SetUp"),
		SetDown ("SetDown"),
		Remove ("Remove"),
		PTR ("PTR");
		
	    private final String name;
		
		Column (String name){
			this.name = name;
		}

		public int getNumber() {
			return this.ordinal();
		}

		public String getName() {
			return name;
		}
	};
	
	public enum MissionItemsType {
		CIRCLE(0, "Circle", Circle.class, new HashSet<Column>(Arrays.asList(Column.Height, Column.Radius, Column.SetUp, Column.SetDown, Column.Remove))),
		WAYPOINT(2, "Waypoint", Waypoint.class, new HashSet<Column>(Arrays.asList(Column.Height, Column.Delay, Column.SetUp, Column.SetDown, Column.Remove))),
		LAND(3, "Land", Land.class, new HashSet<Column>(Arrays.asList(Column.Remove))),
		RTL(4, "Return To Lunch", ReturnToHome.class, new HashSet<Column>(Arrays.asList(Column.Remove))),
		TAKEOFF(10, "Takeoff", Takeoff.class, new HashSet<Column>(Arrays.asList(Column.Height, Column.Remove))),
		UNKNOWN(-1, "Unknown", null, null);

		private final int number;
	    private final String name;
		private final Class<?> type;
		private HashSet<Column> pEditableFields;

		MissionItemsType (int number,String name, Class<?> cl, HashSet<Column> editableFields){
			this.number = number;
			this.name = name;
			this.type = cl;
			this.pEditableFields = editableFields;
		}

		public int getNumber() {
			return number;
		}

		public String getName() {
			return name;
		}

		public Class<?> getType() {
			return type;
		}
		
		public static MissionItemsType getMissionItemTypeByClass(Class<? extends MissionItem> cl) {
			if (cl == CIRCLE.type)
				return CIRCLE;
			if (cl == WAYPOINT.type)
				return WAYPOINT;
			if (cl == LAND.type)
				return LAND;
			if (cl == RTL.type)
				return RTL;
			if (cl == TAKEOFF.type)
				return TAKEOFF;
			return UNKNOWN;
		}
	}	

    public LayerMission(String name) {
        super(name);
    }
    
    public LayerMission(LayerMission layer) {
    	super(layer);
    	if (layer.getMission() != null) {
    		this.mission = new Mission(Dashboard.drone);
    		Iterator<MissionItem> it = layer.getMission().getItems().iterator();
    		while (it.hasNext()) {
    			this.mission.addMissionItem(it.next());
    		}
    	}
    }
    
	public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public LayerMission add(MapObject element) {
    	super.add(element);
        return this;
    }
    
    public void unloadFromMap(JMapViewer map) {
    	super.unloadFromMap(map);
    }

	public void loadToMap(JMapViewer map) {
		super.loadToMap(map);
	}
	
	public void repaintMission(JMapViewer map) {
		if (mission == null)
			return;

		MapPathImpl route = null;
		Iterator<MissionItem> it = mission.getItems().iterator();
		ArrayList<ICoordinate> points = new ArrayList<ICoordinate>();
		int i = 0;
		while (it.hasNext()) {
			MissionItem item = it.next();
			
			switch (item.getType()) {
				case WAYPOINT: {
					Waypoint wp = (Waypoint) item;
					MapMarkerDot m = new MapMarkerDot(this,  MissionItemsType.WAYPOINT.getName() + i, wp.getCoordinate().getLat(), wp.getCoordinate().getLng());
					add(m);
					points.add(wp.getCoordinate().convertToCoordinate());
					break;
				}
				case SPLINE_WAYPOINT:
					//return new SplineWaypoint(referenceItem);
				case TAKEOFF: {
					if (!Dashboard.drone.getGps().isPositionValid())
						return;
					ICoordinate curr = Dashboard.drone.getGps().getPosition().convertToCoordinate();
					MapMarkerDot m = new MapMarkerDot(this, MissionItemsType.TAKEOFF.getName(), curr.getLat(), curr.getLon());
					m.setBackColor(Color.GREEN);
					add(m);
					points.add(curr);
					break;
				}
				case CHANGE_SPEED:
					//return new ChangeSpeed(referenceItem);
				case CAMERA_TRIGGER:
					//return new CameraTrigger(referenceItem);
				case EPM_GRIPPER:
					//return new EpmGripper(referenceItem);
				case RTL: {
					ICoordinate c = points.get(points.size() - 1);
					MapMarkerDot m = new MapMarkerDot(this, MissionItemsType.RTL.getName(), c.getLat(), c.getLon());
					m.setBackColor(Color.MAGENTA);
					add(m);
					break;
				}
				case LAND: {
					Land lnd = (Land) item;
					MapMarkerDot m = new MapMarkerDot(this, MissionItemsType.LAND.getName(), lnd.getCoordinate().getLat(), lnd.getCoordinate().getLng());
					m.setBackColor(Color.MAGENTA);
					add(m);
					points.add(lnd.getCoordinate().convertToCoordinate());
					break;
				}
				case CIRCLE: {
					Circle wp = (Circle) item;
					MapMarkerCircle m = new MapMarkerCircle(this, wp.getCoordinate().getLat(), wp.getCoordinate().getLng(), GeoTools.metersTolat(10));
					//m.setBackColor(Color.MAGENTA);
					add(m);
					points.add(wp.getCoordinate().convertToCoordinate());
					break;
				}
				case ROI:
					//return new RegionOfInterest(referenceItem);
				case SURVEY:
					//return new Survey(referenceItem.getMission(), Collections.<Coord2D> emptyList());
				case CYLINDRICAL_SURVEY:
					//return new StructureScanner(referenceItem);
				default:
					break;
			}
			i++;
		}
		
		route = new MapPathImpl(this, points);
		add(route);
		buildMissionTable(map);
	}
	
	class MissionItemTableEntry
	{
		Integer pOrder;
		String pType, pRemove;
		Double pLat, pLon, pHeight, pDelay, pRadius;
		Object pSetUp;
		Object pSetDown;
		MissionItem pMissionItem;
		
		MissionItemTableEntry(Integer order, String type, Double lat, Double lon, Double height, Double delay, Double radius, Object setUp, Object setDown, String deleteMark, MissionItem mi) {
			pOrder = order;
			pType = type;
			pLat = lat;
			pLon = lon;
			pHeight = height;
			pDelay = delay;
			pRadius = radius;
			pSetUp = setUp;
			pSetDown = setDown;
			pRemove = deleteMark;
			pMissionItem = mi;
		}
		
		MissionItemTableEntry(Object[] obj) {
			pOrder = (Integer) obj[Column.Order.ordinal()];
			pType = (String) obj[Column.Type.ordinal()];
			pLat = (Double) obj[Column.Lat.ordinal()];
			pLon = (Double) obj[Column.Lon.ordinal()];
			pHeight = (Double) obj[Column.Height.ordinal()];
			pDelay = (Double) obj[Column.Delay.ordinal()];
			pRadius = (Double) obj[Column.Radius.ordinal()];
			pSetUp = (Object) obj[Column.SetUp.ordinal()];
			pSetDown = (Object) obj[Column.SetDown.ordinal()];
			pRemove = (String) obj[Column.Remove.ordinal()];
			pMissionItem = (MissionItem) obj[Column.PTR.ordinal()];
		}

		public Object[] toArray() {
			Object[] a = new Object[Column.values().length];
			a[Column.Order.ordinal()] = pOrder;
			a[Column.Type.ordinal()] = pType;
			a[Column.Lat.ordinal()] = pLat;
			a[Column.Lon.ordinal()] = pLon;
			a[Column.Height.ordinal()] = pHeight;
			a[Column.Delay.ordinal()] = pDelay;
			a[Column.Radius.ordinal()] = pRadius;
			a[Column.SetUp.ordinal()] = pSetUp;
			a[Column.SetDown.ordinal()] = pSetDown;
			a[Column.Remove.ordinal()] = pRemove;
			a[Column.PTR.ordinal()] = pMissionItem;
			return a;
		}
	}
	
	
	static JTable missionTable = null;
	public void buildMissionTable(JMapViewer map) {
		if (mission == null)
			return;
		
		String[] columnNames = new String[Column.values().length];
		for (Column col : Column.values()) {
			columnNames[col.ordinal()] = col.getName();
		}
		Object[][] data = new Object[mission.getItems().size()][];
		
		Iterator<MissionItem> it = mission.getItems().iterator();
		int i = 0;
		while (it.hasNext()) {
			MissionItem mItem = it.next();
			
			switch (mItem.getType()) {
				case WAYPOINT: {
					Waypoint wp = (Waypoint) mItem;
					data[i] = (new MissionItemTableEntry(i, "WayPoint", wp.getCoordinate().getLat(), wp.getCoordinate().getLng(), wp.getCoordinate().getAltitude().valueInMeters(), wp.getDelay(), 0.0, "UP", "DOWN", "X", mItem)).toArray();
					break;
				}
				case SPLINE_WAYPOINT:
					//return new SplineWaypoint(referenceItem);
					break;
				case TAKEOFF: {
					msg_mission_item msg = mItem.packMissionItem().get(0);
					double alt = (double) msg.z;
					data[i] = (new MissionItemTableEntry(i, "Takeoff", 0.0, 0.0, alt, 0.0, 0.0, "-", "-", "X",  mItem)).toArray();
					break;	
				}
				case CHANGE_SPEED:
					//return new ChangeSpeed(referenceItem);
				case CAMERA_TRIGGER:
					//return new CameraTrigger(referenceItem);
				case EPM_GRIPPER:
					//return new EpmGripper(referenceItem);
				case RTL: {
					data[i] = (new MissionItemTableEntry(i, "RTL", 0.0, 0.0, 0.0, 0.0, 0.0, "-", "-", "X",  mItem)).toArray();
					break;
				}
				case LAND: {
					data[i] = (new MissionItemTableEntry(i, "LAND", 0.0, 0.0, 0.0, 0.0, 0.0, "-", "-", "X", mItem)).toArray();
					break;
				}
				case CIRCLE: { // Loiter
					Circle wp = (Circle) mItem;
					data[i] = (new MissionItemTableEntry(i, "Circle", wp.getCoordinate().getLat(), wp.getCoordinate().getLng(), wp.getCoordinate().getAltitude().valueInMeters(), 0.0, wp.getRadius(), "UP", "DOWN", "X", mItem)).toArray();
					break;
				}
				case ROI:
					//return new RegionOfInterest(referenceItem);
				case SURVEY:
					//return new Survey(referenceItem.getMission(), Collections.<Coord2D> emptyList());
				case CYLINDRICAL_SURVEY:
					//return new StructureScanner(referenceItem);
				default:
					break;
			}
			
			i++;
		}
		
	    missionTable = new JTable(data, columnNames);

	    Action upMissionItem = new AbstractAction()
	    {
	        /**
			 * 
			 */
			private static final long serialVersionUID = -6034512612820899651L;

			public void actionPerformed(ActionEvent e)
	        {      		
	        	JTable table = (JTable)e.getSource();
	            int modelRow = Integer.valueOf( e.getActionCommand() );
	            MissionItem mi = (MissionItem)table.getModel().getValueAt(modelRow, Column.PTR.ordinal());
	            if (modelRow == 0) {
	            	System.out.println("Reached the top");
	            	return;
	            }
	            ((DefaultTableModel) table.getModel()).moveRow(modelRow, modelRow, modelRow-1);
	            
	            Mission tmpMission = new Mission(Dashboard.drone);
	            for (int i = 0 ; i < table.getRowCount() ; i++) {
	            	mi = (MissionItem)table.getModel().getValueAt(i, Column.PTR.ordinal());
	            	tmpMission.addMissionItem(mi);
	            }
	            mission = tmpMission;
	            repaint(map);
	        }
	    };
	    
	    Action downMissionItem = new AbstractAction()
	    {
	        /**
			 * 
			 */
			private static final long serialVersionUID = 5678821170249360874L;

			public void actionPerformed(ActionEvent e)
	        {	        	
	            JTable table = (JTable)e.getSource();
	            int modelRow = Integer.valueOf( e.getActionCommand() );
	            MissionItem mi = (MissionItem)table.getModel().getValueAt(modelRow, Column.PTR.ordinal());
	            if (modelRow == table.getRowCount() - 1) {
	            	System.out.println("Reached the buttom");
	            	return;
	        	}

	            ((DefaultTableModel) table.getModel()).moveRow(modelRow, modelRow, modelRow+1);
	            
	            Mission tmpMission = new Mission(Dashboard.drone);
	            for (int i = 0 ; i < table.getRowCount() ; i++) {
	            	mi = (MissionItem)table.getModel().getValueAt(i, Column.PTR.ordinal());
	            	tmpMission.addMissionItem(mi);
	            }
	            mission = tmpMission;
	            repaint(map);
	        }
	    };
	    
	    Action deleteMissionItem = new AbstractAction()
	    {
	        /**
			 * 
			 */
			private static final long serialVersionUID = 5678821170249360874L;

			public void actionPerformed(ActionEvent e)
			{
	            JTable table = (JTable)e.getSource();
	            int modelRow = Integer.valueOf( e.getActionCommand() );
	            MissionItem mi = (MissionItem)table.getModel().getValueAt(modelRow, Column.PTR.ordinal());
	            ((DefaultTableModel) table.getModel()).removeRow(modelRow);
	            mission.removeWaypoint(mi);
	            repaint(map);
	        }
	    };
	     
	    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = -4975811431281379109L;
            public boolean isCellEditable(int rowIndex, int columnIndex) {
            	MissionItem mi = (MissionItem) missionTable.getValueAt(rowIndex, Column.PTR.ordinal());
            	MissionItemsType t = MissionItemsType.getMissionItemTypeByClass(mi.getClass());
            	Column col = Column.values()[columnIndex];
            	return t.pEditableFields.contains(col);
            }
	    };
	    
	    model.addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				int row = e.getFirstRow();
				int colidx = e.getColumn();
				if (colidx < 0)
					return; 
				
				Column col = Column.values()[colidx];				
				MissionItem mi = mission.getItems().get(row);
				switch (col) {
					case Height:
						if (mi instanceof Waypoint)
							((Waypoint) mi).setAltitude(new Altitude(Double.parseDouble(missionTable.getValueAt(row, colidx).toString())));
						else if (mi instanceof Circle)
							((Circle) mi).setAltitude(new Altitude(Double.parseDouble(missionTable.getValueAt(row, colidx).toString())));
						else if (mi instanceof Takeoff)
							((Takeoff) mi).setFinishedAlt(new Altitude(Double.parseDouble(missionTable.getValueAt(row, colidx).toString())));
						else 
							Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("Height was modified to irrelevant type");
						break;
					case Delay:
						((Waypoint) mi).setDelay(Double.parseDouble(missionTable.getValueAt(row, colidx).toString()));
						break;
					case Radius:
						((Circle) mi).setRadius(Double.parseDouble(missionTable.getValueAt(row, colidx).toString()));
						break;
					default:
						break;
				}
			}
		});
	    missionTable = new JTable(model);	    
	    new ButtonColumn(missionTable, upMissionItem, Column.SetUp.ordinal());	    
	    new ButtonColumn(missionTable, downMissionItem, Column.SetDown.ordinal());
	    new ButtonColumn(missionTable, deleteMissionItem, Column.Remove.ordinal());
	    Dashboard.window.areaMission.setViewportView(missionTable);
	    
	}
	
	// TALMA
	public void repaint(JMapViewer map) {
		unloadFromMap(map);
		repaintMission(map);
		loadToMap(map);
	}

	@Override
	public void initialize() {
		super.initialize();
	}
}
