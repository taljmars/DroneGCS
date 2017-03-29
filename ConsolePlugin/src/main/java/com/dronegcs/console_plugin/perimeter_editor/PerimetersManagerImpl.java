package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.apis.DroneDbCrudSvcRemote;
import com.dronedb.persistence.scheme.apis.QueryRequestRemote;
import com.dronedb.persistence.scheme.apis.QueryResponseRemote;
import com.dronedb.persistence.scheme.apis.QuerySvcRemote;
import com.dronedb.persistence.scheme.perimeter.CirclePerimeter;
import com.dronedb.persistence.scheme.perimeter.Perimeter;
import com.dronedb.persistence.scheme.perimeter.Point;
import com.dronedb.persistence.scheme.perimeter.PolygonPerimeter;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by taljmars on 3/26/17.
 */
@Component
public class PerimetersManagerImpl implements PerimetersManager {

    @Autowired
    private QuerySvcRemote querySvcRemote;

    @Autowired
    private DroneDbCrudSvcRemote droneDbCrudSvcRemote;

    @Autowired
    private LoggerDisplayerSvc loggerDisplayerSvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PerimeterEditorFactory perimeterEditorFactory;

    List<ClosablePerimeterEditor> closablePerimeterEditorList;

    public PerimetersManagerImpl() {
        closablePerimeterEditorList = new ArrayList<>();
    }

    @Override
    public <T extends PerimeterEditor> T openPerimeterEditor(String name, Class<? extends Perimeter> clz) {
        ClosablePerimeterEditor closablePerimeterEditor = perimeterEditorFactory.getEditor(clz);
        if (closablePerimeterEditor == null)
            throw new RuntimeException("Failed to find perimeter editor");

        closablePerimeterEditor.open(name);
        closablePerimeterEditorList.add(closablePerimeterEditor);
        return (T) closablePerimeterEditor;
    }

    @Override
    public <T extends PerimeterEditor> T getPerimeterEditor(Perimeter perimeter) {
        return (T) findPerimeterEditorByPerimeter(perimeter);
    }

    @Override
    public <T extends PerimeterEditor> Perimeter closePerimeterEditor(T perimeterEditor, boolean shouldSave) {
        loggerDisplayerSvc.logGeneral("closing mission editor");
        if (!(perimeterEditor instanceof ClosablePerimeterEditor)) {
            return null;
        }
        Perimeter perimeter = ((ClosablePerimeterEditor) perimeterEditor).close(shouldSave);
        closablePerimeterEditorList.remove(perimeterEditor);
        return perimeter;
    }

    @Override
    public List<Perimeter> getAllPerimeters() {
        QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(PolygonPerimeter.class);
        queryRequestRemote.setQuery("GetAllPolygonPerimeters");
        QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        List<BaseObject> polygonPerimeterList = queryResponseRemote.getResultList();

        queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(CirclePerimeter.class);
        queryRequestRemote.setQuery("GetAllCirclePerimeters");
        queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        List<BaseObject> circlePerimeterList = queryResponseRemote.getResultList();

        List<Perimeter> list = new ArrayList<>();
//        list.addAll((Collection<? extends Perimeter>) polygonPerimeterList);
//        list.addAll((Collection<? extends Perimeter>) circlePerimeterList);
        return list;
    }

    @Override
    public void delete(Perimeter perimeter) {
        Perimeter oldPerimeter = null;
        ClosablePerimeterEditor closablePerimeterEditor = findPerimeterEditorByPerimeter(perimeter);
        if (closablePerimeterEditor == null)
            return;

        oldPerimeter = closablePerimeterEditor.close(false);
        if (oldPerimeter != null) {
            droneDbCrudSvcRemote.delete(oldPerimeter);
        }
    }

    @Override
    public Perimeter update(Perimeter perimeter) {
        ClosablePerimeterEditor closablePerimeterEditor = findPerimeterEditorByPerimeter(perimeter);
        if (closablePerimeterEditor == null)
            return droneDbCrudSvcRemote.update(perimeter);

        return closablePerimeterEditor.update(perimeter);
    }

    @Override
    public List<Point> getPoints(Perimeter perimeter) {

        List<Point> res = new ArrayList<>();

        if (perimeter instanceof PolygonPerimeter) {
            List<UUID> uuidList = ((PolygonPerimeter) perimeter).getPoints();
            for (UUID uuid : uuidList)
                res.add(droneDbCrudSvcRemote.readByClass(uuid, Point.class));
            return res;
        }

        if (perimeter instanceof CirclePerimeter) {
            UUID uuid = ((CirclePerimeter) perimeter).getCenter();
            res.add(droneDbCrudSvcRemote.readByClass(uuid, Point.class));
            return res;
        }

        return null;
    }

    private ClosablePerimeterEditor findPerimeterEditorByPerimeter(Perimeter perimeter) {
        for (ClosablePerimeterEditor closablePerimeterEditor : closablePerimeterEditorList) {
            if (perimeter.equals(closablePerimeterEditor.getModifiedPerimeter())) {
                return closablePerimeterEditor;
            }
        }
        return null;
    }
}
