package com.dronegcs.console_plugin.perimeter_editor;

import com.dronedb.persistence.scheme.Point;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronedb.persistence.ws.internal.*;
import com.dronegcs.console_plugin.ClosingPair;
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
        this.perimeter = perimeter;
        return this.perimeter;
    }

    @Override
    public PolygonPerimeter open(String perimeterName) throws PerimeterUpdateException {
        logger.debug("Setting new perimeter to perimeter editor");
        try {
            this.perimeter = (PolygonPerimeter) droneDbCrudSvcRemote.create(PolygonPerimeter.class.getName());
            this.perimeter.setName(perimeterName);
            this.perimeter = (PolygonPerimeter) droneDbCrudSvcRemote.update(this.perimeter);
            return this.perimeter;
        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

    @Override
    public ClosingPair<PolygonPerimeter> close(boolean shouldSave) {
        System.err.println("Close, should save:" + shouldSave);
        ClosingPair<PolygonPerimeter> perimeterClosingPair = null;
        PolygonPerimeter res = this.perimeter;
        if (!shouldSave) {
            System.err.println(String.format("Delete perimeter %s %s", res.getKeyId().getObjId(), res.getName()));
            //droneDbCrudSvcRemote.delete(this.perimeter);
            //res = this.originalPerimeter;
            try {
                res = (PolygonPerimeter) droneDbCrudSvcRemote.readByClass(perimeter.getKeyId().getObjId().toString(), PolygonPerimeter.class.getName());
                System.err.println("Found original perimeter " + res.getKeyId().getObjId() + " " + res.getName());
                perimeterClosingPair = new ClosingPair(res, false);
            } catch (ObjectNotFoundException e) {
                System.err.println("perimeter doesn't exist");
                perimeterClosingPair = new ClosingPair(this.perimeter, true);
            }
        }
        else {
            perimeterClosingPair = new ClosingPair(res, false);
        }

        this.perimeter = null;
        logger.debug("Perimeter editor finished");
        return perimeterClosingPair;
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

    @Override
    public void removePoint(Point point) throws PerimeterUpdateException {
        perimeter.getPoints().remove(point.getKeyId().getObjId());
        try {
            droneDbCrudSvcRemote.update(perimeter);
        }
        catch (DatabaseValidationRemoteException e) {
            throw new PerimeterUpdateException(e.getMessage());
        }
    }

}
