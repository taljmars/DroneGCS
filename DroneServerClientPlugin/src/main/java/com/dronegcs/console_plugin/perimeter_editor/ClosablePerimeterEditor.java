package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.Perimeter;
import com.dronegcs.console_plugin.ClosingPair;

/**
 * Created by taljmars on 3/26/17.
 */
public interface ClosablePerimeterEditor<T extends Perimeter> extends PerimeterEditor<T> {

    T open(T perimeter) throws PerimeterUpdateException;

    T open(String perimeter) throws PerimeterUpdateException;

    ClosingPair<T> close(boolean shouldSave) throws PerimeterUpdateException ;
}
