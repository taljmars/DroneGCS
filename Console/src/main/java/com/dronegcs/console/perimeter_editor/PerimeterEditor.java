package com.dronegcs.console.perimeter_editor;

import com.dronedb.persistence.scheme.perimeter.Perimeter;

/**
 * Created by oem on 3/26/17.
 */
public interface PerimeterEditor<T extends Perimeter> {

    T getModifiedPerimeter();

    T update(T perimeter);
}
