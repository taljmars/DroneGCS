package com.dronegcs.console_plugin.layergroup_editor;

import com.db.gui.persistence.scheme.LayersGroup;
import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Created by taljmars on 3/25/17.
 */
@Scope(scopeName = "prototype")
@Component
public class LayerGroupEditorImpl implements ClosableLayersGroupEditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(LayerGroupEditorImpl.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
    private ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

    private LayersGroup layersGroup;

    @Override
    public LayersGroup open(LayersGroup layersGroup) {
        LOGGER.debug("Setting new layer group to layers group editor");
        try {
            this.layersGroup = layersGroup;
            return layersGroup;
        }
        catch (Throwable e) {
            LOGGER.debug("Failed to open layer group editor", e);
        }
        return null;
    }

    @Override
    public LayersGroup open(String layersGroupName) throws LayersGroupUpdateException {
        LOGGER.debug("Setting new LayersGroup to LayersGroup editor");
        try {
            this.layersGroup = objectCrudSvcRemote.create(LayersGroup.class.getCanonicalName());
            this.layersGroup.setName(layersGroupName);
            this.layersGroup = objectCrudSvcRemote.update(this.layersGroup);
            return this.layersGroup;
        }
        catch (ObjectInstanceRemoteException e) {
            throw new LayersGroupUpdateException(e.getMessage());
        }
        catch (DatabaseValidationRemoteException e) {
            throw new LayersGroupUpdateException(e.getMessage());
        }
    }

    @Override
    public ClosingPair<LayersGroup> close(boolean shouldSave) {
        LOGGER.debug("Close, should save:" + shouldSave);
        ClosingPair<LayersGroup> layersGroupClosingPair = null;
        LayersGroup res = this.layersGroup;
        if (!shouldSave) {
            LOGGER.debug(String.format("Delete layer group %s %s", this.layersGroup.getKeyId().getObjId(), this.layersGroup.getName()));
            try {
                res = objectCrudSvcRemote.readByClass(layersGroup.getKeyId().getObjId(), LayersGroup.class.getCanonicalName());
                LOGGER.debug("Found original layersGroup" + res.getKeyId().getObjId() + " " + res.getName());
                layersGroupClosingPair = new ClosingPair(res, false);
            }
            catch (ObjectNotFoundRemoteException e) {
                LOGGER.error("LayersGroup doesn't exist");
                layersGroupClosingPair = new ClosingPair(this.layersGroup, true);
            }
        }
        else {
            layersGroupClosingPair = new ClosingPair(res, false);
        }
        this.layersGroup = null;
        LOGGER.debug("LayersGroup editor finished");
        return layersGroupClosingPair;
    }

    @Override
    public LayersGroup getLayersGroup() {
        return this.layersGroup;
    }

    @Override
    public LayersGroup update(LayersGroup layersGroup) throws LayersGroupUpdateException {
        try {
            LOGGER.debug("Current LayersGroup named '{}'", this.layersGroup.getName());
            LOGGER.debug("After update, LayersGroup will be named '{}'", layersGroup.getName());
            this.layersGroup.setName(layersGroup.getName());
            this.layersGroup = objectCrudSvcRemote.update(layersGroup);

            LOGGER.debug("Updated LayersGroup name is '{}'", this.layersGroup.getName());
            return this.layersGroup;
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new LayersGroupUpdateException(e.getMessage());
        }
    }

    @Override
    public LayersGroup delete() throws LayersGroupUpdateException {
        try {
            this.layersGroup = objectCrudSvcRemote.delete(layersGroup);
            return this.layersGroup;
        }
        catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException | ObjectNotFoundRemoteException e) {
            throw new LayersGroupUpdateException(e.getMessage());
        }
    }

    @Override
    public LayersGroup setLayersGroupName(String name) throws LayersGroupUpdateException {
        try {
            this.layersGroup.setName(name);
            this.layersGroup = objectCrudSvcRemote.update(layersGroup);
            return this.layersGroup;
        }
        catch (Exception e) {
            throw new LayersGroupUpdateException(e.getMessage());
        }
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
