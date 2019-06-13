package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Point;
import com.geo_tools.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by taljmars on 3/26/17.
 */
@Scope(value = "prototype")
@Component
public class CirclePerimeterEditorImpl extends PerimeterEditorImpl<CirclePerimeter> implements ClosablePerimeterEditor<CirclePerimeter>, CirclePerimeterEditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(CirclePerimeterEditorImpl.class);

    @Override
    public Class getManagedDBClass() {
        return CirclePerimeter.class;
    }

    @Override
    public CirclePerimeter open(CirclePerimeter perimeter) {
        return super.open(perimeter);
    }

    @Override
    public CirclePerimeter open(String perimeterName) {
        CirclePerimeter perimeter = super.open(perimeterName);
        perimeter.setCenter("");
        perimeter.setRadius(0.0);
        return perimeter;
    }

    @Override
    public void setRadius(double radius) {
        perimeter.setRadius(radius);
    }

    @Override
    public Point setCenter(Coordinate position){
        try {
            Point center = new Point();
            center.setLat(position.getLat());
            center.setLon(position.getLon());
            center.getKeyId().setObjId("DUMMY" + UUID.randomUUID().toString());
            perimeter.setCenter(center.getKeyId().getObjId());

            perimetersManager.updateItem(center);
            perimetersManager.updateItem(perimeter);
            return center;
        }
        catch (Exception e) {

        }
        return null;
    }

    @Override
    public Point getCenter() {
        String uuid = perimeter.getCenter();
        Point mItem = perimetersManager.getPointItem(uuid);
        return mItem;
    }

    @Override
    public void delete() {
        Point obj = perimetersManager.getPointItem(this.perimeter.getCenter());
        perimetersManager.removeItem(obj);

        perimetersManager.removeItem(perimeter);
    }
}
