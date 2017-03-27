package com.dronegcs.console.perimeter_editor;

import com.dronedb.persistence.scheme.apis.DroneDbCrudSvcRemote;
import com.dronedb.persistence.scheme.perimeter.Perimeter;
import com.dronedb.persistence.services.DroneDbCrudSvc;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by oem on 3/27/17.
 */
public abstract class PerimeterEditorImpl<T extends Perimeter> implements PerimeterEditor<T> {

    @Autowired
    protected DroneDbCrudSvcRemote droneDbCrudSvcRemote;

    protected T perimeter;
    protected T originalPerimeter;

    @Override
    public T update(T perimeter) {
        this.perimeter = droneDbCrudSvcRemote.update(perimeter);
        return this.perimeter;
    }

    @Override
    public T getModifiedPerimeter() {
        return this.perimeter;
    }
}
