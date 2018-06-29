package com.dronegcs.console_plugin.perimeter_editor;

import com.db.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.scheme.Point;
import com.dronegcs.console_plugin.ClosingPair;

import java.util.Collection;
import java.util.List;

/**
 * Created by taljmars on 3/26/17.
 */
public interface PerimetersManager {

    <T extends PerimeterEditor> T openPerimeterEditor(String name, Class<? extends Perimeter> clz) throws PerimeterUpdateException;

    <T extends PerimeterEditor> T openPerimeterEditor(Perimeter perimeter) throws PerimeterUpdateException;

    <T extends PerimeterEditor, P extends Perimeter> T getPerimeterEditor(P perimeter);

    <T extends PerimeterEditor, P extends Perimeter> ClosingPair<P> closePerimeterEditor(T perimeterEditor, boolean shouldSave) throws PerimeterUpdateException;

    List<BaseObject> getAllPerimeters();

    <P extends Perimeter> void delete(P perimeter) throws PerimeterUpdateException;

    <P extends Perimeter> P update(P perimeter) throws PerimeterUpdateException;

    List<BaseObject> getAllModifiedPerimeters();

    <P extends Perimeter> List<Point> getPoints(P perimeter);

    <P extends Perimeter> P clonePerimeter(P perimeter) throws PerimeterUpdateException;

    <P extends Perimeter> Collection<ClosingPair<P>> closeAllPerimeterEditors(boolean shouldSave);

    int loadEditors();
}
