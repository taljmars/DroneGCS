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
package gui.core.mapViewerObjects;

import java.util.ArrayList;
import gui.is.Coordinate;
import gui.is.interfaces.mapObjects.MapLine;
import javafx.scene.paint.Color;
import mavlink.is.utils.geoTools.GeoTools;
/**
 *
 * @author smithjel
 */
public class MapVectorImpl extends MapLineImpl {
        
    public MapVectorImpl() {
    	super("",Color.BLUE,1,1,new ArrayList<>());
    }
    
    public MapVectorImpl(Coordinate start, Coordinate end) {
    	this("",Color.BLUE,1,1,start, end);
    }
    

    public MapVectorImpl(String n, Color c, double width, double dashOffset, Coordinate start, Coordinate end) {
        super(n, c, width, dashOffset, new ArrayList<>());
        addCoordinate(start);
        addCoordinate(end);
    }
 
    

    public MapVectorImpl(MapVectorImpl mapVectorImpl) {
    	super(mapVectorImpl);
	}

	@Override
    public void addCoordinate(Coordinate coordinate) {
		if (this.Coordinates.size() == 2)
			return;
		
        this.Coordinates.add(coordinate);
    }

    @Override
    public String toString() {
        return "MapVector" + Name;
    }

	@Override
	public double getBearing() {
		return GeoTools.getHeadingFromCoordinates(Coordinates.get(0), Coordinates.get(1));
	}

    public MapLine clone() {
    	return new MapVectorImpl(this);
    }
    
}
