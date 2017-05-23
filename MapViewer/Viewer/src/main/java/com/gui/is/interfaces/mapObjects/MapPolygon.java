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
package com.gui.is.interfaces.mapObjects;

import com.geo_tools.Coordinate;
import com.gui.core.mapViewer.ViewMap;
import javafx.scene.Group;
import javafx.scene.paint.Color;

import java.util.List;

public interface MapPolygon extends MapObject {
    
    /**
     * @return Latitude/Longitude pairs
     */
    public List<Coordinate> getCoordinates();
    
    public Color getOutlineColor();
    
    public Color getFillColor();
    
    public void Render(ViewMap viewer, Group g);

	public void addCoordinate(Coordinate coordinate);

	public boolean contains(Coordinate coord);

	public Coordinate getClosestPointOnEdge(Coordinate coord);
    
}
