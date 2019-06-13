package com.dronegcs.console_plugin.perimeter_editor;

import com.db.persistence.scheme.BaseObject;
import com.db.persistence.scheme.QueryRequestRemote;
import com.db.persistence.scheme.QueryResponseRemote;
import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.scheme.Point;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.QuerySvcRemoteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by taljmars on 3/26/17.
 */
@Component
public class PerimetersManagerImpl implements PerimetersManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(PerimetersManagerImpl.class);

    @Autowired
    private QuerySvcRemoteWrapper querySvcRemote;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PerimeterEditorFactory perimeterEditorFactory;

    private Map<String, BaseObject> dbItems;
    private Map<String, BaseObject> dirtyDeleted;
    private Map<String, BaseObject> dirtyItems;

    public PerimetersManagerImpl() {
        dbItems = new HashMap<>();
        dirtyDeleted = new HashMap<>();
        dirtyItems = new HashMap<>();
    }

    @Override
    public <T extends PerimeterEditor> T openPerimeterEditor(String initialName, Class<? extends Perimeter> clz) {
        ClosablePerimeterEditor closablePerimeterEditor = perimeterEditorFactory.getEditor(clz);
        if (closablePerimeterEditor == null)
            throw new RuntimeException("Failed to find perimeter editor");

        closablePerimeterEditor.open(initialName);
        return (T) closablePerimeterEditor;
    }

    @Override
    public <T extends PerimeterEditor> T openPerimeterEditor(Perimeter perimeter) {
        LOGGER.debug("Setting new perimeter to perimeter editor");
        ClosablePerimeterEditor perimeterEditor = perimeterEditorFactory.getEditor(perimeter.getClass());
        perimeterEditor.open(perimeter);
        return (T) perimeterEditor;
    }

    @Override
    public List<BaseObject> getAllPerimeters() {
        QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(PolygonPerimeter.class.getCanonicalName());
        queryRequestRemote.setQuery("GetAllPolygonPerimeters");
        QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        List<BaseObject> polygonPerimeterList = queryResponseRemote.getResultList();

        queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(CirclePerimeter.class.getCanonicalName());
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
        queryRequestRemote.setClz(PolygonPerimeter.class.getCanonicalName());
        queryRequestRemote.setQuery("GetAllModifiedPolygonPerimeters");
        QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        List<BaseObject> polygonPerimeterList = queryResponseRemote.getResultList();

        queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(CirclePerimeter.class.getCanonicalName());
        queryRequestRemote.setQuery("GetAllModifiedCirclePerimeters");
        queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        List<BaseObject> circlePerimeterList = queryResponseRemote.getResultList();

        List<BaseObject> list = new ArrayList<>();
        list.addAll(polygonPerimeterList);
        list.addAll(circlePerimeterList);
        return list;
    }

    @Override
    public Perimeter getPerimeter(String missionUid) {
        Perimeter res = (Perimeter) dirtyItems.get(missionUid);
        if (res == null)
            res = (Perimeter) dbItems.get(missionUid);
        return res;
    }

    @Override
    public void updateItem(BaseObject object) {
        dirtyItems.put(object.getKeyId().getObjId(), object);
    }

    @Override
    public Point getPointItem(String itemUid) {
        Point res = (Point) dirtyItems.get(itemUid);
        if (res == null)
            res = (Point) dbItems.get(itemUid);
        return res;
    }

    @Override
    public void load(BaseObject item) {
        dbItems.put(item.getKeyId().getObjId(), item);
    }

    @Override
    public void removeItem(BaseObject object) {
        String key = object.getKeyId().getObjId();
        dirtyItems.remove(key);
        dbItems.remove(key);
        dirtyDeleted.put(key, object);
    }

    @Override
    public boolean isDirty(BaseObject item) {
        String key = item.getKeyId().getObjId();
        return dirtyItems.containsKey(key) || dirtyDeleted.containsKey(key);
    }

    @Override
    public Collection<ClosingPair<Perimeter>> flushAllItems(boolean isPublish) {
        List<ClosingPair<Perimeter>> res = new ArrayList<>();
        if (!isPublish){
            dbItems.clear();
            dirtyDeleted.clear();
            dirtyItems.clear();
            return res;
        }

        ObjectCrudSvcRemoteWrapper objectCrudSvcRemote = applicationContext.getBean(ObjectCrudSvcRemoteWrapper.class);
        try {
            for (BaseObject a : this.dirtyItems.values()) {
                BaseObject updatedObj = objectCrudSvcRemote.update(a);
                if (updatedObj instanceof  Perimeter) {
                    LOGGER.debug("Adding to Publish-update list: {}", updatedObj);
                    res.add(new ClosingPair<>((Perimeter) updatedObj, false));
                }
                dbItems.put(updatedObj.getKeyId().getObjId(), updatedObj);
            }
            this.dirtyItems.clear();
            for (BaseObject deletedObj : this.dirtyDeleted.values()) {
                objectCrudSvcRemote.delete(deletedObj);
                if (deletedObj instanceof  Perimeter) {
                    LOGGER.debug("Adding to Publish-delete list: {}", deletedObj);
                    res.add(new ClosingPair<>((Perimeter) deletedObj, false));
                }
                dbItems.remove(deletedObj.getKeyId().getObjId());
            }
            this.dirtyDeleted.clear();
        }
        catch (Exception e ) {
            System.out.println(e.getMessage());
        }
        return res;
    }


    @Override
    public List<Point> getPoints(Perimeter perimeter) {
        List<Point> res = new ArrayList<>();
//        PerimeterEditor perimeterEditor = openPerimeterEditor(perimeter);

        if (perimeter instanceof PolygonPerimeter) {
            for (String pointUid : ((PolygonPerimeter) perimeter).getPoints()) {
                res.add(getPointItem(pointUid));
            }
        }

        if (perimeter instanceof CirclePerimeter) {
            String pointUid  = ((CirclePerimeter) perimeter).getCenter();
            res.add(getPointItem(pointUid));
        }

        return res;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Perimeter Manager Status:\n");
//        for (ClosablePerimeterEditor closablePerimeterEditor : closablePerimeterEditorList) {
//            builder.append(closablePerimeterEditor);
//        }
        return builder.toString();
    }
}
