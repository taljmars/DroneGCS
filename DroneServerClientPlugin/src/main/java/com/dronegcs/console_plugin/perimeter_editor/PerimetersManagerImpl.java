package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.*;
import com.dronedb.persistence.ws.internal.*;
import com.dronedb.persistence.ws.internal.DatabaseValidationRemoteException;
import com.dronedb.persistence.ws.internal.ObjectNotFoundException;
import com.dronedb.persistence.ws.internal.ObjectNotFoundRemoteException;
import com.dronegcs.console_plugin.ClosingPair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.*;

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

    @Autowired  @NotNull(message = "Internal Error: Failed to get perimeter object crud")
    private PerimeterCrudSvcRemote perimeterCrudSvcRemote;

    List<ClosablePerimeterEditor> closablePerimeterEditorList;

    public PerimetersManagerImpl() {
        closablePerimeterEditorList = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        List<BaseObject> perimetersList = getAllModifiedPerimeters();
        for (BaseObject item : perimetersList) {
            Perimeter perimeter = (Perimeter) item;
            try {
                logger.debug("perimeter '" + perimeter.getName() + "' is in edit mode");
                openPerimeterEditor(perimeter);
            } catch (PerimeterUpdateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public <T extends PerimeterEditor> T openPerimeterEditor(String initialName, Class<? extends Perimeter> clz) throws PerimeterUpdateException {
        ClosablePerimeterEditor closablePerimeterEditor = perimeterEditorFactory.getEditor(clz);
        if (closablePerimeterEditor == null)
            throw new RuntimeException("Failed to find perimeter editor");

        closablePerimeterEditor.open(initialName);
        closablePerimeterEditorList.add(closablePerimeterEditor);
        return (T) closablePerimeterEditor;
    }

    @Override
    public <T extends PerimeterEditor> T openPerimeterEditor(Perimeter perimeter) throws PerimeterUpdateException {
        logger.debug("Setting new perimeter to perimeter editor");
        ClosablePerimeterEditor perimeterEditor = findPerimeterEditorByPerimeter(perimeter);
        if (perimeterEditor == null) {
            System.err.println("Editor not exist for perimeter " + perimeter.getName() + ", creating new one");
            PerimeterEditorFactory perimeterEditorFactory = applicationContext.getBean(PerimeterEditorFactory.class);
            perimeterEditor = perimeterEditorFactory.getEditor(perimeter.getClass());
            perimeterEditor.open(perimeter);
            closablePerimeterEditorList.add(perimeterEditor);
        }
        else {
            System.err.println("Found existing perimeter editor");
        }
        return (T) perimeterEditor;
    }

    @Override
    public <P extends Perimeter> void delete(P perimeter) throws PerimeterUpdateException {
        if (perimeter == null) {
            logger.error("Received Empty perimeter, skip deletion");
            return;
        }
        try {
            ClosablePerimeterEditor closablePerimeterEditor = openPerimeterEditor(perimeter);
            closablePerimeterEditor.delete();
        } catch (PerimeterUpdateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <P extends Perimeter> P update(P perimeter) throws PerimeterUpdateException {
        try {
            ClosablePerimeterEditor closablePerimeterEditor = findPerimeterEditorByPerimeter(perimeter);
            if (closablePerimeterEditor == null)
                return (P) droneDbCrudSvcRemote.update(perimeter);

            return (P) closablePerimeterEditor.update(perimeter);

        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public <P extends Perimeter> P clonePerimeter(P perimeter) throws PerimeterUpdateException {
        try {
            return (P) perimeterCrudSvcRemote.clonePerimeter(perimeter);
        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
        catch (ObjectNotFoundRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public <T extends PerimeterEditor, P extends Perimeter> ClosingPair<P> closePerimeterEditor(T perimeterEditor, boolean shouldSave) throws PerimeterUpdateException {
        logger.debug("closing mission editor");
        if (!(perimeterEditor instanceof ClosablePerimeterEditor)) {
            return null;
        }
        ClosingPair<P> perimeterClosingPair = ((ClosablePerimeterEditor) perimeterEditor).close(shouldSave);
        closablePerimeterEditorList.remove(perimeterEditor);
        return perimeterClosingPair;
    }

    @Override
    public <P extends Perimeter> Collection<ClosingPair<P>> closeAllPerimeterEditors(boolean shouldSave) throws PerimeterUpdateException {
        Collection<ClosingPair<P>> closedPerimeters = new ArrayList<>();
        Iterator<ClosablePerimeterEditor> it = closablePerimeterEditorList.iterator();
        while (it.hasNext()) {
            closedPerimeters.add(it.next().close(shouldSave));
        }
        closablePerimeterEditorList.clear();
        return closedPerimeters;
    }

    @Override
    public <T extends PerimeterEditor, P extends Perimeter> T getPerimeterEditor(P perimeter) {
        return (T) findPerimeterEditorByPerimeter(perimeter);
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
    public List<BaseObject> getAllModifiedPerimeters() {
        QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(PolygonPerimeter.class.getName());
        queryRequestRemote.setQuery("GetAllModifiedPolygonPerimeters");
        QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        List<BaseObject> polygonPerimeterList = queryResponseRemote.getResultList();

        queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(CirclePerimeter.class.getName());
        queryRequestRemote.setQuery("GetAllModifiedCirclePerimeters");
        queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        List<BaseObject> circlePerimeterList = queryResponseRemote.getResultList();

        List<BaseObject> list = new ArrayList<>();
        list.addAll(polygonPerimeterList);
        list.addAll(circlePerimeterList);
        return list;
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
            if (uuid != null && !uuid.isEmpty()) {
                try {
                    res.add((Point) droneDbCrudSvcRemote.readByClass(uuid.toString(), Point.class.getName()));
                } catch (ObjectNotFoundException e) {
                    e.printStackTrace();
                    //TODO
                }
            }
            return res;
        }

        return null;
    }

    private <P extends Perimeter> ClosablePerimeterEditor findPerimeterEditorByPerimeter(P perimeter) {
        for (ClosablePerimeterEditor closablePerimeterEditor : closablePerimeterEditorList) {
            if (perimeter.equals(closablePerimeterEditor.getModifiedPerimeter())) {
                return closablePerimeterEditor;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Perimeter Manager Status:\n");
        for (ClosablePerimeterEditor closablePerimeterEditor : closablePerimeterEditorList) {
            builder.append(closablePerimeterEditor);
        }
        return builder.toString();
    }
}
