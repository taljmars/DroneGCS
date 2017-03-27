package com.dronegcs.console.perimeter_editor;

import com.dronedb.persistence.scheme.perimeter.Perimeter;

/**
 * Created by oem on 3/27/17.
 */
public interface PerimeterEditorFactory {
    
    <R extends Perimeter, T extends PerimeterEditor<R>> T getEditor(Class<R> clz);
}
