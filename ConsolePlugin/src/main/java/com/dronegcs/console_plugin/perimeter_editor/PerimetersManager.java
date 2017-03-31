package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.scheme.Point;

import java.util.List;

/**
 * Created by taljmars on 3/26/17.
 */
public interface PerimetersManager {

    <T extends PerimeterEditor> T openPerimeterEditor(String name, Class<? extends Perimeter> clz);

    <T extends PerimeterEditor> T getPerimeterEditor(Perimeter perimeter);

    <T extends PerimeterEditor> Perimeter closePerimeterEditor(T perimeterEditor, boolean shouldSave);

    List<BaseObject> getAllPerimeters();

    void delete(Perimeter perimeter);

    Perimeter update(Perimeter perimeter);

    List<Point> getPoints(Perimeter perimeter);
}
