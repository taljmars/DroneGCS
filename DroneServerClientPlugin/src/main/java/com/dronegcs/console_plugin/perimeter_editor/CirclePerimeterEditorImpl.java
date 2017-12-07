package com.dronegcs.console_plugin.perimeter_editor;

import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Point;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
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
public class CirclePerimeterEditorImpl extends PerimeterEditorImpl<CirclePerimeter> implements ClosablePerimeterEditor<CirclePerimeter>, CirclePerimeterEditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(CirclePerimeterEditorImpl.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
    private ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

    @Override
    public CirclePerimeter open(CirclePerimeter perimeter) throws PerimeterUpdateException {
        return super.open(perimeter);
    }

    @Override
    public CirclePerimeter open(String perimeter) throws PerimeterUpdateException {
        return super.open(perimeter, CirclePerimeter.class);
    }

    @Override
    public void setRadius(double radius) throws PerimeterUpdateException {
        try {
            perimeter.setRadius(radius);
            perimeter = (CirclePerimeter) objectCrudSvcRemote.update(perimeter);
        } catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException  e) {
            throw new PerimeterUpdateException("Failed to set radius");
        }
    }

    @Override
    public Point setCenter(Coordinate position) throws PerimeterUpdateException{
        try {
            Point center = objectCrudSvcRemote.create(Point.class.getCanonicalName());
            center.setLat(position.getLat());
            center.setLon(position.getLon());
            center = objectCrudSvcRemote.update(center);
            perimeter.setCenter(center.getKeyId().getObjId());
            perimeter = objectCrudSvcRemote.update(perimeter);
            return center;
        }
        catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException e) {
            throw new PerimeterUpdateException("Failed to update circled perimeter central coordinates, " + e);
        }
    }
}
