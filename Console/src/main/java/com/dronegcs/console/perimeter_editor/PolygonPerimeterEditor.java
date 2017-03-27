package com.dronegcs.console.perimeter_editor;

import com.dronedb.persistence.scheme.perimeter.Point;
import com.geo_tools.Coordinate;

/**
 * Created by oem on 3/27/17.
 */
public interface PolygonPerimeterEditor {

    Point addPoint(Coordinate coordinate);
}
