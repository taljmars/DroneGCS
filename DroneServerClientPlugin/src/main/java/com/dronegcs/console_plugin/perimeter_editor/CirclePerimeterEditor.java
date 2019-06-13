package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Point;
import com.geo_tools.Coordinate;

/**
 * Created by taljmars on 3/27/17.
 */
public interface CirclePerimeterEditor extends PerimeterEditor<CirclePerimeter> {

    void setRadius(double radius);

    Point setCenter(Coordinate position);

    Point getCenter();
}
