package com.drone_tester.Tests;

import com.db.persistence.scheme.BaseObject;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.scheme.Point;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterEditor;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.dronegcs.console_plugin.perimeter_editor.PolygonPerimeterEditor;
import com.geo_tools.Coordinate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
public class Test_PolylinePerimeters extends Test {

    private int idx = 0;
    private int total = 30;

    @Override
    public Status preTestCheck() {
        restClientHelper.setToken(login("tester1", "tester1"));
//        System.out.println(restClientHelper.getToken());

        Assert.isTrue(perimetersManager.getAllPerimeters().isEmpty());
        publish(new TestEvent(this, Status.IN_PROGRESS, "No perimeters", ++idx, total));

        Assert.isTrue(perimetersManager.getAllModifiedPerimeters().isEmpty());
        publish(new TestEvent(this, Status.IN_PROGRESS, "No mod perimeter", ++idx, total));

        publish(new TestEvent(this, Status.IN_PROGRESS, "pre test checked", ++idx, total));
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        try {
            // Creating and discard mission

            PerimeterEditor perimeterEditor = perimetersManager.openPerimeterEditor("talma1", PolygonPerimeter.class);
            Perimeter perimeter = perimeterEditor.getModifiedPerimeter();
            publish(new TestEvent(this, Status.IN_PROGRESS, "Creating perimeter", ++idx, total));

            perimeter.setName("talma1to2");
            perimeter = perimetersManager.update(perimeter);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Updating perimeter name", ++idx, total));

            String LAST_NAME = "talma2to3";
            perimeter.setName(LAST_NAME);
            perimeter = perimetersManager.update(perimeter);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Updating perimeter name once again", ++idx, total));

            perimeterEditor = perimetersManager.openPerimeterEditor(perimeter);
            Assert.isTrue(perimeterEditor.getModifiedPerimeter().getName().equals(LAST_NAME));
            publish(new TestEvent(this, Status.IN_PROGRESS, "Verify it is possible to fetch perimeter by name", ++idx, total));

            PolygonPerimeterEditor polygonPerimeterEditor = (PolygonPerimeterEditor) perimeterEditor;
            polygonPerimeterEditor.addPoint(new Coordinate(11.11, 11.11));
            polygonPerimeterEditor.addPoint(new Coordinate(22.22, 22.22));
            Point point = polygonPerimeterEditor.addPoint(new Coordinate(33.33, 33.33));
            publish(new TestEvent(this, Status.IN_PROGRESS, "Adding points to polyline perimeters", ++idx, total));

            PolygonPerimeter polygonPerimeter = polygonPerimeterEditor.getModifiedPerimeter();
            Assert.isTrue(polygonPerimeter.getPoints().size() == 3);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Validate points amount", ++idx, total));

            point.setLat(44.44);
            point = polygonPerimeterEditor.updatePoint(point);
            Assert.isTrue(point.getLat() == 44.44);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify point can be changed", ++idx, total));

            List<Point> pointList = perimetersManager.getPoints(polygonPerimeter);
            Assert.isTrue(pointList.size() == 3);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify points amount", ++idx, total));

            Assert.isTrue(pointList.get(2).getLat() == 44.44);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Validate point modification", ++idx, total));

            perimetersManager.closePerimeterEditor(polygonPerimeterEditor, true);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Closing editor", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish perimeter changes", ++idx, total));

            List<BaseObject> perimeterList = perimetersManager.getAllPerimeters();
            Assert.isTrue(perimeterList.size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "get all perimeters", ++idx, total));

            polygonPerimeter = (PolygonPerimeter) perimeterList.get(0);
            Assert.isTrue(polygonPerimeter.getPoints().size() == 3);
            publish(new TestEvent(this, Status.IN_PROGRESS, "check point amount of perimeter", ++idx, total));


            polygonPerimeterEditor = perimetersManager.openPerimeterEditor(polygonPerimeter);
            polygonPerimeter = polygonPerimeterEditor.delete();
            publish(new TestEvent(this, Status.IN_PROGRESS, "delete perimeter", ++idx, total));

            pointList = perimetersManager.getPoints(polygonPerimeter);
            Assert.isTrue(pointList.get(2).getLat() == 44.44);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Validate point #3 recent values", ++idx, total));

            Assert.isTrue(polygonPerimeter.isDeleted());
            publish(new TestEvent(this, Status.IN_PROGRESS, "Validate deleted mark", ++idx, total));

            perimetersManager.closePerimeterEditor(polygonPerimeterEditor, false);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Closing editor", ++idx, total));

            sessionsSvcRemoteWrapper.discard();
            publish(new TestEvent(this, Status.IN_PROGRESS, "Discarding deletion", ++idx, total));

            perimeterList = perimetersManager.getAllPerimeters();
            Assert.isTrue(perimeterList.size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "get all perimeters", ++idx, total));

            polygonPerimeter = (PolygonPerimeter) perimeterList.get(0);
            Assert.isTrue(!polygonPerimeter.isDeleted());
            publish(new TestEvent(this, Status.IN_PROGRESS, "Validate non deleted mark", ++idx, total));

            publish(new TestEvent(this, Status.IN_PROGRESS, "test core finished", ++idx, total));
            return Status.SUCCESS;
        }
        catch (PerimeterUpdateException e) {
            e.printStackTrace();
            return Status.FAIL;
        }
    }

    @Override
    public Status postTestCleanup() {
        try {
            List<BaseObject> perimeterList = perimetersManager.getAllPerimeters();
            Assert.isTrue(perimeterList.size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "get all perimeters", ++idx, total));


            for (BaseObject perimeter : perimeterList) {
                perimetersManager.delete((Perimeter) perimeter);
            }
            publish(new TestEvent(this, Status.IN_PROGRESS, "delete perimeters", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish deletion", ++idx, total));

            Assert.isTrue(perimetersManager.getAllModifiedPerimeters().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify not perimeters exist", ++idx, total));

            Assert.isTrue(perimetersManager.getAllPerimeters().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify not modified perimeters exist", ++idx, total));

            logout();

            publish(new TestEvent(this, Status.SUCCESS, "test completed", ++idx, total));
            return Status.SUCCESS;
        }
        catch (PerimeterUpdateException e) {
            e.printStackTrace();
            return Status.FAIL;
        }
    }

}
