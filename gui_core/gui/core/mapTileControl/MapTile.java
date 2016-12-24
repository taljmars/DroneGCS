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
package gui.core.mapTileControl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import gui.is.interfaces.maptiles.TileSource;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tools.os_utilities.Environment;

public class MapTile {

    public ImageView ImgView = new ImageView();
    
    public static final CharSequence MAP_TILES_STORAGE = "MAP_TILES";

    protected TileSource source;
    protected int xtile;
    protected int ytile;
    protected int zoom;
    protected String tileLocation;

	private String key;

    public MapTile(TileSource source, int xtile, int ytile, int zoom) {
        this.source = source;
        this.xtile = xtile;
        this.ytile = ytile;
        this.zoom = zoom;
        try {
			this.tileLocation = source.getTileUrl(zoom, xtile, ytile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if (this.tileLocation.startsWith("http")) {
            Image delayed_img = new Image(this.getClass().getResource("/mapImages/hourglass.png").toString());
            this.ImgView.setImage( delayed_img );
            this.ImgView.setFitWidth(256);
            this.ImgView.setFitHeight(256);
            this.loadImage(this.tileLocation);
            
        } else {
        	System.err.println("Unknown Tile [" +  this.tileLocation + "]");
        }
        
        this.key = getTileKey(source, xtile, ytile, zoom);
    }

    public static String getTileKey(TileSource source, int xtile, int ytile, int zoom) {
    	return zoom + "/" + xtile + "/" + ytile + "@" + source.getName();
	}

    /**
     * @return tile number on the x axis of this tile
     */
    public int getXtile() {
        return xtile;
    }

    /**
     * @return tile number on the y axis of this tile
     */
    public int getYtile() {
        return ytile;
    }

    public int getZoom() {
        return zoom;
    }

    public void setImage(Image image) {
        this.ImgView.setImage(image);
    }

    public void animateWaitImage() {
        double angle = this.ImgView.getRotate();
        angle += 1;
        this.ImgView.setRotate( angle );
    }
    
    public void animateWaitComplete() {
        this.ImgView.setRotate( 0 );
    }

    private void loadImage(java.lang.String url) {
        final Image img = new Image(url ,true);

        //You can add a specific action when each frame is started.
        final AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                animateWaitImage();
            }
        };
        
        timer.start();
        
        img.progressProperty().addListener( (ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
        	try {
                double percentLoaded = new_val.doubleValue();
                if (percentLoaded == 1.0) {
                    timer.stop();
                    animateWaitComplete();
                    setImage(img);
					saveToDisk(img, url);
                } 
                else {
                    animateWaitImage();
                }
            }
        	catch (URISyntaxException e) {
        		System.err.println("Failed to save tile");
				e.printStackTrace();
			}
        });
        
        img.errorProperty().addListener( (arg) -> {
        	try {
        		timer.stop();
        		File file;
				file = loadFromDisk(url);
				if (file.exists())
	        		loadImage(file.toURI().toString());
	        	else 
	        		loadImage(this.getClass().getResource("/mapImages/error.png").toString());
			}
        	catch (URISyntaxException e) {
				loadImage("gui/core/mapTileControl/images/error.png");
				System.err.println("Failed to load tile");
				e.printStackTrace();
			}
        });
        
    }

	public String getKey() {
		return key;
	}
	
	private void saveToDisk(Image img, String fileName) throws URISyntaxException {
		if (img.isError())
			return;
		
		String extention = getTileExtention(fileName);
		if (extention == null)
			return;
		
        File file = new File(rebuildLegalPath(fileName));
        if (file.exists())
        	return;
        
        File parent = new File(file.getParent());
        if (!parent.exists())
        	parent.mkdirs();
		
        System.out.println("Tile '" + file + "' not exist, saving to disk");
    	BufferedImage bImage = SwingFXUtils.fromFXImage(img, null);
        try {
			ImageIO.write(bImage, extention, file);
		} catch (IOException e) {
			System.err.println("Failed to dump tile");
			e.printStackTrace();
		}
	}
	
	private File loadFromDisk(String url) throws URISyntaxException {
        return new File(rebuildLegalPath(url));
	}
	
	private String rebuildLegalPath(String path) throws URISyntaxException {
		String newPath = path.toString().replace("http://", Environment.getRunningEnvCacheDirectory() + Environment.DIR_SEPERATOR + MAP_TILES_STORAGE + Environment.DIR_SEPERATOR);
		newPath = newPath.replace("https://", Environment.getRunningEnvCacheDirectory() + Environment.DIR_SEPERATOR + MAP_TILES_STORAGE + Environment.DIR_SEPERATOR);
        newPath = newPath.replace("/", "//");
        newPath = newPath.replace("?", "");
        return newPath;
	}
	
	private String getTileExtention(String file) {
		String extention = "";
		int i = file.lastIndexOf('.');
		if (i <= 0)
			return null;
		
		int j = file.lastIndexOf('?');
		if (j <= 0)
			extention = file.substring(i+1);
		else
			extention = file.substring(i+1, j);
		
		return extention;
	}

}
