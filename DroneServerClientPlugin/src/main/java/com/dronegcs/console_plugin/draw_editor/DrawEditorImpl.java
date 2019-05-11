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

    private Map<String, Shape> objectInLayer = new HashMap<>();
    private boolean markDeleted = false;

    @Override
    public Layer open(Layer layer) throws DrawUpdateException {
        LOGGER.debug("Setting new layer to layer editor");
        this.layer = layer;
        return this.layer;
    }

    @Override
    public Layer open(String layerName) throws DrawUpdateException {
        LOGGER.debug("Setting new layer to layer editor");
        try {
            this.layer = objectCrudSvcRemote.create(Layer.class.getCanonicalName());
            this.layer.setName(layerName);
//            this.layer = objectCrudSvcRemote.update(this.layer);
            return this.layer;
        }
        catch (ObjectInstanceRemoteException e) {
            throw new DrawUpdateException(e.getMessage());
        }
//        catch (DatabaseValidationRemoteException e) {
//            throw new DrawUpdateException(e.getMessage());
//        }
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

    @Override
    public ClosingPair<Layer> close(boolean shouldSave) {
        LOGGER.debug("Close, should save:" + shouldSave);
        ClosingPair<Layer> layerClosingPair = null;
        Layer res = this.layer;
        if (!shouldSave) {
            LOGGER.debug(String.format("Delete layer %s %s", res.getKeyId().getObjId(), res.getName()));
//            objectCrudSvcRemote.delete(mission);
            objectInLayer.clear();
            markDeleted = false;
            try {
                res = objectCrudSvcRemote.readByClass(layer.getKeyId().getObjId(), Layer.class.getCanonicalName());
                LOGGER.debug("Found original layer " + res.getKeyId().getObjId() + " " + res.getName());
                layerClosingPair = new ClosingPair(res, false);
            }
            catch (ObjectNotFoundRemoteException e) {
                LOGGER.error("Layer doesn't exist");
                layerClosingPair = new ClosingPair(this.layer, true);
            }
        }
        else {
            try {
                if (markDeleted) {
                    res = objectCrudSvcRemote.delete(this.layer);
                }
                else {
                    this.layer.getObjectsUids().forEach(uid -> {
                        try {
                            objectCrudSvcRemote.update(objectInLayer.get(uid));
                        } catch (DatabaseValidationRemoteException e) {
                            e.printStackTrace();
                        } catch (ObjectInstanceRemoteException e) {
                            e.printStackTrace();
                        }
                    });
                    objectInLayer.clear();
                    res = objectCrudSvcRemote.update(this.layer);
                }
            }
            catch (DatabaseValidationRemoteException | ObjectNotFoundRemoteException | ObjectInstanceRemoteException e) {
                e.printStackTrace();
            }
            layerClosingPair = new ClosingPair(res, false);
        }
        //System.err.println(String.format("Before resetting %s %s", res.getKeyId().getObjId(), res.getName()));
        this.layer = null;
        LOGGER.debug("Layer editor finished");
        //System.err.println(String.format("After resetting %s %s", res.getKeyId().getObjId(), res.getName()));
        return layerClosingPair;
    }

    @Override
    public Shape createMarker() throws DrawUpdateException {
        try {
            Shape marker = (Shape) objectCrudSvcRemote.create(Shape.class.getCanonicalName());
            objectInLayer.put(marker.getKeyId().getObjId(), marker);
            return marker;
        } catch (ObjectInstanceRemoteException e) {
            throw new DrawUpdateException(e.getMessage());
        }
    }

    @Override
    public Shape addMarker(Coordinate position) throws DrawUpdateException {
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
    public Layer update(Layer layer) throws DrawUpdateException {
        try {
            LOGGER.debug("Current layer named '{}' have '{}' items", this.layer.getName(), this.layer.getObjectsUids().size());
            LOGGER.debug("After update, layer will be named '{}' with '{}' items", layer.getName(), layer.getObjectsUids().size());
//            this.layer = (Layer) objectCrudSvcRemote.update(layer);
            this.layer = layer;
            LOGGER.debug("Updated layer name is '{}' with '{}' items", this.layer.getName(), this.layer.getObjectsUids().size());
            return this.layer;
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new DrawUpdateException(e.getMessage());
        }
    }

    @Override
    public <T extends Shape> void removeItem(T item) throws DrawUpdateException {
        layer.getObjectsUids().remove(item.getKeyId().getObjId());
//        try {
//            layer = objectCrudSvcRemote.update(layer);
//        }
//        catch (DatabaseValidationRemoteException e) {
//            throw new DrawUpdateException(e.getMessage());
//        } catch (ObjectInstanceRemoteException e) {
//            throw new DrawUpdateException(e.getMessage());
//        }
    }

    @Override
    public List<Shape> getLayerItems() {
        List<Shape> layerItemsList = new ArrayList<>();
        List<String> uuidList = layer.getObjectsUids();
        uuidList.forEach((String uuid) -> {
            try {
                Shape shape = objectInLayer.get(uuid);
                if (shape == null)
                    shape = (Shape) objectCrudSvcRemote.readByClass(uuid, Shape.class.getCanonicalName());

                layerItemsList.add(shape);
            }
            catch (ObjectNotFoundRemoteException e) {
                LOGGER.error("Failed to get layer items", e);
            }
        });
        return layerItemsList;
    }

    @Override
    public Layer delete() throws DrawUpdateException {
//        try {
//            this.layer = objectCrudSvcRemote.delete(layer);
            this.markDeleted = true;
            return this.layer;
//        }
//        catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException | ObjectNotFoundRemoteException e) {
//            throw new DrawUpdateException(e.getMessage());
//        }
    }

    @Override
    public Layer setDrawLayerName(String name) throws DrawUpdateException {
        try {
            this.layer.setName(name);
//            this.layer = objectCrudSvcRemote.update(layer);
            return this.layer;
        }
        catch (Exception e) {
            throw new DrawUpdateException(e.getMessage());
        }

    }

    @Override
    public <T extends Shape> T updateItem(T item) throws DrawUpdateException {
        // Update Item
        T res = null;
//        try {
//            res = (T) objectCrudSvcRemote.update(item);
        res = item;
            if (!layer.getObjectsUids().contains(res.getKeyId().getObjId())) {
                LOGGER.debug("LayerItem {} is not part of the layer, adding it", res.getKeyId().getObjId());
                layer.getObjectsUids().add(res.getKeyId().getObjId());
                LOGGER.debug("Layer items amount is now {} ", layer.getObjectsUids().size());
                objectInLayer.replace(item.getKeyId().getObjId(), item);
            }
            else {
                objectInLayer.put(item.getKeyId().getObjId(), item);
            }
            // Update Mission
//            layer = (Layer) objectCrudSvcRemote.update(layer);
            return res;
//        }
//        catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException e) {
//            throw new DrawUpdateException(e.getMessage());
//        }
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
