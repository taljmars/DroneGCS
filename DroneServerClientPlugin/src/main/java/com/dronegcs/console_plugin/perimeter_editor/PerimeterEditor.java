package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.Perimeter;

/**
 * Created by taljmars on 3/26/17.
 */
public interface PerimeterEditor<T extends Perimeter> {

    T getModifiedPerimeter();

    T update(T perimeter) throws PerimeterUpdateException;
}
