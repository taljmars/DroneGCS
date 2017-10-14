package com.dronegcs.console_plugin.perimeter_editor;

import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.dronedb.persistence.scheme.Point;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronegcs.console_plugin.remote_services_wrappers.PerimeterCrudSvcRemoteWrapper;
import com.geo_tools.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Created by taljmars on 3/26/17.
 */
@Scope(value = "prototype")
@Component
public class PolygonPerimeterEditorImpl extends PerimeterEditorImpl<PolygonPerimeter> implements ClosablePerimeterEditor<PolygonPerimeter>, PolygonPerimeterEditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(PolygonPerimeterEditorImpl.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get perimeter object crud")
    private PerimeterCrudSvcRemoteWrapper perimeterCrudSvcRemote;

    @Override
    public PolygonPerimeter open(PolygonPerimeter perimeter) throws PerimeterUpdateException {
        return super.open(perimeter);
    }

    @Override
    public PolygonPerimeter open(String perimeterName) throws PerimeterUpdateException {
        return super.open(perimeterName, PolygonPerimeter.class);
    }

    @Override
    public Point addPoint(Coordinate coordinate) throws PerimeterUpdateException {
        try {
            Point point = objectCrudSvcRemote.create(Point.class.getCanonicalName());
            point.setLat(coordinate.getLat());
            point.setLon(coordinate.getLon());

            // Update Item
            Point res = (Point) objectCrudSvcRemote.update(point);
            if (!perimeter.getPoints().contains(point.getKeyId().getObjId())) {
                perimeter.getPoints().add(res.getKeyId().getObjId());
            }
            // Update Mission
            perimeter = super.update(perimeter);
//            perimeter = (PolygonPerimeter) objectCrudSvcRemote.update(perimeter);
            return res;
        }
        catch (ObjectInstanceRemoteException | DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public void removePoint(Point point) throws PerimeterUpdateException {
        perimeter.getPoints().remove(point.getKeyId().getObjId());
//        try {
//            perimeter = (PolygonPerimeter) objectCrudSvcRemote.update(perimeter);
            perimeter = super.update(perimeter);
//        }
//        catch (ObjectInstanceRemoteException | DatabaseValidationRemoteException e) {
//            throw new PerimeterUpdateException(e.getMessage());
//        }
    }

    @Override
    public Point updatePoint(Point point) throws PerimeterUpdateException {
        try {
            return objectCrudSvcRemote.update(point);
        }
        catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

}
