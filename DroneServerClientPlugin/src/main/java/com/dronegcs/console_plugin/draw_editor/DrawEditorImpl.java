package com.dronegcs.console_plugin.draw_editor;

import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.Shape;
import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.geo_tools.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Created by taljmars on 3/25/17.
 */
@Scope(scopeName = "prototype")
@Component
public class DrawEditorImpl implements ClosableDrawEditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(DrawEditorImpl.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
    private ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

    private Layer layer;

    @Autowired
    DrawManagerImpl drawManager;

    @Override
    public Layer open(Layer layer) {
        LOGGER.debug("Setting new layer to layer editor");
        this.layer = layer;
        drawManager.updateItem(this.layer);
        return layer;
    }

    @Override
    public Layer open(String layerName) {
        LOGGER.debug("Setting new layer to layer editor");
        this.layer = new Layer();
        this.layer.getKeyId().setObjId("DUMMY" + UUID.randomUUID().toString());
        this.layer.setName(layerName);
        this.layer.setObjectsUids(new ArrayList<>());
        drawManager.updateItem(this.layer);
        return this.layer;

    }

    @Override
    public boolean equals(Object o) {
        System.out.println("In equals");
        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        DrawEditorImpl that = (DrawEditorImpl) o;
        Layer that = (Layer) o;
        return Objects.equals(layer.getKeyId().getObjId(), that.getKeyId().getObjId());
    }

    @Override
    public int hashCode() {
        System.out.println("In Hashcode");
        return Objects.hash(layer.getKeyId().getObjId());
    }

//    @Override
//    public ClosingPair<Layer> close(boolean shouldSave) {
//        LOGGER.debug("Close, should save:" + shouldSave);
//        ClosingPair<Layer> layerClosingPair = null;
//        Layer res = this.layer;
//        if (!shouldSave) {
//            LOGGER.debug(String.format("Delete layer %s %s", res.getKeyId().getObjId(), res.getName()));
////            objectCrudSvcRemote.delete(mission);
//            objectInLayer.clear();
//            markDeleted = false;
//            try {
//                res = objectCrudSvcRemote.readByClass(layer.getKeyId().getObjId(), Layer.class.getCanonicalName());
//                LOGGER.debug("Found original layer " + res.getKeyId().getObjId() + " " + res.getName());
//                layerClosingPair = new ClosingPair(res, false);
//            }
//            catch (ObjectNotFoundRemoteException e) {
//                LOGGER.error("Layer doesn't exist");
//                layerClosingPair = new ClosingPair(this.layer, true);
//            }
//        }
//        else {
//            try {
//                if (markDeleted) {
//                    res = objectCrudSvcRemote.delete(this.layer);
//                }
//                else {
//                    this.layer.getObjectsUids().forEach(uid -> {
//                        try {
//                            objectCrudSvcRemote.update(objectInLayer.get(uid));
//                        } catch (DatabaseValidationRemoteException e) {
//                            e.printStackTrace();
//                        } catch (ObjectInstanceRemoteException e) {
//                            e.printStackTrace();
//                        }
//                    });
//                    objectInLayer.clear();
//                    res = objectCrudSvcRemote.update(this.layer);
//                }
//            }
//            catch (DatabaseValidationRemoteException | ObjectNotFoundRemoteException | ObjectInstanceRemoteException e) {
//                e.printStackTrace();
//            }
//            layerClosingPair = new ClosingPair(res, false);
//        }
//        //System.err.println(String.format("Before resetting %s %s", res.getKeyId().getObjId(), res.getName()));
//        this.layer = null;
//        LOGGER.debug("Layer editor finished");
//        //System.err.println(String.format("After resetting %s %s", res.getKeyId().getObjId(), res.getName()));
//        return layerClosingPair;
//    }

    @Override
    public Shape createMarker() {
        Shape marker = new Shape();
        marker.getKeyId().setObjId("DUMMY" + UUID.randomUUID().toString());
        drawManager.updateItem(marker);
        return marker;
    }

    @Override
    public Shape addMarker(Coordinate position) {
        Shape shape = createMarker();
        Coordinate c3 = new Coordinate(position);
//        shape.setLat(c3.getLat());
//        shape.setLon(c3.getLon());
        shape.setLat(c3.getLon());
        shape.setLon(c3.getLat());
        return updateItem(shape);
    }

    @Override
    public Layer getModifiedLayer() {
        return this.layer;
    }

    @Override
    public Layer update(Layer layer)  {
        LOGGER.debug("Current layer named '{}' have '{}' items", this.layer.getName(), this.layer.getObjectsUids().size());
        LOGGER.debug("After update, layer will be named '{}' with '{}' items", layer.getName(), layer.getObjectsUids().size());
        this.layer = layer;
        LOGGER.debug("Updated layer name is '{}' with '{}' items", this.layer.getName(), this.layer.getObjectsUids().size());
        return this.layer;
    }

    @Override
    public <T extends Shape> void removeItem(T item) {
        layer.getObjectsUids().remove(item.getKeyId().getObjId());
        String key = item.getKeyId().getObjId();
        layer.getObjectsUids().remove(key);
        drawManager.removeItem(item);
    }

    @Override
    public List<Shape> getLayerItems() {
        List<Shape> missionItemList = new ArrayList<>();
        List<String> uuidList = layer.getObjectsUids();
        uuidList.forEach((String uuid) -> {
            Shape mItem = drawManager.getLayerItems(uuid);
            missionItemList.add(mItem);
        });
        return missionItemList;
    }

    @Override
    public void deleteLayer() {
        for (String child : this.layer.getObjectsUids()) {
            Shape obj = drawManager.getLayerItems(child);
            drawManager.removeItem(obj);
        }
        drawManager.removeItem(layer);
    }

    @Override
    public Layer setDrawLayerName(String name)  {
        this.layer.setName(name);
        return this.layer;
    }

    @Override
    public <T extends Shape> T updateItem(T item) {
        if (!layer.getObjectsUids().contains(item.getKeyId().getObjId())) {
            LOGGER.debug("LayerItem {} is not part of the layer, adding it", item.getKeyId().getObjId());
            layer.getObjectsUids().add(item.getKeyId().getObjId());
            LOGGER.debug("Layer items amount is now {} ", layer.getObjectsUids().size());
        }
        drawManager.updateItem(item);
        return item;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Draw Layer Editor: ");
        builder.append(layer.getKeyId().getObjId() + " ");
        builder.append(layer.getName());
        return builder.toString();
    }
}
