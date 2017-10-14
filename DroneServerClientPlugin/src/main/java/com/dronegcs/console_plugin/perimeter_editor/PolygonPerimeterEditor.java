package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.Point;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.geo_tools.Coordinate;

/**
 * Created by taljmars on 3/27/17.
 */
public interface PolygonPerimeterEditor extends PerimeterEditor<PolygonPerimeter>  {

    Point addPoint(Coordinate coordinate) throws PerimeterUpdateException;

    void removePoint(Point point) throws PerimeterUpdateException;

    Point updatePoint(Point point) throws PerimeterUpdateException;
}
