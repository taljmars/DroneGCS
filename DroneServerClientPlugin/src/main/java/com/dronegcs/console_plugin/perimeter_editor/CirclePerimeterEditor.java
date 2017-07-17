package com.dronegcs.console_plugin.perimeter_editor;

import com.geo_tools.Coordinate;

/**
 * Created by taljmars on 3/27/17.
 */
public interface CirclePerimeterEditor {

    void setRadius(double radius) throws PerimeterUpdateException;

    void setCenter(Coordinate position);
}
