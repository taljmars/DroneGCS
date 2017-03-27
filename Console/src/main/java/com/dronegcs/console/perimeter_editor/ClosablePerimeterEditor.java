package com.dronegcs.console.perimeter_editor;

import com.dronedb.persistence.scheme.perimeter.Perimeter;

/**
 * Created by oem on 3/26/17.
 */
public interface ClosablePerimeterEditor<T extends Perimeter> extends PerimeterEditor<T> {

    T open(T perimeter);

    T open(String perimeter);

    T close(boolean shouldSave);
}
