package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.ws.internal.DroneDbCrudSvcRemote;
import com.dronedb.persistence.scheme.Perimeter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by taljmars on 3/27/17.
 */
public abstract class PerimeterEditorImpl<T extends Perimeter> implements PerimeterEditor<T> {

    @Autowired
    protected DroneDbCrudSvcRemote droneDbCrudSvcRemote;

    protected T perimeter;
    protected T originalPerimeter;

    @Override
    public T update(T perimeter) {
        this.perimeter = (T) droneDbCrudSvcRemote.update(perimeter);
        return this.perimeter;
    }

    @Override
    public T getModifiedPerimeter() {
        return this.perimeter;
    }
}
