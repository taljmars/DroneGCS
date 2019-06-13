package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.Perimeter;

/**
 * Created by taljmars on 3/26/17.
 */
public interface PerimeterEditor<T extends Perimeter> {

    Class<T> getManagedDBClass();

    T getPerimeter();

    T update(T perimeter);

    void delete();

    T setPerimeterName(String name);
}
