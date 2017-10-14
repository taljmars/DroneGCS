package com.dronegcs.console_plugin.perimeter_editor;

import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by taljmars on 3/27/17.
 */
public abstract class PerimeterEditorImpl<T extends Perimeter> implements PerimeterEditor<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(PerimeterEditorImpl.class);

    @Autowired
    protected ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

    protected T perimeter;

    @Override
    public T update(T perimeter) throws PerimeterUpdateException {
        try {
            this.perimeter = (T) objectCrudSvcRemote.update(perimeter);
            return this.perimeter;
        }
        catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public T delete() throws PerimeterUpdateException {
        try {
            this.perimeter = objectCrudSvcRemote.delete(perimeter);
            return this.perimeter;
        }
        catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException | ObjectNotFoundRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public T getModifiedPerimeter() {
        return this.perimeter;
    }

    protected T open(T perimeter) throws PerimeterUpdateException {
        LOGGER.debug("Setting new perimeter to perimeter editor");
        this.perimeter = perimeter;
        return this.perimeter;
    }

    protected T open(String perimeterName, Class<T> perimeterClass) throws PerimeterUpdateException {
        LOGGER.debug("Setting new perimeter to perimeter editor");
        try {
            this.perimeter = perimeterClass.cast(objectCrudSvcRemote.create(perimeterClass.getCanonicalName()));
            this.perimeter.setName(perimeterName);
            this.perimeter = perimeterClass.cast(objectCrudSvcRemote.update(this.perimeter));
            return this.perimeter;
        }
        catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    public ClosingPair<T> close(boolean shouldSave) {
        LOGGER.debug("Close, should save:" + shouldSave);
        ClosingPair<T> perimeterClosingPair = null;
        T res = this.perimeter;
        if (!shouldSave) {
            LOGGER.debug(String.format("Delete perimeter %s %s", res.getKeyId().getObjId(), res.getName()));
            //objectCrudSvcRemote.delete(this.perimeter);
            //res = this.originalPerimeter;
            try {
                res = (T) objectCrudSvcRemote.readByClass(perimeter.getKeyId().getObjId(), perimeter.getClass().getCanonicalName());
                LOGGER.debug("Found original perimeter " + res.getKeyId().getObjId() + " " + res.getName());
                perimeterClosingPair = new ClosingPair(res, false);
            }
            catch (ObjectNotFoundRemoteException e) {
                LOGGER.debug("perimeter doesn't exist");
                perimeterClosingPair = new ClosingPair(this.perimeter, true);
            }
        }
        else {
            perimeterClosingPair = new ClosingPair(res, false);
        }

        this.perimeter = null;
        LOGGER.debug("Perimeter editor finished");
        return perimeterClosingPair;
    }
}
