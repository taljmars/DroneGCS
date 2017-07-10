package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.Perimeter;

/**
 * Created by taljmars on 3/27/17.
 */
public interface PerimeterEditorFactory {
    
    <R extends Perimeter, T extends PerimeterEditor<R>> T getEditor(Class<R> clz);
}
