package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;


import com.db.gui.persistence.scheme.BaseLayer;
import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.LayersGroup;
import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.db.persistence.scheme.QueryRequestRemote;
import com.db.persistence.scheme.QueryResponseRemote;
import com.dronegcs.console_plugin.draw_editor.DrawUpdateException;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.QuerySvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.SessionsSvcRemoteWrapper;
import com.gui.core.layers.AbstractLayer;
import com.gui.core.layers.LayerGroup;
import com.gui.core.layers.LayerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by taljmars on 3/25/17.
 */
@Component
public class LayerManagerDbWrapper extends LayerManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(LayerManagerDbWrapper.class);

    private LayerGroupEditable rootTemplate;

    @Autowired
    private QuerySvcRemoteWrapper querySvcRemote;

    @Autowired
    private ObjectCrudSvcRemoteWrapper objectCrudSvc;

    @Autowired
    private SessionsSvcRemoteWrapper sessionsSvcRemoteWrapper;

    private Map<String, AbstractLayer> dataBaseUUID_layerGroups_Map;
    private Map<String, String> guiUUID_dataBaseUUID_Map;
    private boolean synced = false;

    private Map<Class, Set<GuiLayer_From_DatabaseLayer_Loader>> guiLayerLoaderMap;
    private Map<Class, DatabaseLayer_From_GuiLayer_Loader> dbLayerLoaderMap;

    @PostConstruct
    private void init() {
        dataBaseUUID_layerGroups_Map = new HashMap<>();
        rootTemplate = new LayerGroupEditable("Layers");
        rootTemplate.addChildren(new LayerGroupEditable("Missions"));
        rootTemplate.addChildren(new LayerGroupEditable("Perimeters"));
        rootTemplate.addChildren(new LayerGroupEditable("General"));

        guiUUID_dataBaseUUID_Map = new HashMap<>();

    }

    private void sync() {
        LOGGER.debug("Syncing Layers");
        List<BaseObject> allLayers = getAllLayers();
        if (allLayers.isEmpty()) {
            LOGGER.debug("There are no layers exist for this user");
            loadTemplate();
        }

        allLayers = getAllLayers();
        List<BaseObject> allModifiedLayers = getAllModifiedLayers();
        loadExistingData(allLayers, allModifiedLayers);

        synced = true;
    }

    private void loadTemplate() {
        try {
            LOGGER.debug("Loading Template");
            LOGGER.debug("Creating Layer group named '" + rootTemplate.getName() + "' from template");
            LayersGroup rootLayersGroupDB = objectCrudSvc.create(LayersGroup.class.getCanonicalName());
            rootLayersGroupDB.setRoot(true);
            rootLayersGroupDB.setName(rootTemplate.getName());
            loadTemplate(rootTemplate, rootLayersGroupDB);

            LOGGER.debug("Template was generated for user - publish it for constant use");
            sessionsSvcRemoteWrapper.publish();
        }
        catch (ObjectInstanceRemoteException e) {
            LOGGER.error("Failed to create root instance", e);
            e.printStackTrace();
        }
    }

    private BaseLayer loadTemplate(LayerGroupEditable layersGroupGui, BaseLayer layersGroupDB) {
        try {
            List<String> children = new ArrayList<>();
            for (AbstractLayer layer : layersGroupGui.getChildren()) {
                if ( ! (layer instanceof LayerGroup)) {
                    LOGGER.error("Unexpected type '{}'", layer);
                    System.exit(-4);
                }
                LayerGroupEditable layerGroupGui = (LayerGroupEditable) layer;

                LOGGER.debug("Creating Layer group named '" + layerGroupGui.getName() + "' from template");
                BaseLayer layersGroupDb = objectCrudSvc.create(LayersGroup.class.getCanonicalName());
                layersGroupDb.setName(layerGroupGui.getName());
                layersGroupDb = loadTemplate((LayerGroupEditable) layer, layersGroupDb);

                layersGroupDb = objectCrudSvc.update(layersGroupDb);
                children.add(layersGroupDb.getKeyId().getObjId());
            }
            if (layersGroupDB instanceof LayersGroup)
                ((LayersGroup) layersGroupDB).setLayersUids(children);
            else {
                LOGGER.error("Unexpected type {}", layersGroupDB);
                System.exit(-3);
            }

            layersGroupDB = objectCrudSvc.update(layersGroupDB);
            layersGroupGui.setPayload(layersGroupDB);
            return layersGroupDB;
        }
        catch (ObjectInstanceRemoteException | DatabaseValidationRemoteException e) {
            LOGGER.error("Failed to create instance", e);
            System.exit(-1);
        }
        return null;
    }

    private void loadExistingData(List<BaseObject> allLayers, List<BaseObject> allModifiedLayers) {
        try {
            Iterator<BaseObject> layersGroupIterator = allLayers.iterator();
            Map<String, AbstractLayer> orphanLayer = new HashMap<>();

            while (layersGroupIterator.hasNext()) {
                BaseObject baseObject = layersGroupIterator.next();
                String uuid = baseObject.getKeyId().getObjId();
                Class clz = baseObject.getClz();
                AbstractLayer layerGui = null;
                for (GuiLayer_From_DatabaseLayer_Loader a : guiLayerLoaderMap.get(clz)) {
                    if (a.isRelevant(baseObject)) {
                        layerGui = a.load(baseObject);
                        if (allModifiedLayers.contains(baseObject)) {
                            allModifiedLayers.remove(baseObject);
                            LOGGER.debug("Founding layer that exist in private DB: {}", layerGui);
                            layerGui.setWasEdited(true);
                        }
                        break;
                    }
                }
                if (LOGGER.isDebugEnabled()) {
                    if (layerGui == null) {
                        Layer l = ((Layer)baseObject);
                        LOGGER.error("Layer name: " + l.getName()  + "Objects: " + l.getObjectsUids());
                    }
                }

                Assert.isTrue(layerGui != null, "Failed to find appropriate gui layer for " + baseObject);
                dataBaseUUID_layerGroups_Map.put(uuid, layerGui);
                guiUUID_dataBaseUUID_Map.put(layerGui.getUuid().toString(), uuid);
                orphanLayer.put(uuid, layerGui);
            }

//        Assert.isTrue(!rootUuid.isEmpty(), "Root not found");

            layersGroupIterator = allLayers.iterator();
            while (layersGroupIterator.hasNext()) {
                BaseObject baseObject = layersGroupIterator.next();
                String uuid = baseObject.getKeyId().getObjId();
                if (baseObject instanceof LayersGroup) {
                    List<String> children = ((LayersGroup) baseObject).getLayersUids();
                    for (String childUuid : children) {
                        if (orphanLayer.containsKey(childUuid)) {
                            AbstractLayer layerGroupGui = orphanLayer.remove(childUuid);
                            ((LayerGroupEditable) dataBaseUUID_layerGroups_Map.get(uuid)).addChildren(layerGroupGui);
                        }
                    }
                }
                else if (baseObject instanceof com.db.gui.persistence.scheme.Layer) {
                    // Nothing to do for layers
                }
            }

            // Validate we've left with root only
            if (orphanLayer.size() != 1) {
                orphanLayer.forEach((uuid, layer) -> LOGGER.debug(uuid + " -> " + layer));
                Assert.isTrue(orphanLayer.size() == 1, "Unexpected amount of layers (" + orphanLayer.size() + ")");
            }
            String lastLayerUuid = orphanLayer.keySet().iterator().next();
//        Assert.isTrue(lastLayerUuid.equals(rootUuid), "More than one root");

            AbstractLayer layerGroupGui = orphanLayer.remove(lastLayerUuid);
            setRoot((LayerGroupEditable) layerGroupGui);
        }
        catch (Exception e) {
            LOGGER.error("Failed to load data", e);
        }
    }

    private AbstractLayer loadGuiLayer(Object baseObject) {
        throw new RuntimeException("Unexpected type of Layer -> " + baseObject);
    }

    private List<BaseObject> getAllLayers() {
        List<BaseObject> layersGroupList = new ArrayList();
        QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
        QueryResponseRemote queryResponseRemote;

        queryRequestRemote.setClz(com.db.gui.persistence.scheme.LayersGroup.class.getCanonicalName());
        queryRequestRemote.setQuery("GetAllLayersGroup");
        queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        layersGroupList.addAll(queryResponseRemote.getResultList());
        LOGGER.debug("There are currently {} layers Group in total", layersGroupList .size());

        queryRequestRemote.setClz(com.db.gui.persistence.scheme.Layer.class.getCanonicalName());
        queryRequestRemote.setQuery("GetAllLayers");
        queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        layersGroupList.addAll(queryResponseRemote.getResultList());
        LOGGER.debug("There are currently {} layers Group in total", layersGroupList .size());

        return layersGroupList;
    }

    public List<BaseObject> getAllModifiedLayers() {
        QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(com.db.gui.persistence.scheme.LayersGroup.class.getCanonicalName());
        queryRequestRemote.setQuery("GetAllModifiedLayersGroup");
        QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        List<BaseObject> modifiedLayersGroupList = queryResponseRemote.getResultList();
        LOGGER.debug("There are currently {} modified LayersGroup in total", modifiedLayersGroupList.size());

        queryRequestRemote = new QueryRequestRemote();
        queryRequestRemote.setClz(com.db.gui.persistence.scheme.Layer.class.getCanonicalName());
        queryRequestRemote.setQuery("GetAllModifiedLayers");
        queryResponseRemote = querySvcRemote.query(queryRequestRemote);
        modifiedLayersGroupList = queryResponseRemote.getResultList();
        LOGGER.debug("There are currently {} modified Layers in total", modifiedLayersGroupList.size());


        return modifiedLayersGroupList;
    }

    @Override
    public LayerGroup getRoot() {
        if (!synced)
            sync();
        return super.getRoot();
    }

    @Override
    public void flush() {
        super.flush();
        dataBaseUUID_layerGroups_Map.clear();
        guiUUID_dataBaseUUID_Map.clear();
        synced = false;
    }

    /**
     * The following function receives a gui layer and update the DB accordingly
     * @param layer
     */
    public void create(AbstractLayer layer) {
        LOGGER.debug("Adding Layer to the database: " + layer.toString());
        try {
            BaseLayer dbLayer;
            if (layer instanceof LayerGroupEditable) {
                dbLayer = objectCrudSvc.create(LayersGroup.class.getCanonicalName().toString());
                dbLayer.setName(layer.getName());
                List<String> uuid = new ArrayList<>();
                for (AbstractLayer l : ((LayerGroupEditable) layer).getChildren()) {
                    uuid.add(guiUUID_dataBaseUUID_Map.get(l.getUuid().toString()));
                }
                ((LayersGroup) dbLayer).setLayersUids(uuid);
                dbLayer = objectCrudSvc.update(dbLayer);
                layer.setPayload(dbLayer);
            }
            else {
                dbLayer = objectCrudSvc.create(Layer.class.getCanonicalName().toString());
                dbLayer.setName(layer.getName());
                dbLayer = objectCrudSvc.update(dbLayer);

                dbLayer = dbLayerLoaderMap.get(layer.getClass()).load(layer, dbLayer);
                layer.setPayload(dbLayer);
            }

            layer.loadMapObjects();


            // Update pointers
            dataBaseUUID_layerGroups_Map.put(dbLayer.getKeyId().getObjId(), layer);
            guiUUID_dataBaseUUID_Map.put(layer.getUuid().toString(), dbLayer.getKeyId().getObjId());

            // Updating parent
            BaseLayer parent = objectCrudSvc.read(guiUUID_dataBaseUUID_Map.get(layer.getParent().getUuid().toString()));
//            if (parent instanceof LayersGroupRoot) {
//                ((LayersGroupRoot) parent).getLayersUids().add(dbLayer.getKeyId().getObjId());
//            }
            if (parent instanceof LayersGroup) {
                ((LayersGroup) parent).getLayersUids().add(dbLayer.getKeyId().getObjId());
            }
            objectCrudSvc.update(parent);
        }
        catch (Throwable e) {
            LOGGER.error("Failed to create layer", e);
        }
    }

    public void delete(AbstractLayer layer) {
        try {
            LOGGER.debug("Removing Layer: " + layer);

            // Updating pointers
            String dataBaseUUID = guiUUID_dataBaseUUID_Map.remove(layer.getUuid().toString());
            dataBaseUUID_layerGroups_Map.remove(dataBaseUUID);

            BaseLayer deletionCandidate = objectCrudSvc.read(dataBaseUUID);

            // Updating parents
            BaseLayer parent = objectCrudSvc.read(guiUUID_dataBaseUUID_Map.get(layer.getParent().getUuid().toString()));
//            if (parent instanceof LayersGroupRoot) {
//                ((LayersGroupRoot) parent).getLayersUids().remove(deletionCandidate.getKeyId().getObjId());
//            }
            if (parent instanceof LayersGroup) {
                ((LayersGroup) parent).getLayersUids().remove(deletionCandidate.getKeyId().getObjId());
            }
            objectCrudSvc.update(parent);

            objectCrudSvc.delete(deletionCandidate);
        }
        catch (Exception e) {
            LOGGER.error("Failed to delete layer", e);
        }
    }

    // Loaders for the interraction between GUI and Databse

    public interface GuiLayer_From_DatabaseLayer_Loader {
        boolean isRelevant(BaseObject baseLayer) throws ObjectNotFoundRemoteException;
        AbstractLayer load(BaseObject baseLayer) throws ObjectNotFoundRemoteException;
    }

    public interface DatabaseLayer_From_GuiLayer_Loader {
        BaseLayer load(AbstractLayer guiLayer, BaseLayer dbLayer) throws ObjectNotFoundRemoteException, MissionUpdateException, DatabaseValidationRemoteException, ObjectInstanceRemoteException, PerimeterUpdateException, DrawUpdateException;
    }

    public void registerGuiLayerFromDbLayerLoader(Class<? extends BaseLayer> layerClass, GuiLayer_From_DatabaseLayer_Loader guiLayerLoader2) {
        if (guiLayerLoaderMap == null)
            guiLayerLoaderMap = new HashMap<>();

        if (!guiLayerLoaderMap.keySet().contains(layerClass))
            guiLayerLoaderMap.put(layerClass, new HashSet<>());

        guiLayerLoaderMap.get(layerClass).add(guiLayerLoader2);
    }

    public void registerDbLayerFromGuiLayerLoader(Class<? extends AbstractLayer> layerClass, DatabaseLayer_From_GuiLayer_Loader guiLayerLoader3) {
        if (dbLayerLoaderMap == null)
            dbLayerLoaderMap = new HashMap<>();

        dbLayerLoaderMap.put(layerClass, guiLayerLoader3);
    }
}
