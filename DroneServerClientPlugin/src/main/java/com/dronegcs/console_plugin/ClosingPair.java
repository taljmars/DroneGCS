package com.dronegcs.console_plugin;

import com.dronedb.persistence.scheme.BaseObject;

/**
 * Created by taljmars on 5/6/17.
 */
public class ClosingPair<T extends BaseObject> {

    private T object;
    private boolean isDeleted = false;

    public ClosingPair(T object, boolean isDeleted) {
        this.object = object;
        this.isDeleted = isDeleted;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public T getObject() {
        return object;
    }
}
