package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.Point;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronedb.persistence.ws.internal.DatabaseValidationRemoteException;
import com.dronedb.persistence.ws.internal.ObjectNotFoundRemoteException;
import com.dronedb.persistence.ws.internal.PerimeterCrudSvcRemote;
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
public class PolygonPerimeterEditorImpl extends PerimeterEditorImpl<PolygonPerimeter> implements ClosablePerimeterEditor<PolygonPerimeter>, PolygonPerimeterEditor {

    private final static Logger logger = Logger.getLogger(PolygonPerimeterEditorImpl.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get query")
    private QuerySvcRemote querySvcRemote;

    @Autowired @NotNull(message = "Internal Error: Failed to get perimeter object crud")
    private PerimeterCrudSvcRemote perimeterCrudSvcRemote;

    @Override
    public PolygonPerimeter open(PolygonPerimeter perimeter) throws PerimeterUpdateException {
        logger.debug("Setting new perimeter to perimeter editor");
        try {
            this.perimeter = (PolygonPerimeter) perimeter;
            this.originalPerimeter = (PolygonPerimeter) perimeterCrudSvcRemote.clonePerimeter(this.perimeter);
            this.perimeter.setName(perimeter.getName());

            droneDbCrudSvcRemote.update(this.perimeter);
            return this.perimeter;
        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        } catch (ObjectNotFoundRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public PolygonPerimeter open(String perimeterName) throws PerimeterUpdateException {
        logger.debug("Setting new perimeter to perimeter editor");
        try {
            this.perimeter = new PolygonPerimeter();
            this.originalPerimeter = null;
            this.perimeter.setName(perimeterName);
            droneDbCrudSvcRemote.update(this.perimeter);
            return this.perimeter;
        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public PolygonPerimeter close(boolean shouldSave) throws PerimeterUpdateException {
        try {
            PolygonPerimeter res = this.perimeter;
            if (!shouldSave) {
                droneDbCrudSvcRemote.delete(this.perimeter);
                res = this.originalPerimeter;
            } else {
                if (originalPerimeter != null) droneDbCrudSvcRemote.delete(originalPerimeter);
            }
            System.out.println("Before resting " + res);
            this.originalPerimeter = null;
            this.perimeter = null;
            logger.debug("DronePolygon editor finished");
            System.out.println("After resting " + res);
            return res;
        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public Point addPoint(Coordinate coordinate) throws PerimeterUpdateException {
        try {
            Point point = new Point();
            point.setLat(coordinate.getLat());
            point.setLon(coordinate.getLon());

            // Update Item
            Point res = (Point) droneDbCrudSvcRemote.update(point);
            perimeter.getPoints().add(res.getKeyId().getObjId());
            // Update Mission
            droneDbCrudSvcRemote.update(perimeter);
            return res;
        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }
}
