package com.drone_tester.Tests;

import com.db.persistence.scheme.BaseObject;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.scheme.Point;
import com.dronegcs.console_plugin.perimeter_editor.CirclePerimeterEditor;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterEditor;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.geo_tools.Coordinate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
public class Test_CirclePerimeters extends Test {

    private int idx = 0;
    private int total = 28;

    @Override
    public Status preTestCheck() {
        restClientHelper.setToken(login("tester1", "tester1"));
        System.out.println(restClientHelper.getToken());

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

            PerimeterEditor perimeterEditor = perimetersManager.openPerimeterEditor("talma1", CirclePerimeter.class);
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

            CirclePerimeterEditor circlePerimeterEditor = (CirclePerimeterEditor) perimeterEditor;
            Point center = circlePerimeterEditor.setCenter(new Coordinate(11.11, 11.11));
            circlePerimeterEditor.setRadius(20);
            publish(new TestEvent(this, Status.IN_PROGRESS, "setting center and radius to circle perimeters", ++idx, total));

            CirclePerimeter circlePerimeter = circlePerimeterEditor.getModifiedPerimeter();
            Assert.isTrue(circlePerimeter.getRadius() == 20);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Validate radius", ++idx, total));

            center = circlePerimeterEditor.setCenter(new Coordinate(44.44, 44.44));
            Assert.isTrue(center.getLat() == 44.44);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify point can be changed", ++idx, total));

            List<Point> pointList = perimetersManager.getPoints(circlePerimeter);
            Assert.isTrue(pointList.size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify points amount", ++idx, total));

            Assert.isTrue(pointList.get(0).getLat() == 44.44);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Validate point modification", ++idx, total));

            perimetersManager.closePerimeterEditor(circlePerimeterEditor, true);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Closing editor", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish perimeter changes", ++idx, total));

            List<BaseObject> perimeterList = perimetersManager.getAllPerimeters();
            Assert.isTrue(perimeterList.size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "get all perimeters", ++idx, total));

            pointList = perimetersManager.getPoints(circlePerimeter);
            Assert.isTrue(pointList.get(0).getLat() == 44.44);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Validate center point recent values", ++idx, total));

            circlePerimeterEditor = perimetersManager.openPerimeterEditor(circlePerimeter);
            circlePerimeter = circlePerimeterEditor.delete();
            publish(new TestEvent(this, Status.IN_PROGRESS, "delete perimeter", ++idx, total));

            Assert.isTrue(circlePerimeter.isDeleted());
            publish(new TestEvent(this, Status.IN_PROGRESS, "Validate deleted mark", ++idx, total));

            sessionsSvcRemoteWrapper.discard();
            publish(new TestEvent(this, Status.IN_PROGRESS, "Discarding deletion", ++idx, total));

            perimeterList = perimetersManager.getAllPerimeters();
            Assert.isTrue(perimeterList.size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "get all perimeters", ++idx, total));

            circlePerimeter = (CirclePerimeter) perimeterList.get(0);
            Assert.isTrue(!circlePerimeter.isDeleted());
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
