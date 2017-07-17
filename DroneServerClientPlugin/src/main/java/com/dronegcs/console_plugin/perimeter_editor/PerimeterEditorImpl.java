package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.ws.internal.DatabaseValidationRemoteException;
import com.dronedb.persistence.ws.internal.DroneDbCrudSvcRemote;
import com.dronedb.persistence.ws.internal.ObjectNotFoundException;
import com.dronegcs.console_plugin.ClosingPair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by taljmars on 3/27/17.
 */
public abstract class PerimeterEditorImpl<T extends Perimeter> implements PerimeterEditor<T> {

    private final static Logger logger = Logger.getLogger(PerimeterEditorImpl.class);

    @Autowired
    protected DroneDbCrudSvcRemote droneDbCrudSvcRemote;

    protected T perimeter;

    @Override
    public T update(T perimeter) throws PerimeterUpdateException {
        try {
            this.perimeter = (T) droneDbCrudSvcRemote.update(perimeter);
            return this.perimeter;
        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public void delete() throws PerimeterUpdateException {
        try {
            droneDbCrudSvcRemote.delete(perimeter);
        } catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public T getModifiedPerimeter() {
        return this.perimeter;
    }

    protected T open(T perimeter) throws PerimeterUpdateException {
        logger.debug("Setting new perimeter to perimeter editor");
        this.perimeter = perimeter;
        return this.perimeter;
    }

    protected T open(String perimeterName, Class<T> perimeterClass) throws PerimeterUpdateException {
        logger.debug("Setting new perimeter to perimeter editor");
        try {
            this.perimeter = perimeterClass.cast(droneDbCrudSvcRemote.create(perimeterClass.getName()));
            this.perimeter.setName(perimeterName);
            this.perimeter = perimeterClass.cast(droneDbCrudSvcRemote.update(this.perimeter));
            return this.perimeter;
        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    public ClosingPair<T> close(boolean shouldSave) {
        System.err.println("Close, should save:" + shouldSave);
        ClosingPair<T> perimeterClosingPair = null;
        T res = this.perimeter;
        if (!shouldSave) {
            System.err.println(String.format("Delete perimeter %s %s", res.getKeyId().getObjId(), res.getName()));
            //droneDbCrudSvcRemote.delete(this.perimeter);
            //res = this.originalPerimeter;
            try {
                res = (T) droneDbCrudSvcRemote.readByClass(perimeter.getKeyId().getObjId().toString(), perimeter.getClass().getName());
                System.err.println("Found original perimeter " + res.getKeyId().getObjId() + " " + res.getName());
                perimeterClosingPair = new ClosingPair(res, false);
            } catch (ObjectNotFoundException e) {
                System.err.println("perimeter doesn't exist");
                perimeterClosingPair = new ClosingPair(this.perimeter, true);
            }
        }
        else {
            perimeterClosingPair = new ClosingPair(res, false);
        }

        this.perimeter = null;
        logger.debug("Perimeter editor finished");
        return perimeterClosingPair;
    }
}
