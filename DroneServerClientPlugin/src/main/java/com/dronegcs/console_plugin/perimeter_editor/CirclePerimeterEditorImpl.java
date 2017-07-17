package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Point;
import com.dronedb.persistence.ws.internal.DatabaseValidationRemoteException;
import com.dronedb.persistence.ws.internal.DroneDbCrudSvcRemote;
import com.dronedb.persistence.ws.internal.QuerySvcRemote;
import com.geo_tools.Coordinate;
import org.apache.log4j.Logger;
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

    private final static Logger logger = Logger.getLogger(CirclePerimeterEditorImpl.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
    private DroneDbCrudSvcRemote droneDbCrudSvcRemote;

    @Autowired @NotNull(message = "Internal Error: Failed to get query")
    private QuerySvcRemote querySvcRemote;

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
            perimeter = (CirclePerimeter) droneDbCrudSvcRemote.update(perimeter);
        } catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException("Failed to set radius");
        }
    }

    @Override
    public void setCenter(Coordinate position) {
        try {
            Point center = (Point) droneDbCrudSvcRemote.create(Point.class.getName());
            center.setLat(position.getLat());
            center.setLon(position.getLon());
            center = (Point) droneDbCrudSvcRemote.update(center);
            perimeter.setCenter(center.getKeyId().getObjId());
            perimeter = (CirclePerimeter) droneDbCrudSvcRemote.update(perimeter);
        } catch (DatabaseValidationRemoteException e) {
            e.printStackTrace();
        }
    }
}
