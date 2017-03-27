package com.dronegcs.console.perimeter_editor;

import com.dronedb.persistence.scheme.perimeter.Perimeter;
import com.dronedb.persistence.scheme.perimeter.Point;

import java.util.List;

/**
 * Created by oem on 3/26/17.
 */
public interface PerimetersManager {

    <T extends PerimeterEditor> T openPerimeterEditor(String name, Class<? extends Perimeter> clz);

    <T extends PerimeterEditor> T getPerimeterEditor(Perimeter perimeter);

    <T extends PerimeterEditor> Perimeter closePerimeterEditor(T perimeterEditor, boolean shouldSave);

    List<Perimeter> getAllPerimeters();

    void delete(Perimeter perimeter);

    Perimeter update(Perimeter perimeter);

    List<Point> getPoints(Perimeter perimeter);
}
