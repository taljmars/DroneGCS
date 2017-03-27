package com.dronegcs.console.perimeter_editor;

import com.dronedb.persistence.scheme.apis.PerimeterCrudSvcRemote;
import com.dronedb.persistence.scheme.apis.QuerySvcRemote;
import com.dronedb.persistence.scheme.perimeter.Point;
import com.dronedb.persistence.scheme.perimeter.PolygonPerimeter;
import com.dronegcs.console.services.DialogManagerSvc;
import com.dronegcs.console.services.LoggerDisplayerSvc;
import com.geo_tools.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Created by oem on 3/26/17.
 */
@Scope(value = "prototype")
@Component
public class PolygonPerimeterEditorImpl extends PerimeterEditorImpl<PolygonPerimeter> implements ClosablePerimeterEditor<PolygonPerimeter>, PolygonPerimeterEditor {

    @Autowired
    @NotNull(message = "Internal Error: Failed to get log displayer")
    private LoggerDisplayerSvc loggerDisplayerSvc;

    @Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
    private DialogManagerSvc dialogManagerSvc;

    @Autowired @NotNull(message = "Internal Error: Failed to get query")
    private QuerySvcRemote querySvcRemote;

    @Autowired @NotNull(message = "Internal Error: Failed to get perimeter object crud")
    private PerimeterCrudSvcRemote perimeterCrudSvcRemote;

    @Override
    public PolygonPerimeter open(PolygonPerimeter perimeter) {
        loggerDisplayerSvc.logGeneral("Setting new perimeter to perimeter editor");
        this.perimeter = (PolygonPerimeter) perimeter;
        this.originalPerimeter = perimeterCrudSvcRemote.clonePerimeter(this.perimeter);
        this.perimeter.setName(perimeter.getName());
        droneDbCrudSvcRemote.update(this.perimeter);
        return this.perimeter;
    }

    @Override
    public PolygonPerimeter open(String perimeterName) {
        loggerDisplayerSvc.logGeneral("Setting new perimeter to perimeter editor");
        this.perimeter = new PolygonPerimeter();
        this.originalPerimeter = null;
        this.perimeter.setName(perimeterName);
        droneDbCrudSvcRemote.update(this.perimeter);
        return this.perimeter;
    }

    @Override
    public PolygonPerimeter close(boolean shouldSave) {
        PolygonPerimeter res = this.perimeter;
        if (!shouldSave) {
            droneDbCrudSvcRemote.delete(this.perimeter);
            res = this.originalPerimeter;
        }
        else {
            if (originalPerimeter != null) droneDbCrudSvcRemote.delete(originalPerimeter);
        }
        System.out.println("Before resting " + res);
        this.originalPerimeter = null;
        this.perimeter = null;
        loggerDisplayerSvc.logGeneral("DronePolygon editor finished");
        System.out.println("After resting " + res);
        return res;
    }

    @Override
    public Point addPoint(Coordinate coordinate) {
        Point point = new Point();
        point.setLat(coordinate.getLat());
        point.setLon(coordinate.getLon());

        // Update Item
        Point res = droneDbCrudSvcRemote.update(point);
        perimeter.addPoint(res.getObjId());
        // Update Mission
        droneDbCrudSvcRemote.update(perimeter);
        return res;
    }
}
