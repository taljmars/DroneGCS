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

    <T extends PerimeterEditor> T openPerimeterEditor(String name, Class<? extends Perimeter> clz);

    <T extends PerimeterEditor> T openPerimeterEditor(Perimeter perimeter);

    List<BaseObject> getAllPerimeters();

    List<BaseObject> getAllModifiedPerimeters();

    boolean isDirty(BaseObject item);

    Collection<ClosingPair<Perimeter>> flushAllItems(boolean isPublish);

    <P extends Perimeter> List<Point> getPoints(P perimeter);

    void removeItem(BaseObject point);

    Perimeter getPerimeter(String missionUid);

    void updateItem(BaseObject item);

    Point getPointItem(String child);

    void load(BaseObject item);

}
