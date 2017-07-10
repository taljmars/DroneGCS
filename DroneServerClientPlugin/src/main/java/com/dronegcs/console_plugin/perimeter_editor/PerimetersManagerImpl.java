package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.*;
import com.dronedb.persistence.ws.internal.DatabaseValidationRemoteException;
import com.dronedb.persistence.ws.internal.DroneDbCrudSvcRemote;
import com.dronedb.persistence.ws.internal.ObjectNotFoundException;
import com.dronedb.persistence.ws.internal.QuerySvcRemote;
import org.apache.log4j.Logger;
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

    private final static Logger logger = Logger.getLogger(PerimetersManagerImpl.class);

    @Autowired
    private QuerySvcRemote querySvcRemote;

    @Autowired
    private DroneDbCrudSvcRemote droneDbCrudSvcRemote;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PerimeterEditorFactory perimeterEditorFactory;

    List<ClosablePerimeterEditor> closablePerimeterEditorList;

    public PerimetersManagerImpl() {
        closablePerimeterEditorList = new ArrayList<>();
    }

    @Override
    public <T extends PerimeterEditor> T openPerimeterEditor(String name, Class<? extends Perimeter> clz) throws PerimeterUpdateException {
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
    public <T extends PerimeterEditor> Perimeter closePerimeterEditor(T perimeterEditor, boolean shouldSave) throws PerimeterUpdateException {
        logger.debug("closing mission editor");
        if (!(perimeterEditor instanceof ClosablePerimeterEditor)) {
            return null;
        }
        Perimeter perimeter = ((ClosablePerimeterEditor) perimeterEditor).close(shouldSave);
        closablePerimeterEditorList.remove(perimeterEditor);
        return perimeter;
    }

    @Override
    public List<BaseObject> getAllPerimeters() {
        QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(PolygonPerimeter.class.getName());
        queryRequestRemote.setQuery("GetAllPolygonPerimeters");
        QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        List<BaseObject> polygonPerimeterList = queryResponseRemote.getResultList();

        queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(CirclePerimeter.class.getName());
        queryRequestRemote.setQuery("GetAllCirclePerimeters");
        queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        List<BaseObject> circlePerimeterList = queryResponseRemote.getResultList();

        List<BaseObject> list = new ArrayList<>();
        list.addAll(polygonPerimeterList);
        list.addAll(circlePerimeterList);
        return list;
    }

    @Override
    public void delete(Perimeter perimeter) throws PerimeterUpdateException {
        try {
            Perimeter oldPerimeter = null;
            ClosablePerimeterEditor closablePerimeterEditor = findPerimeterEditorByPerimeter(perimeter);
            if (closablePerimeterEditor == null)
                return;

            oldPerimeter = closablePerimeterEditor.close(false);
            if (oldPerimeter != null) {
                droneDbCrudSvcRemote.delete(oldPerimeter);
            }
        } catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public Perimeter update(Perimeter perimeter) throws PerimeterUpdateException {
        try {
            ClosablePerimeterEditor closablePerimeterEditor = findPerimeterEditorByPerimeter(perimeter);
            if (closablePerimeterEditor == null)
                return (Perimeter) droneDbCrudSvcRemote.update(perimeter);

            return closablePerimeterEditor.update(perimeter);

        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public List<Point> getPoints(Perimeter perimeter) {

        List<Point> res = new ArrayList<>();

        if (perimeter instanceof PolygonPerimeter) {
            List<String> uuidList = ((PolygonPerimeter) perimeter).getPoints();
            for (String uuid : uuidList)
                try {
                    res.add((Point) droneDbCrudSvcRemote.readByClass(uuid.toString(), Point.class.getName()));
                } catch (ObjectNotFoundException e) {
                    e.printStackTrace();
                    //TODO
                }
            return res;
        }

        if (perimeter instanceof CirclePerimeter) {
            String uuid = ((CirclePerimeter) perimeter).getCenter();
            try {
                res.add((Point) droneDbCrudSvcRemote.readByClass(uuid.toString(), Point.class.getName()));
            } catch (ObjectNotFoundException e) {
                e.printStackTrace();
                //TODO
            }
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
