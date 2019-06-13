package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.Point;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.geo_tools.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by taljmars on 3/26/17.
 */
@Scope(value = "prototype")
@Component
public class PolygonPerimeterEditorImpl extends PerimeterEditorImpl<PolygonPerimeter> implements ClosablePerimeterEditor<PolygonPerimeter>, PolygonPerimeterEditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(PolygonPerimeterEditorImpl.class);

    @Override
    public Class getManagedDBClass() {
        return PolygonPerimeter.class;
    }

    @Override
    public PolygonPerimeter open(PolygonPerimeter perimeter) {
        return super.open(perimeter);
    }

    @Override
    public PolygonPerimeter open(String perimeterName) {
        PolygonPerimeter polygonPerimeter = super.open(perimeterName);
        polygonPerimeter.setPoints(new ArrayList<>());
        return polygonPerimeter;
    }

    @Override
    public Point addPoint(Coordinate coordinate) {
        Point point = new Point();
        point.getKeyId().setObjId("DUMMY" + UUID.randomUUID().toString());
        point.setLat(coordinate.getLat());
        point.setLon(coordinate.getLon());

        perimetersManager.updateItem(point);

        perimeter.getPoints().add(point.getKeyId().getObjId());

        return point;
    }

    @Override
    public void removePoint(Point point) {
        perimeter.getPoints().remove(point.getKeyId().getObjId());
        perimetersManager.removeItem(point);
    }

    @Override
    public Point updatePoint(Point item) {
        if (!perimeter.getPoints().contains(item.getKeyId().getObjId())) {
            LOGGER.debug("LayerItem {} is not part of the layer, adding it", item.getKeyId().getObjId());
            perimeter.getPoints().add(item.getKeyId().getObjId());
            LOGGER.debug("Layer items amount is now {} ", perimeter.getPoints().size());
        }
        perimetersManager.updateItem(item);
        return item;
    }

    @Override
    public List<Point> getPoints() {
        List<Point> pointItemList = new ArrayList<>();
        List<String> uuidList = perimeter.getPoints();
        uuidList.forEach((String uuid) -> {
            Point mItem = perimetersManager.getPointItem(uuid);
            pointItemList.add(mItem);
        });
        return pointItemList;
    }

    @Override
    public void delete() {
        for (String child : this.perimeter.getPoints()) {
            Point obj = perimetersManager.getPointItem(child);
            perimetersManager.removeItem(obj);
        }
        perimetersManager.removeItem(perimeter);
    }
}
