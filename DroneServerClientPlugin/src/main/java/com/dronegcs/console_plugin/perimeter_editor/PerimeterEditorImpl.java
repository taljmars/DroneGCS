package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.Perimeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * Created by taljmars on 3/27/17.
 */
public abstract class PerimeterEditorImpl<T extends Perimeter> implements PerimeterEditor<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(PerimeterEditorImpl.class);

    protected T perimeter;

    @Autowired
    protected PerimetersManager perimetersManager;

    @Override
    public T update(T perimeter) {
        this.perimeter = perimeter;
        return this.perimeter;
    }

    @Override
    public T getPerimeter() {
        return this.perimeter;
    }

    protected T open(T perimeter) {
        LOGGER.debug("Setting new perimeter to perimeter editor");
        this.perimeter = perimeter;
        perimetersManager.updateItem(this.perimeter);
        return this.perimeter;
    }

    protected T open(String perimeterName) {
        LOGGER.debug("Setting new perimeter to perimeter editor");
        if (perimeterName == null || perimeterName.isEmpty()) {
            throw new RuntimeException("Perimeter name cannot be empty");
        }
        try {

            this.perimeter = getManagedDBClass().newInstance();
            this.perimeter.getKeyId().setObjId("DUMMY" + UUID.randomUUID().toString());
            this.perimeter.setName(perimeterName);
            perimetersManager.updateItem(this.perimeter);
            return this.perimeter;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public T setPerimeterName(String name) {
        this.perimeter.setName(name);
        return this.perimeter;
    }
}
