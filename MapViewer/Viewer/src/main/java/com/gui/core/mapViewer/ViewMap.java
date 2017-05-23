//==============================================================================
//   JFXMapPane is a Java library for parsing raw weather data
//   Copyright (C) 2012 Jeffrey L Smith
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//    
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//    
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//    
//  For more information, please email jsmith.carlsbad@gmail.com
//    
//==============================================================================
package com.gui.core.mapViewer;

import com.geo_tools.Coordinate;
import com.gui.core.mapTileControl.MapTile;
import com.gui.core.mapTileControl.TileController;
import com.gui.core.mapTileSources.*;
import com.gui.is.interfaces.mapObjects.MapLine;
import com.gui.is.interfaces.mapObjects.MapMarker;
import com.gui.is.interfaces.mapObjects.MapPolygon;
import com.gui.is.interfaces.maptiles.TileSource;
import com.gui.is.mapTileSources.OsmMercator;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 *
 * @author smithjel
 */
public abstract class ViewMap extends Pane {
	private final static Logger LOGGER = LoggerFactory.getLogger(ViewMap.class);

	private static final int LOCATION_LABEL_PADDING = 35;

	public Pane RootPane = this;

	private Pane topPane;
	private HBox buttomPane;

	private TileSource tileSource;

	protected static final Point[] move = { new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1) };
	public static final int MIN_ZOOM = 0;
	public static final int MAX_ZOOM = 22;

	protected List<MapMarker> mapMarkerList;
	protected List<MapPolygon> mapPolygonList;
	protected List<MapLine> mapLineList;

	// X&Y position of the center of this JFXMapPane on the world 
	// in screen pixels for the current zoom level.
	protected Point center;

	// Current zoom level
	protected int zoom;
	protected Slider zoomSlider;
	protected Button zoomInButton;
	protected Button zoomOutButton;
	VBox zoomControlsVbox;

	private boolean IgnoreRepaint;
	private Rectangle ClipMask = new Rectangle();
	private Group tilesGroup = new Group();

	// Map menu
	private TextField CursorLocationText;
	private CheckBox cbShowMapMarker;

	private Point lastDragPoint = new Point();
	private boolean isMoving = false;

	protected transient TileController tileController;

	private SimpleIntegerProperty MapX;
	//public void setMapX( int val) { this.MapX.set( val); }
	public int getMapX() { return this.MapX.get(); }

	private SimpleIntegerProperty MapY;
	//public void setMapY( int val) { this.MapY.set( val); }
	public int getMapY() { return this.MapY.get(); }

	private SimpleIntegerProperty MapWidth;
	//public void setMapWidth(double val) { System.err.println("setMapWidth"); this.MapWidth.set( (int)val); }
	public int getMapWidth() { return this.MapWidth.get(); }

	private SimpleIntegerProperty MapHeight;
	//public void setMapHeight(double val) { this.MapHeight.set( (int)val); }
	public int getMapHeight() { return this.MapHeight.get(); }

	protected SimpleBooleanProperty mapPolygonsVisible = new SimpleBooleanProperty( true );
	public void setMapPolygonsVisible( boolean val ) { this.mapPolygonsVisible.set(val);  renderControl(); }
	public boolean getMapPolygonsVisible() { return this.mapPolygonsVisible.get(); }

	protected SimpleBooleanProperty mapRoutesVisible = new SimpleBooleanProperty( true );
	public void setMapRoutesVisible( boolean val ) { this.mapRoutesVisible.set(val);  renderControl(); }
	public boolean getMapRoutesVisible() { return this.mapRoutesVisible.get(); }

	protected SimpleBooleanProperty showZoomControls = new SimpleBooleanProperty( true );
	public void setShowZoomControls( boolean val ) { this.showZoomControls.set(val); renderControl(); }
	public boolean getShowZoomControls() { return this.showZoomControls.get(); }

	protected SimpleBooleanProperty setMonochromeMode = new SimpleBooleanProperty( false );
	public void setMonochromeMode( boolean val ) { this.setMonochromeMode.set(val); renderControl(); }
	public boolean getMonochromeMode() { return this.setMonochromeMode.get(); }

	protected SimpleBooleanProperty mapMarkersVisible;
	public void setMapMarkersVisible( boolean val ) { this.tileGridVisible.set(val);  renderControl(); }
	public boolean getMapMarkersVisible() { return this.tileGridVisible.get(); }

	protected SimpleBooleanProperty tileGridVisible;
	public void setMapGridVisible( boolean val ) { this.tileGridVisible.set(val);  renderControl(); }
	public boolean getMapGridVisible() { return this.tileGridVisible.get(); }

	protected SimpleBooleanProperty cursorLocationVisible;
	public void setCursorLocationVisible( boolean val ) { this.cursorLocationVisible.set(val); }
	public boolean getCursorLocationVisible() { return this.cursorLocationVisible.get(); }

	private static TileSource[] mapTilesSources = new TileSource[]
			{ 
					new BingAerialTileSource(),
					new OsmIntelMapTileSource(),
					new OsmHotMapTileSource(),
					new OsmCartoMapTileSource(),
					new OsmDarkMapTileSource(),
					new OsmCycleMapTileSource(),
			};

	public ViewMap() {
		this( mapTilesSources[0], 0, 0, 400, 400, 11 );
	}

	private ViewMap( TileSource ts, int x, int y, int width, int height, int initial_zoom ) {

		LOGGER.info("In ViewMap Constructor");
		
		CursorLocationText = new TextField("");
		CursorLocationText.setEditable(false);
		CursorLocationText.setPrefWidth(240);
		CursorLocationText.setStyle("-fx-control-inner-background: white");

		cbShowMapMarker = new CheckBox("Show Markers");
		cbShowMapMarker.setSelected(true);
		cbShowMapMarker.setOnAction( e -> setMapMarkerVisible(cbShowMapMarker.isSelected()));

		this.MapX = new SimpleIntegerProperty(x);
		this.MapX.addListener( (observable, oldValue, newValue) -> {
			int val = (int) newValue;
			RootPane.setLayoutX(val);
			ClipMask.setLayoutX(val);
		});

		this.MapY = new SimpleIntegerProperty(y);
		this.MapY.addListener( (observable, oldValue, newValue) -> {
			int val = (int) newValue;
			RootPane.setLayoutY(val);
			ClipMask.setLayoutY(val);
		});

		this.MapWidth = new SimpleIntegerProperty(width);
		this.MapWidth.addListener( (observable, oldValue, newValue) -> {
			int val = (int) newValue;
			//RootPane.setMinWidth(val);
			//RootPane.setMaxWidth(val);
			RootPane.setPrefWidth(val);
			ClipMask.setWidth(val);
			CursorLocationText.setLayoutX( val / 2 );
			renderControl();
		});

		this.MapHeight = new SimpleIntegerProperty(height);
		this.MapHeight.addListener( (observable, oldValue, newValue) -> {
			int val = (int) newValue;
			RootPane.setMinHeight(val);
			RootPane.setMaxHeight(val);
			RootPane.setPrefHeight(val);
			ClipMask.setHeight(val);
			renderControl();
		});

		this.showZoomControls.addListener( (observable, oldValue, newValue) -> setZoomControlsVisible(newValue));

		this.mapMarkersVisible = new SimpleBooleanProperty(true);
		this.tileGridVisible = new SimpleBooleanProperty(false);
		this.cursorLocationVisible = new SimpleBooleanProperty(true);

		this.tileSource = ts;
		this.tileController = new TileController(tileSource);

		// Thread safe lists
		mapMarkerList = Collections.synchronizedList(new LinkedList<>());
		mapPolygonList = Collections.synchronizedList(new LinkedList<>());
		mapLineList = Collections.synchronizedList(new LinkedList<>());

		IgnoreRepaint = true;
		initializeZoomSlider();
		IgnoreRepaint = false;

		this.zoom = initial_zoom;


		this.setMinSize(tileSource.getTileSize(), tileSource.getTileSize());
		this.setPrefSize(width, height);
		this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

		setDisplayPositionByLatLon( 0, 0, 3 );

		this.ClipMask.setFill(Color.WHITE);

		/*
		 * Sliding down fingers on screen also count as scroll event, that is why
		 * we are counting touch point
		 */
		this.tilesGroup.setOnScroll( me -> {
			if (me.getTouchCount() != 1) {
				lastDragPoint = null;
			}
			
			if (me.getTouchCount() == 0) {
				if( me.getDeltaY() > 0 && getZoom() < MAX_ZOOM ) {
					zoomIn();
				} else if( getZoom() > MIN_ZOOM ) {
					zoomOut();
				}
			}
		});

		this.tilesGroup.setOnMouseMoved( me -> {
			if (me.isPrimaryButtonDown()) {
				Point p = new Point( (int)me.getX(), (int)me.getY());
				if (lastDragPoint != null) {
					int diffx = lastDragPoint.x - p.x;
					int diffy = lastDragPoint.y - p.y;
					moveMap(diffx, diffy);
				}
				lastDragPoint = p;
			} else if( cursorLocationVisible.get() == true ) {
				Coordinate mouseLocation = getPosition( (int)me.getX(), (int)me.getY() );
				updateLocationLabel(mouseLocation);
			}
		});
		
		this.tilesGroup.setOnZoom( me -> {
			if( me.getZoomFactor() > 1 && getZoom() < MAX_ZOOM ) {
				zoomIn();
			} else if( getZoom() > MIN_ZOOM ) {
				zoomOut();
			}
		});
		
		this.tilesGroup.setOnTouchMoved( me -> isMoving = true);

		this.tilesGroup.setOnMouseClicked( me -> HandleMouseClick(me));


		this.tilesGroup.setOnMousePressed( me -> {
			if (me.isPrimaryButtonDown()) {
				lastDragPoint = null;
				isMoving = true;
			}
		});

		this.tilesGroup.setOnMouseReleased( me -> {
			if (isMoving) {
				Point p = new Point((int)me.getX(), (int)me.getY());
				if (lastDragPoint != null) {
					int diffx = lastDragPoint.x - p.x;
					int diffy = lastDragPoint.y - p.y;
					moveMap(diffx, diffy);
				}
				lastDragPoint = null;
				isMoving = false;
			}
		});

		this.tilesGroup.setOnMouseDragged( me -> {
			if (!isMoving)
				return;
			
			Point p = new Point((int)me.getX(), (int)me.getY());
			if (lastDragPoint != null) {
				int diffx = lastDragPoint.x - p.x;
				int diffy = lastDragPoint.y - p.y;
				moveMap(diffx, diffy);
			}

			if (me.isPrimaryButtonDown() && lastDragPoint == null)
				isMoving = true;
			
			lastDragPoint = p;
		});

		this.tilesGroup.setOnMouseDragReleased( me -> {
			if (!isMoving)
				return;                

			Point p = new Point((int)me.getX(), (int)me.getY());
			if (lastDragPoint != null) {
				int diffx = lastDragPoint.x - p.x;
				int diffy = lastDragPoint.y - p.y;
				moveMap(diffx, diffy);
			}
		});

		this.tilesGroup.setClip(ClipMask);

		topPane = getMapTopPane();
		buttomPane = getMapButtomPane();

		this.getChildren().add( tilesGroup );
		this.getChildren().add( zoomControlsVbox );

		if (topPane != null) {
			this.getChildren().add(topPane);
			topPane.setLayoutX( MapWidth.get() / 1.5 );
			topPane.setLayoutY( LOCATION_LABEL_PADDING / 2 );
		}

		if (buttomPane != null) {
			this.getChildren().add(buttomPane);
			//buttomPane.setLayoutX( (((MapWidth.get() - Screen.getPrimary().getBounds().getWidth() * AppConfig.FRAME_CONTAINER_REDUCE_PRECENTAGE) / 2) - (buttomPane.getWidth() / 2)) );
			buttomPane.setLayoutY( MapHeight.doubleValue() - LOCATION_LABEL_PADDING );
		}
		
		buttomPane.setVisible(false);
	}

	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");

		setTileSource(mapTilesSources[0]);
		setMonochromeMode(false);
		setMapBounds( 0, 0, 800, 600 );
	}

	public Pane getMapTopPane() {
		HBox p = new HBox();
		p.setAlignment(Pos.CENTER);
		p.setPadding(new Insets(5, 5, 5, 5));
	    p.setSpacing(5);
	    p.setStyle("-fx-background-color: white;");
		p.getChildren().add(CursorLocationText);
		
		Button btnMapMenu = new Button("Map Menu");
		btnMapMenu.setOnAction( (event) -> buttomPane.setVisible(!buttomPane.isVisible()));
		p.getChildren().add(btnMapMenu);

		return p;
	}

	@SuppressWarnings("unchecked")
	public HBox getMapButtomPane() {
		HBox p = new HBox();
		p.setAlignment(Pos.CENTER);
		p.setPadding(new Insets(5, 5, 5, 5));
	    p.setSpacing(5);
	    p.setStyle("-fx-background-color: white;");
	    
		ComboBox<TileSource> cmbTileSourceSelector = new ComboBox<TileSource>();
		cmbTileSourceSelector.getItems().addAll(new Vector<TileSource>(Arrays.asList(mapTilesSources)));
		cmbTileSourceSelector.setValue(tileSource);
		cmbTileSourceSelector.setOnAction( e -> setTileSource(((ComboBox<TileSource>) e.getSource()).getValue()));
		p.getChildren().add(cmbTileSourceSelector);
	    
	    Button btnSetDisplayToFitMarkers = new Button("Fit All Markers");
		btnSetDisplayToFitMarkers.setOnAction( e -> setDisplayToFitMapMarkers());
		p.getChildren().add(btnSetDisplayToFitMarkers);
		
		CheckBox cbShowZoomControls = new CheckBox("Zoom Controls");
		cbShowZoomControls.setSelected(true);
		cbShowZoomControls.setOnAction( e -> setShowZoomControls(cbShowZoomControls.isSelected()));
		p.getChildren().add(cbShowZoomControls);
		
		CheckBox cbMonochromeMode = new CheckBox("Monochrome");
		cbMonochromeMode.setOnAction( (event) -> setMonochromeMode(cbMonochromeMode.isSelected()));
		p.getChildren().add(cbMonochromeMode);
		
		return p;
	}

	/**
	 * Calculates the position on the map of a given coordinate
	 *
	 * @param lat Latitude
	 * @param lon longitude
	 * @param offset Offset respect Latitude
	 * @param checkOutside check if the point is outside the displayed area
	 * @return Integer the radius in pixels
	 */
	public Double getLatOffset(double lat, double lon, double offset, boolean checkOutside) {
		Point p = tileSource.latLonToXY(lat + offset, lon, zoom);
		Double y = (double) (p.y - (center.y - getMapHeight() / 2));
		if (checkOutside && (y < 0 || y > getMapHeight())) {
			return null;
		}
		return y;
	}

	/**
	 * Calculates the position on the map of a given coordinate
	 *
	 * @param marker MapMarker object that define the x,y coordinate
	 * @return Integer the radius in pixels
	 */
	public Double getRadius(MapMarker marker) {       
		if (marker == null)
			return null;
		return marker.getRadius() / getMeterPerPixel();
	}

	/**
	 * Gets the meter per pixel.
	 *
	 * @return the meter per pixel
	 */
	public double getMeterPerPixel() {
		Point origin = new Point(5, 5);
		Point center = new Point((int) getWidth() / 2, (int) getHeight() / 2);

		double pDistance = center.distance(origin);

		Coordinate originCoord = getPosition(origin);
		Coordinate centerCoord = getPosition(center);

		double mDistance = tileSource.getDistance(originCoord.getLat(), originCoord.getLon(),centerCoord.getLat(), centerCoord.getLon());

		return mDistance / pDistance;
	}

	private void updateLocationLabel(Coordinate coord) {
		double lat = coord.getLat();
		double lon = coord.getLon();
		CursorLocationText.setText( "Lat: " + String.format("%2.5f", lat) + " Lon: " + String.format("%3.6f", lon) + " Zoom:" + zoom );
	}

	protected abstract void HandleMouseClick(MouseEvent me);

	protected void initializeZoomSlider() {
		zoomSlider = new Slider();
		zoomSlider.setOrientation(Orientation.VERTICAL);
		zoomSlider.setMin(MIN_ZOOM);
		zoomSlider.setMax(tileController.getTileSource().getMaxZoom());
		zoomSlider.setValue(MIN_ZOOM);

		zoomSlider.setPrefSize(30, 150);

		zoomSlider.valueProperty().addListener( (ov, old_val, new_val) -> setZoom(new_val.intValue()));

		Image imagePlus = new Image(this.getClass().getResource("/com/mapImages/plus.png").toString());
		zoomInButton = new Button();
		zoomInButton.setGraphic(new ImageView(imagePlus));
		zoomInButton.setOnAction( e -> {if( IgnoreRepaint == false ) zoomIn();});

		Image imageMinus = new Image(this.getClass().getResource("/com/mapImages/minus.png").toString());
		zoomOutButton = new Button();
		zoomOutButton.setGraphic(new ImageView(imageMinus));
		zoomOutButton.setOnAction(e -> {if( IgnoreRepaint == false ) zoomOut();});

		zoomControlsVbox = new VBox();
		zoomControlsVbox.getChildren().add(zoomInButton);
		zoomControlsVbox.getChildren().add(zoomSlider);
		zoomControlsVbox.getChildren().add(zoomOutButton);
		zoomControlsVbox.setLayoutX(10);
		zoomControlsVbox.setLayoutY(10);
	}

	public void setMapBounds( int x, int y, int width, int height ) {
		this.MapX.set(x);
		this.MapY.set(y);
		this.MapWidth.set(width);
		this.MapHeight.set(height);
	}

	public void setDisplayPositionByLatLon(double lat, double lon, int zoom) {
		setDisplayPositionByLatLon(new Point( (int)(getMapWidth() / 2), (int)(getMapHeight() / 2)), lat, lon, zoom);
	}

	public void setDisplayPositionByLatLon(Point mapPoint, double lat, double lon, int zoom) {
		int x = OsmMercator.LonToX(lon, zoom);
		int y = OsmMercator.LatToY(lat, zoom);
		setDisplayPosition(mapPoint, x, y, zoom);
	}

	protected void setDisplayPosition(Coordinate coord, int zoom) {
		setDisplayPositionByLatLon(coord.getLat(), coord.getLon(), zoom);
	}

	public void setDisplayPosition(int x, int y, int zoom) {
		setDisplayPosition(new Point( (int)(getMapWidth() / 2), (int)(getMapHeight() / 2)), x, y, zoom);
	}

	public void setDisplayPosition(Point mapPoint, int x, int y, int zoom) {

		if (zoom > tileController.getTileSource().getMaxZoom() || zoom < MIN_ZOOM)
			return;

		// Get the plain tile number
		Point p = new Point();
		p.x = x - mapPoint.x + (int)(getMapWidth() / 2);
		p.y = y - mapPoint.y + (int)(getMapHeight() / 2);
		center = p;
		IgnoreRepaint = true;
		try {
			int oldZoom = this.zoom;
			this.zoom = zoom;
			if (oldZoom != zoom) {
				zoomChanged(oldZoom);
			}
			if (zoomSlider.getValue() != zoom) {
				zoomSlider.setValue(zoom);
			}
		} finally {
			IgnoreRepaint = false;
			renderControl();
		}
	}

	public void setDisplayToFitMapMarkers() {
		if (mapMarkerList == null || mapMarkerList.isEmpty()) {
			return;
		}
		int x_min = Integer.MAX_VALUE;
		int y_min = Integer.MAX_VALUE;
		int x_max = Integer.MIN_VALUE;
		int y_max = Integer.MIN_VALUE;
		int mapZoomMax = tileController.getTileSource().getMaxZoom();
		for (MapMarker marker : mapMarkerList) {
			int x = OsmMercator.LonToX(marker.getLon(), mapZoomMax);
			int y = OsmMercator.LatToY(marker.getLat(), mapZoomMax);
			x_max = Math.max(x_max, x);
			y_max = Math.max(y_max, y);
			x_min = Math.min(x_min, x);
			y_min = Math.min(y_min, y);
		}
		int height = (int)Math.max(0, getMapHeight());
		int width  = (int)Math.max(0, getMapWidth());
		int newZoom = mapZoomMax;
		int x = x_max - x_min;
		int y = y_max - y_min;
		while (x > width || y > height) {
			newZoom--;
			x >>= 1;
			y >>= 1;
		}
		x = x_min + (x_max - x_min) / 2;
		y = y_min + (y_max - y_min) / 2;
		int z = 1 << (mapZoomMax - newZoom);
		x /= z;
		y /= z;
		setDisplayPosition(x, y, newZoom);
	}

	public void setDisplayToFitMapRectangle() {
		if (mapPolygonList == null || mapPolygonList.isEmpty())
			return;
		
		int x_min = Integer.MAX_VALUE;
		int y_min = Integer.MAX_VALUE;
		int x_max = Integer.MIN_VALUE;
		int y_max = Integer.MIN_VALUE;
		int mapZoomMax = tileController.getTileSource().getMaxZoom();
		//        for (MapPolygon rectangle : mapPolygonList) {
		//            x_max = Math.max(x_max, OsmMercator.LonToX(rectangle.getBottomRight().getLon(), mapZoomMax));
		//            y_max = Math.max(y_max, OsmMercator.LatToY(rectangle.getTopLeft().getLat(), mapZoomMax));
		//            x_min = Math.min(x_min, OsmMercator.LonToX(rectangle.getTopLeft().getLon(), mapZoomMax));
		//            y_min = Math.min(y_min, OsmMercator.LatToY(rectangle.getBottomRight().getLat(), mapZoomMax));
		//        }
		int height = (int)Math.max(0, getMapHeight());
		int width  = (int)Math.max(0, getMapWidth());
		int newZoom = mapZoomMax;
		int x = x_max - x_min;
		int y = y_max - y_min;
		while (x > width || y > height) {
			newZoom--;
			x >>= 1;
			y >>= 1;
		}
		x = x_min + (x_max - x_min) / 2;
		y = y_min + (y_max - y_min) / 2;
		int z = 1 << (mapZoomMax - newZoom);
		x /= z;
		y /= z;
		setDisplayPosition(x, y, newZoom);
	}

	// Calculates the latitude/longitude coordinate of the center of the
	// currently displayed map area.
	public Coordinate getPosition() {
		double lon = OsmMercator.XToLon(center.x, zoom);
		double lat = OsmMercator.YToLat(center.y, zoom);
		return new Coordinate(lat, lon);
	}

	// Converts the relative pixel coordinate (regarding the top left corner of
	// the displayed map) into a latitude / longitude coordinate
	public Coordinate getPosition(Point mapPoint) {
		return getPosition(mapPoint.x, mapPoint.y);
	}

	// Converts the relative pixel coordinate (regarding the top left corner of
	// the displayed map) into a latitude / longitude coordinate
	public Coordinate getPosition(int mapPointX, int mapPointY) {
		int x = (int)(center.x + mapPointX - getMapWidth()  / 2);
		int y = (int)(center.y + mapPointY - getMapHeight() / 2);
		double lon = OsmMercator.XToLon(x, zoom);
		double lat = OsmMercator.YToLat(y, zoom);
		return new Coordinate(lat, lon);
	}

	// Calculates the position on the map of a given coordinate
	public Point getMapPosition(double lat, double lon, boolean checkOutside) {
		int x = OsmMercator.LonToX(lon, zoom);
		int y = OsmMercator.LatToY(lat, zoom);
		x -= center.x - getMapWidth() / 2;
		y -= center.y - getMapHeight() / 2;
		if (checkOutside) {
			if (x < 0 || y < 0 || x > getMapWidth() || y > getMapHeight()) {
				return null;
			}
		}
		return new Point(x, y);
	}

	// Calculates the position on the map of a given coordinate
	public Point getMapPosition(double lat, double lon) {
		return getMapPosition(lat, lon, true);
	}

	// Calculates the position on the map of a given coordinate
	public Point getMapPosition(Coordinate coord) {
		if (coord != null)
			return getMapPosition(coord.getLat(), coord.getLon());
		return null;
	}

	// Calculates the position on the map of a given coordinate
	public Point getMapPosition(Coordinate coord, boolean checkOutside) {
		if (coord != null)
			return getMapPosition(coord.getLat(), coord.getLon(), checkOutside);
		return null;
	}

	public Point getMapPoint(Coordinate coord) {
		return getMapPosition(coord.getLat(), coord.getLon(), false);
	}

	public void moveMap(int x, int y) {
		center.x += x;
		center.y += y;
		renderControl();
	}

	public int getZoom() {
		return zoom;
	}

	public void zoomIn() {
		setZoom(zoom + 1);
	}

	public void zoomIn(Point mapPoint) {
		setZoom(zoom + 1, mapPoint);
	}

	public void zoomOut() {
		setZoom(zoom - 1);
	}

	public void zoomOut(Point mapPoint) {
		setZoom(zoom - 1, mapPoint);
	}

	public void setZoom(int zoom, Point mapPoint) {
		if (zoom > tileController.getTileSource().getMaxZoom() || 
			zoom < tileController.getTileSource().getMinZoom() || 
			zoom == this.zoom)
			return;
		
		Coordinate zoomPos = getPosition(mapPoint);
		setDisplayPositionByLatLon(mapPoint, zoomPos.getLat(), zoomPos.getLon(), zoom);
	}

	public void setZoom(int zoom) {
		setZoom(zoom, new Point( (int)(getMapWidth() / 2), (int)(getMapHeight() / 2)));
	}

	protected void zoomChanged(int oldZoom) {
		zoomSlider.setTooltip(new Tooltip("Zoom level " + zoom));
		zoomInButton.setTooltip(new Tooltip("Zoom to level " + (zoom + 1)));
		zoomOutButton.setTooltip(new Tooltip("Zoom to level " + (zoom - 1)));
		zoomOutButton.setDisable(!(zoom > tileController.getTileSource().getMinZoom()));
		zoomInButton.setDisable(!(zoom < tileController.getTileSource().getMaxZoom()));
	}

	public void setMapMarkerVisible(boolean mapMarkersVisible) {
		this.mapMarkersVisible.set( mapMarkersVisible );
		renderControl();
	}

	public void setMapMarkerList(List<MapMarker> mapMarkerList) {
		this.mapMarkerList = mapMarkerList;
		renderControl();
	}

	public List<MapMarker> getMapMarkerList() {
		return mapMarkerList;
	}

	public void setMapPolygonList(List<MapPolygon> mapPolygonList) {
		this.mapPolygonList = mapPolygonList;
		renderControl();
	}

	public List<MapPolygon> getMapRectangleList() {
		return mapPolygonList;
	}

	public void addMapLine(MapLine line) {
		mapLineList.add(line);
		renderControl();
	}

	public void removeMapLine(MapLine line) {
		mapLineList.remove(line);
		renderControl();
	}

	public void addMapMarker(MapMarker marker) {
		mapMarkerList.add(marker);
		renderControl();
	}

	public void removeMapMarker(MapMarker marker) {
		mapMarkerList.remove(marker);
		renderControl();
	}

	public void addMapPolygon(MapPolygon polygon) {
		mapPolygonList.add(polygon);
		renderControl();
	}

	public void removeMapPolygon(MapPolygon polygon) {
		mapPolygonList.remove(polygon);
		renderControl();
	}

	public void setZoomControlsVisible(boolean visible) {
		this.zoomSlider.setVisible(visible);
		this.zoomInButton.setVisible(visible);
		this.zoomOutButton.setVisible(visible);
		this.zoomControlsVbox.setVisible(visible);
	}

	public boolean getZoomContolsVisible() {
		return zoomSlider.isVisible();
	}

	public void setTileSource(TileSource tileSource) {
		int minZoom = tileSource.getMinZoom();
		int maxZoom = tileSource.getMaxZoom();
		if (maxZoom > MAX_ZOOM)
			throw new RuntimeException("Maximum zoom level too high");
		
		if (minZoom < MIN_ZOOM)
			throw new RuntimeException("Minumim zoom level too low");
		
		this.tileSource = tileSource;
		tileController.setTileSource(tileSource);
		zoomSlider.setMin(tileSource.getMinZoom());
		zoomSlider.setMax(tileSource.getMaxZoom());

		if (zoom > tileSource.getMaxZoom())
			setZoom(tileSource.getMaxZoom());

		renderControl();
	}

	public void tileLoadingFinished(MapTile tile, boolean success) {
		renderControl();
	}

	protected void renderControl() {
		
		//Platform.runLater( () -> {

		int iMove = 0;
		int tilesize = tileSource.getTileSize();
		int tilex = (center.x / tilesize);
		int tiley = (center.y / tilesize);
		int off_x = (center.x % tilesize);
		int off_y = (center.y % tilesize);

		int w2 = (int) (getMapWidth() / 2);
		int h2 = (int) (getMapHeight() / 2);
		int posx = w2 - off_x;
		int posy = h2 - off_y;

		int diff_left = off_x;
		int diff_right = tilesize - off_x;
		int diff_top = off_y;
		int diff_bottom = tilesize - off_y;

		boolean start_left = diff_left < diff_right;
		boolean start_top = diff_top < diff_bottom;

		this.tilesGroup.getChildren().clear();
		this.ClipMask.setTranslateX( -(this.MapX.get()) );
		this.ClipMask.setTranslateY( -(this.MapY.get()) );


		if (getShowZoomControls() == true) setZoomControlsVisible(true);
		else setZoomControlsVisible(false);

		if (start_top) {
			if (start_left) iMove = 2;
			else iMove = 3;
		} 
		else {
			if (start_left) iMove = 1;
			else iMove = 0;
		}
		// calculate the visibility borders
		int x_min = -tilesize;
		int y_min = -tilesize;
		int x_max = (int)getMapWidth();
		int y_max = (int)getMapHeight();

		// paint the tiles in a spiral, starting from center of the map
		boolean painted = true;
		int x = 0;
		while (painted) {
			painted = false;
			for (int i = 0; i < 4; i++) {
				if (i % 2 == 0)
					x++;
				
				for (int j = 0; j < x; j++) {
					if (x_min <= posx && posx <= x_max && y_min <= posy && posy <= y_max) {
						// tile is visible
						MapTile tile = tileController.getTile(tilex, tiley, zoom);
						if (tile != null) {
							painted = true;

							tile.ImgView.translateXProperty().set(posx);
							tile.ImgView.translateYProperty().set(posy);

							if( getMonochromeMode() == true ) {
								ColorAdjust monochrome = new ColorAdjust();
								monochrome.setSaturation(-1); monochrome.setContrast(-0.3); monochrome.setBrightness(-0.3);
								tile.ImgView.setEffect(monochrome);
							}

							this.tilesGroup.getChildren().add(tile.ImgView);

							if ( tileGridVisible.get() == true ) {
								Path path = new Path();
								path.getElements().add(new MoveTo( posx, posy ) );  
								path.getElements().add(new LineTo( posx + tilesize, posy           ) );  
								path.getElements().add(new LineTo( posx + tilesize, posy + tilesize) );  
								path.getElements().add(new LineTo( posx           , posy + tilesize) );  
								path.getElements().add(new LineTo( posx           , posy           ) );  
								path.setStrokeWidth(1);
								path.setStroke(Color.BLACK);
								this.tilesGroup.getChildren().add(path);
							}
						}
					}
					Point p = move[iMove];
					posx += p.x * tilesize;
					posy += p.y * tilesize;
					tilex += p.x;
					tiley += p.y;
				}
				iMove = (iMove + 1) % move.length;
			}
		}

		if (mapPolygonsVisible.get() && mapPolygonList != null)
			for (MapPolygon polygon : mapPolygonList) polygon.Render(this, tilesGroup);

		if (mapMarkersVisible.get() && mapMarkerList != null) {
			for (MapMarker marker : mapMarkerList) {
				Point p = getMapPosition(marker.getLat(), marker.getLon());
				if (p != null) 
					marker.Render(tilesGroup, p, getRadius(marker));
			}
			
			for( MapLine line: mapLineList)
				line.Render(this, tilesGroup);
		}
	}
}
