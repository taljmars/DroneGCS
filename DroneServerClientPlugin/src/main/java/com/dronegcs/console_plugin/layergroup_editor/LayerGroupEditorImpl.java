package com.dronegcs.console_plugin.layergroup_editor;

import com.db.gui.persistence.scheme.BaseLayer;
import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.LayersGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by taljmars on 3/25/17.
 */
@Scope(scopeName = "prototype")
@Component
public class LayerGroupEditorImpl implements ClosableLayersGroupEditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(LayerGroupEditorImpl.class);

    @Autowired
    private LayersGroupsManager layersGroupManager;

    private LayersGroup layersGroup;

    @Override
    public LayersGroup open(LayersGroup layersGroup) {
        LOGGER.debug("Setting new layer group to layers group editor");
        this.layersGroup = layersGroup;
        layersGroupManager.updateItem(this.layersGroup);
        return layersGroup;
    }

    @Override
    public LayersGroup open(String layersGroupName) {// throws LayersGroupUpdateException {
        LOGGER.debug("Setting new LayersGroup to LayersGroup editor");
        this.layersGroup = new LayersGroup();
        this.layersGroup.getKeyId().setObjId("DUMMY" + UUID.randomUUID().toString());
        this.layersGroup.setName(layersGroupName);
        this.layersGroup.setLayersUids(new ArrayList<>());
        layersGroupManager.updateItem(this.layersGroup);
        return this.layersGroup;
    }

    @Override
    public LayersGroup getLayersGroup() {
        return this.layersGroup;
    }

    @Override
    public LayersGroup setLayersGroupName(String name){
        this.layersGroup.setName(name);
        return this.layersGroup;
    }

    @Override
    public Layer addSubLayer(String name) {
        Layer dbLayer = new Layer();
        String key = "DUMMY_" + UUID.randomUUID();
        dbLayer.getKeyId().setObjId(key);
        dbLayer.setName(name);
        List<String> layers = new ArrayList<>();
        layers.addAll(this.layersGroup.getLayersUids());
        layers.add(key);
        this.layersGroup.setLayersUids(layers);
        layersGroupManager.updateItem(dbLayer);
        return dbLayer;
    }

    @Override
    public LayersGroup addSubGroupLayer(String name) {
        LayersGroupEditor editor = layersGroupManager.openLayersGroupEditor(name);
        List<String> layers = new ArrayList<>();
        layers.addAll(this.layersGroup.getLayersUids());
        layers.add(editor.getLayersGroup().getKeyId().getObjId());
        this.layersGroup.setLayersUids(layers);
        return editor.getLayersGroup();
    }

    @Override
    public void deleteLayer() {
        String key = layersGroup.getKeyId().getObjId();
        for (String child : this.layersGroup.getLayersUids()) {
            BaseLayer obj = layersGroupManager.getLayerItem(child);
            if (obj instanceof LayersGroup) {
                LayersGroupEditor editor = layersGroupManager.openLayersGroupEditor((LayersGroup) obj);
                editor.deleteLayer();
            }
            else {
                layersGroupManager.removeItem(obj);
            }
        }
        layersGroupManager.removeItem(layersGroup);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LayersGroup Editor: ");
        builder.append(layersGroup.getKeyId().getObjId() + " ");
        builder.append(layersGroup.getName());
        return builder.toString();
    }
}
